package com.petedev.wavemusic

import android.Manifest
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.core.net.toUri
import androidx.core.os.LocaleListCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.eygraber.uri.toKmpUriOrNull
import com.petedev.wavemusic.common.FIRST_TIME_MIGRATION
import com.petedev.wavemusic.common.SELECTED_LANGUAGE
import com.petedev.wavemusic.common.STATUS_DONE
import com.petedev.wavemusic.common.SUPPORTED_LANGUAGE
import com.petedev.wavemusic.domain.data.model.intent.GenericIntent
import com.petedev.wavemusic.domain.manager.DataStoreManager
import com.petedev.wavemusic.domain.mediaservice.handler.MediaPlayerHandler
import com.petedev.wavemusic.domain.mediaservice.handler.ToastType
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.media3.di.setServiceActivitySession
import com.petedev.wavemusic.di.viewModelModule
import com.petedev.wavemusic.utils.ComposeResUtils
import com.petedev.wavemusic.utils.VersionManager
import com.petedev.wavemusic.viewModel.SharedViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.android.inject
import org.koin.core.context.loadKoinModules
import org.koin.core.context.unloadKoinModules
import org.koin.dsl.module
import com.petedev.wavemusic.crashlytics.pushPlayerError
import pub.devrel.easypermissions.EasyPermissions
import java.util.Locale

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    val viewModel: SharedViewModel by inject()
    val mediaPlayerHandler by inject<MediaPlayerHandler>()
    val dataStoreManager: DataStoreManager by inject()

    private var mBound = false
    private var shouldUnbind = false
    private val serviceConnection =
        object : ServiceConnection {
            override fun onServiceConnected(
                name: ComponentName?,
                service: IBinder?,
            ) {
//                mediaPlayerHandler.setActivitySession(this@MainActivity, MainActivity::class.java, service)
                setServiceActivitySession(this@MainActivity, MainActivity::class.java, service)
                Logger.w("MainActivity", "onServiceConnected: ")
                mBound = true
            }

            override fun onServiceDisconnected(name: ComponentName?) {
                Logger.w("MainActivity", "onServiceDisconnected: ")
                mBound = false
            }
        }

    override fun onStart() {
        super.onStart()
        // Service/player setup can initialize Media3 and databases. Let the first Home frame
        // settle before adding that startup work to the CPU, especially after a fresh install or
        // process death. The existing service keeps playing while a foreground Activity reconnects.
        window.decorView.postDelayed({
            if (!isFinishing && !isDestroyed && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                startMusicService()
            }
        }, 300L)
    }

    override fun onStop() {
        super.onStop()
        if (shouldUnbind) {
            unbindService(serviceConnection)
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Logger.d("MainActivity", "onNewIntent: $intent")
        viewModel.setIntent(
            GenericIntent(
                action = intent.action,
                data = (intent.data ?: intent.getStringExtra(Intent.EXTRA_TEXT)?.toUri())?.toKmpUriOrNull(),
                type = intent.type,
            ),
        )
    }

    @ExperimentalFoundationApi
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        loadKoinModules(
            module {
                single<AppCompatActivity> { this@MainActivity }
            },
        )
        // Recreate view model to fix the issue of view model not getting data from the service
        unloadKoinModules(viewModelModule)
        loadKoinModules(viewModelModule)
        VersionManager.initialize()
        if (viewModel.recreateActivity.value || viewModel.isServiceRunning) {
            viewModel.activityRecreateDone()
        }
        Logger.d("MainActivity", "onCreate: ")
        val data = (intent?.data ?: intent?.getStringExtra(Intent.EXTRA_TEXT)?.toUri())?.toKmpUriOrNull()
        if (data != null) {
            viewModel.setIntent(
                GenericIntent(
                    action = intent.action,
                    data = data,
                    type = intent.type,
                ),
            )
        }
        Logger.d("Italy", "Key: ${Locale.ITALY.toLanguageTag()}")

        enableEdgeToEdge(
            navigationBarStyle =
                SystemBarStyle.dark(
                    scrim = Color.Transparent.toArgb(),
                ),
            statusBarStyle =
                SystemBarStyle.dark(
                    scrim = Color.Transparent.toArgb(),
                ),
        )
        // Draw the first Compose frame before disk reads, first-run migrations, and update checks.
        // DataStore's first read can be especially slow on a fresh install and must not hold up
        // Activity creation on the UI thread.
        setContent {
            App(viewModel)
        }

        // Observe preference changes and lifecycle restarts without updating the
        // window on animation frames or recomposing the app shell.
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.getHideStatusBar().collect { applyStatusBarPreference() }
            }
        }

        viewModel.checkIsRestoring()

        lifecycleScope.launch {
            // Give Compose a frame before first-run preference migrations open DataStore.
            delay(80)
            // Check if the locale migration has already been done. All preference access here is
            // suspending so a cold DataStore open cannot stall the first frame.
            if (dataStoreManager.getString(FIRST_TIME_MIGRATION).first() != STATUS_DONE) {
                dataStoreManager.putString("location", "US")
                dataStoreManager.putString(FIRST_TIME_MIGRATION, STATUS_DONE)
            }

            val storedLanguage = dataStoreManager.getString(SELECTED_LANGUAGE).first()
            // Android owns the per-app locale, including choices made in system Settings.
            // Keep an empty selection for "System default" instead of pinning today's system
            // language. Feed service requests the effective UI language separately.
            val selectedLanguage = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            if (selectedLanguage != storedLanguage) {
                dataStoreManager.putString(SELECTED_LANGUAGE, selectedLanguage)
            }
            dataStoreManager.putString("effective_language", resources.configuration.locales[0].toLanguageTag())

            // VN was the app's previous built-in default. Upgrade that default once while keeping
            // later explicit country choices unchanged.
            if (dataStoreManager.getString("ContentCountryDefaultUsMigration").first() != STATUS_DONE) {
                if (dataStoreManager.getString("location").first() == "VN") {
                    dataStoreManager.putString("location", "US")
                }
                dataStoreManager.putString("ContentCountryDefaultUsMigration", STATUS_DONE)
            }

            viewModel.checkForUpdateIfEnabled()

            // Let the first frame settle before showing the platform permission prompt on a fresh
            // install. Resolve the localized string asynchronously instead of runBlocking.
            delay(300)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                !EasyPermissions.hasPermissions(this@MainActivity, Manifest.permission.POST_NOTIFICATIONS)
            ) {
                val doNotAsk = dataStoreManager.getString("notification_permission_do_not_ask").first()
                if (doNotAsk != "true") {
                    val wasAsked = dataStoreManager.getString("notification_permission_asked").first()
                    if (wasAsked != "true") {
                        EasyPermissions.requestPermissions(
                            this@MainActivity,
                            ComposeResUtils.getResString(ComposeResUtils.StringType.NOTIFICATION_REQUEST),
                            1,
                            Manifest.permission.POST_NOTIFICATIONS,
                        )
                        dataStoreManager.putString("notification_permission_asked", "true")
                    } else {
                        viewModel.showNotificationPermissionDialog()
                    }
                }
            }
        }

    }

    override fun onDestroy() {
        val shouldStopMusicService = viewModel.shouldStopMusicService()
        Logger.w("MainActivity", "onDestroy: Should stop service $shouldStopMusicService")

        // Always unbind service if it was bound to prevent MusicBinder leak
        if (shouldStopMusicService && shouldUnbind && isFinishing) {
            viewModel.isServiceRunning = false
        }
        unloadKoinModules(viewModelModule)
        super.onDestroy()
        Logger.d("MainActivity", "onDestroy: ")
    }

    override fun onRestart() {
        super.onRestart()
        viewModel.activityRecreate()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus && lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
            applyStatusBarPreference()
        }
    }

    private fun applyStatusBarPreference() {
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        if (viewModel.getHideStatusBar().value == DataStoreManager.TRUE || viewModel.isFullScreen) {
            controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.statusBars())
        } else {
            controller.show(WindowInsetsCompat.Type.statusBars())
        }
    }

    private fun startMusicService() {
//        mediaPlayerHandler.startMediaService(this, serviceConnection)
        com.petedev.wavemusic.media3.di
            .startService(this@MainActivity, serviceConnection)
        mediaPlayerHandler.pushPlayerError = { it ->
            pushPlayerError(it)
        }
        mediaPlayerHandler.showToast = { type ->
            viewModel.makeToast(
                when (type) {
                    is ToastType.ExplicitContent -> {
                        runBlocking { ComposeResUtils.getResString(ComposeResUtils.StringType.EXPLICIT_CONTENT_BLOCKED) }
                    }

                    is ToastType.PlayerError -> {
                        runBlocking { ComposeResUtils.getResString(ComposeResUtils.StringType.TIME_OUT_ERROR, type.error) }
                    }

                    is ToastType.SponsorBlockSkip -> {
                        runBlocking { ComposeResUtils.getResString(ComposeResUtils.StringType.SPONSOR_BLOCK_SKIP, type.category) }
                    }
                },
            )
        }
        viewModel.isServiceRunning = true
        shouldUnbind = true
        Logger.d("Service", "Service started")
    }

    // AppCompatActivity forwards handled configuration changes to Compose. Rotation updates
    // window constraints and insets in place without rebuilding the player or reconnecting.
}
