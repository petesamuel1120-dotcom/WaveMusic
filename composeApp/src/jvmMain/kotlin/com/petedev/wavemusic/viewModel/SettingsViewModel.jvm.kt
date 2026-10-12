package com.petedev.wavemusic.viewModel

import com.eygraber.uri.Uri
import com.petedev.wavemusic.common.DB_NAME
import com.petedev.wavemusic.common.SETTINGS_FILENAME
import com.petedev.wavemusic.data.io.getHomeFolderPath
import com.petedev.wavemusic.domain.repository.CacheRepository
import com.petedev.wavemusic.domain.repository.CommonRepository
import com.petedev.wavemusic.logger.Logger
import com.petedev.wavemusic.extension.zipOutputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import multiplatform.network.cmptoast.ToastGravity
import multiplatform.network.cmptoast.showToast
import org.jetbrains.compose.resources.getString
import com.petedev.wavemusic.resources.Res
import com.petedev.wavemusic.resources.restore_success
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.system.exitProcess

actual suspend fun calculateDataFraction(cacheRepository: CacheRepository): SettingsStorageSectionFraction? = null

actual suspend fun restoreNative(
    commonRepository: CommonRepository,
    uri: Uri,
    getData: () -> Unit,
) {
    ZipInputStream(
        FileInputStream(File(uri.toString())),
    ).use { inputStream ->
        var entry =
            try {
                inputStream.nextEntry
            } catch (e: Exception) {
                null
            }
        while (entry != null) {
            Logger.d("BackupRestore", "Processing entry: ${entry.name}")
            when {
                entry.name == "$SETTINGS_FILENAME.preferences_pb" -> {
                    File(getHomeFolderPath(listOf(".simpmusic")), "$SETTINGS_FILENAME.preferences_pb")
                        .outputStream()
                        .use { outputStream ->
                            inputStream.copyTo(outputStream)
                        }
                }

                entry.name == DB_NAME -> {
                    runBlocking(Dispatchers.IO) {
                        commonRepository.databaseDaoCheckpoint()
                        commonRepository.closeDatabase()
                    }
                    FileOutputStream(commonRepository.getDatabasePath()).use { outputStream ->
                        inputStream.copyTo(outputStream)
                    }
                }
            }
            entry = inputStream.nextEntry
        }
        withContext(Dispatchers.Main) {
            showToast(getString(Res.string.restore_success), ToastGravity.Bottom)
            showToast("App will restart to apply changes", ToastGravity.Bottom)
            delay(2000)
            exitProcess(0)
        }
    }
}

actual suspend fun backupNative(
    commonRepository: CommonRepository,
    uri: Uri,
    backupDownloaded: Boolean,
) {
    FileOutputStream(File(uri.toString())).use {
        it.buffered().zipOutputStream().use { outputStream ->
            File(getHomeFolderPath(listOf(".simpmusic")), "$SETTINGS_FILENAME.preferences_pb")
                .inputStream()
                .buffered()
                .use { inputStream ->
                    outputStream.putNextEntry(ZipEntry("$SETTINGS_FILENAME.preferences_pb"))
                    inputStream.copyTo(outputStream)
                }
            runBlocking(Dispatchers.IO) {
                commonRepository.databaseDaoCheckpoint()
            }
            FileInputStream(commonRepository.getDatabasePath()).use { inputStream ->
                outputStream.putNextEntry(ZipEntry(DB_NAME))
                inputStream.copyTo(outputStream)
            }
        }
    }
}

actual fun getPackageName(): String = ""

actual fun getFileDir(): String = ""

private val desktopSystemLocale = Locale.getDefault()

actual fun changeLanguageNative(code: String) {
    Locale.setDefault(
        if (code.isBlank()) desktopSystemLocale else Locale.forLanguageTag(
            if (code == "id-ID") {
                "in-ID"
            } else {
                code
            },
        ),
    )
}
