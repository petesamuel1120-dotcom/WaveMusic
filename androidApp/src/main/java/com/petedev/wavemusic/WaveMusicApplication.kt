package com.petedev.wavemusic

import android.annotation.SuppressLint
import android.app.Application
import android.app.NotificationManager
import android.database.CursorWindow
import android.os.Build
import android.util.Log
import androidx.work.Constraints
import androidx.work.Configuration
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import cat.ereza.customactivityoncrash.config.CaocConfig
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.disk.DiskCache
import coil3.network.okhttp.OkHttpNetworkFetcherFactory
import coil3.request.CachePolicy
import com.petedev.wavemusic.common.AppIdentity
import com.petedev.wavemusic.data.di.loader.loadAllModules
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.di.viewModelModule
import com.petedev.wavemusic.service.backup.AutoBackupScheduler
import com.petedev.wavemusic.service.rss.RssFeedNotifyWork
import com.petedev.wavemusic.service.test.notification.NotifyWork
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import multiplatform.network.cmptoast.AppContext
import okhttp3.OkHttpClient
import okio.FileSystem
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.startKoin
import org.koin.core.logger.Level
import com.petedev.wavemusic.crashlytics.configCrashlytics
import com.petedev.wavemusic.lastfm.configLastfm
import java.lang.reflect.Field
import java.util.concurrent.TimeUnit

class WaveMusicApplication :
    Application(),
    Configuration.Provider,
    KoinComponent,
    SingletonImageLoader.Factory {

    // WorkManager is only needed for background scheduling. Providing its configuration lets
    // WorkManager initialize on first use instead of doing database/scheduler setup on the launch
    // thread in Application.onCreate().
    override val workManagerConfiguration: Configuration
        get() =
            Configuration
                .Builder()
                .setMinimumLoggingLevel(Log.INFO)
                .build()

    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
    private val dataStoreManager: DataStoreManager by inject()
    // Reuse one connection pool and dispatcher for every artwork request. Creating a client from
    // the Coil factory callback per fetch wastes threads and connections during feed population.
    private val imageHttpClient: OkHttpClient by lazy(LazyThreadSafetyMode.SYNCHRONIZED) {
        OkHttpClient.Builder().build()
    }

    override fun onCreate() {
        super.onCreate()
        configCrashlytics(this, BuildKonfig.sentryDsn)
        configLastfm(BuildKonfig.lastfmApiKey, BuildKonfig.lastfmSecret)
        startKoin {
            androidLogger(level = if (BuildConfig.DEBUG) Level.DEBUG else Level.ERROR)
            androidContext(this@WaveMusicApplication)
            loadAllModules(
                AppIdentity(
                    applicationId = BuildConfig.APPLICATION_ID,
                    versionName = BuildConfig.VERSION_NAME,
                    platform = "Android ${Build.VERSION.RELEASE}",
                ),
            )
            loadKoinModules(viewModelModule)
        }
        // Work scheduling and cache-index loading both touch disk. Keep low-priority startup
        // maintenance away from the launch path and let media caches open only when playback or
        // download recovery actually needs them.
        applicationScope.launch {
            delay(15_000L)
            scheduleBackgroundWork()
            // Allow the initial work requests to settle before starting the preference observer,
            // which may enqueue or cancel another request on its first emission.
            delay(5_000L)
            AutoBackupScheduler(this@WaveMusicApplication, dataStoreManager).observeAndSchedule()
        }

        CaocConfig.Builder
            .create()
            .backgroundMode(CaocConfig.BACKGROUND_MODE_SILENT) // default: CaocConfig.BACKGROUND_MODE_SHOW_CUSTOM
            .enabled(true) // default: true
            .showErrorDetails(true) // default: true
            .showRestartButton(true) // default: true
            .errorDrawable(R.mipmap.ic_launcher_round)
            .logErrorOnRestart(false) // default: true
            .trackActivities(true) // default: false
            .minTimeBetweenCrashesMs(2000) // default: 3000 //default: bug image
            .restartActivity(MainActivity::class.java) // default: null (your app's launch activity)
            .apply()

        @SuppressLint("DiscouragedPrivateApi")
        val field: Field = CursorWindow::class.java.getDeclaredField("sCursorWindowSize")
        field.isAccessible = true
        val expectSize = 100 * 1024 * 1024
        field.set(null, expectSize)

        AppContext.apply {
            set(applicationContext)
        }
    }

    override fun onTerminate() {
        super.onTerminate()

        Logger.w("Terminate", "Checking")
    }

    private fun scheduleBackgroundWork() {
        val request =
            PeriodicWorkRequestBuilder<NotifyWork>(12L, TimeUnit.HOURS)
                .addTag("Worker Test")
                .setConstraints(
                    Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build(),
                ).build()
        val workManager = WorkManager.getInstance(applicationContext)
        workManager.enqueueUniquePeriodicWork(
            "Artist Worker",
            ExistingPeriodicWorkPolicy.KEEP,
            request,
        )
        workManager.cancelUniqueWork(BLOG_RSS_WORK_NAME)
        workManager.cancelAllWorkByTag(RssFeedNotifyWork::class.java.name)
        workManager.cancelAllWorkByTag(BLOG_RSS_WORK_NAME)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                .deleteNotificationChannel(BLOG_NOTIFICATION_CHANNEL_ID)
        }
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader =
        ImageLoader
            .Builder(context)
            .components {
                add(
                    OkHttpNetworkFetcherFactory(
                        callFactory = {
                            imageHttpClient
                        },
                    ),
                )
            }.diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .diskCache(
                DiskCache
                    .Builder()
                    .directory(FileSystem.SYSTEM_TEMPORARY_DIRECTORY / "image_cache")
                    .maxSizeBytes(512L * 1024 * 1024)
                    .build(),
            )
            // Feed artwork is loaded in batches. Crossfading every thumbnail adds simultaneous
            // alpha animations and GPU overdraw during cold start and tab entry; screens that need
            // a hero-image transition can opt in on their individual request.
            .build()
}

private const val BLOG_RSS_WORK_NAME = "Blog RSS Worker"
private const val BLOG_NOTIFICATION_CHANNEL_ID = "blog_updates_channel"
