package com.maxrave.data.dataStore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.maxrave.common.SELECTED_LANGUAGE
import com.maxrave.common.SUPPORTED_LANGUAGE
import com.maxrave.common.SponsorBlockType
import com.maxrave.domain.data.model.network.ProxyConfiguration
import com.maxrave.domain.data.player.ReverbPreset
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.LiquidGlassIntensity
import com.maxrave.domain.manager.DataStoreManager.Values.FALSE
import com.maxrave.domain.manager.DataStoreManager.Values.GITHUB
import com.maxrave.domain.manager.DataStoreManager.Values.LOCAL_PLAYLIST_FILTER_OLDER_FIRST
import com.maxrave.domain.manager.DataStoreManager.Values.PROXY_TYPE_HTTP
import com.maxrave.domain.manager.DataStoreManager.Values.PROXY_TYPE_SOCKS
import com.maxrave.domain.manager.DataStoreManager.Values.REPEAT_ALL
import com.maxrave.domain.manager.DataStoreManager.Values.REPEAT_MODE_OFF
import com.maxrave.domain.manager.DataStoreManager.Values.REPEAT_ONE
import com.maxrave.domain.manager.DataStoreManager.Values.SIMPMUSIC
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.logger.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import com.maxrave.common.QUALITY as COMMON_QUALITY

internal class DataStoreManagerImpl(
    private val settingsDataStore: DataStore<Preferences>,
) : DataStoreManager {
    // DataStore publishes a complete snapshot after every edit. Each setting should notify its
    // observers only when that setting changes, rather than replaying player/theme setup after
    // unrelated history, cache, account, or resume-point writes.
    private fun <T> preferenceFlow(read: suspend (Preferences) -> T): Flow<T> =
        settingsDataStore.data.map(read).distinctUntilChanged()

    override val appVersion: Flow<String> =
        preferenceFlow { preferences ->
            preferences[APP_VERSION] ?: ""
        }

    override suspend fun setAppVersion(version: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[APP_VERSION] = version
            }
        }
    }

    override val openAppTime: Flow<Int> =
        preferenceFlow { preferences ->
            preferences[OPEN_APP_TIME] ?: 0
        }

    override suspend fun openApp() {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[OPEN_APP_TIME] = openAppTime.first() + 1
            }
        }
    }

    override suspend fun resetOpenAppTime() {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[OPEN_APP_TIME] = 0
            }
        }
    }

    override suspend fun doneOpenAppTime() {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[OPEN_APP_TIME] = 31
            }
        }
    }

    override val location: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LOCATION] ?: "US"
        }

    override suspend fun setLocation(location: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LOCATION] = location
            }
        }
    }

    override val moodAndGenresCache: Flow<String?> =
        preferenceFlow { preferences ->
            preferences[MOOD_AND_GENRES_CACHE]
        }

    override suspend fun setMoodAndGenresCache(json: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[MOOD_AND_GENRES_CACHE] = json
            }
        }
    }

    override val moodArtworkCache: Flow<String?> =
        preferenceFlow { preferences ->
            preferences[MOOD_ARTWORK_CACHE]
        }

    override suspend fun setMoodArtworkCache(json: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[MOOD_ARTWORK_CACHE] = json
            }
        }
    }

    override val quality: Flow<String> =
        preferenceFlow { preferences ->
            preferences[QUALITY] ?: COMMON_QUALITY.items[3].toString()
        }

    override suspend fun setQuality(quality: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[QUALITY] = quality
            }
        }
    }

    override val downloadQuality: Flow<String> =
        preferenceFlow { preferences ->
            preferences[DOWNLOAD_QUALITY] ?: COMMON_QUALITY.items[3].toString()
        }

    override suspend fun setDownloadQuality(quality: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DOWNLOAD_QUALITY] = quality
            }
        }
    }

    override val videoDownloadQuality: Flow<String> =
        preferenceFlow { preferences ->
            preferences[VIDEO_DOWNLOAD_QUALITY] ?: "720p"
        }

    override suspend fun setVideoDownloadQuality(quality: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[VIDEO_DOWNLOAD_QUALITY] = quality
            }
        }
    }

    override val language: Flow<String> =
        preferenceFlow { preferences ->
            preferences[stringPreferencesKey(SELECTED_LANGUAGE)].orEmpty().ifBlank {
                preferences[stringPreferencesKey("effective_language")]
                    ?: com.maxrave.kotlinytmusicscraper.getLanguage()
            }
        }

    override fun getString(key: String): Flow<String?> =
        preferenceFlow { preferences ->
            preferences[stringPreferencesKey(key)]
        }

    override suspend fun putString(
        key: String,
        value: String,
    ) {
        settingsDataStore.edit { settings ->
            settings[stringPreferencesKey(key)] = value
        }
    }

    override val loggedIn: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LOGGED_IN] ?: FALSE
        }

    override val cookie: Flow<String> =
        preferenceFlow { preferences ->
            preferences[COOKIE] ?: ""
        }

    override val pageId: Flow<String> =
        preferenceFlow { preferences ->
            preferences[PAGE_ID] ?: ""
        }

    override val authUser: Flow<Int> =
        preferenceFlow { preferences ->
            preferences[AUTH_USER] ?: 0
        }

    override suspend fun setCookie(
        cookie: String,
        pageId: String?,
        authUser: Int,
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[COOKIE] = cookie
                settings[PAGE_ID] = pageId ?: ""
                settings[AUTH_USER] = authUser
            }
        }
    }

    override suspend fun setLoggedIn(logged: Boolean) {
        withContext(Dispatchers.IO) {
            if (logged) {
                settingsDataStore.edit { settings ->
                    settings[LOGGED_IN] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[LOGGED_IN] = FALSE
                }
            }
        }
    }

    override val normalizeVolume: Flow<String> =
        preferenceFlow { preferences ->
            preferences[NORMALIZE_VOLUME] ?: FALSE
        }

    override suspend fun setNormalizeVolume(normalize: Boolean) {
        withContext(Dispatchers.IO) {
            if (normalize) {
                settingsDataStore.edit { settings ->
                    settings[NORMALIZE_VOLUME] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[NORMALIZE_VOLUME] = FALSE
                }
            }
        }
    }

    override val skipSilent: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SKIP_SILENT] ?: FALSE
        }

    override suspend fun setSkipSilent(skip: Boolean) {
        withContext(Dispatchers.IO) {
            if (skip) {
                settingsDataStore.edit { settings ->
                    settings[SKIP_SILENT] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SKIP_SILENT] = FALSE
                }
            }
        }
    }

    override val saveStateOfPlayback: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SAVE_STATE_OF_PLAYBACK] ?: FALSE
        }

    override suspend fun setSaveStateOfPlayback(save: Boolean) {
        withContext(Dispatchers.IO) {
            if (save) {
                settingsDataStore.edit { settings ->
                    settings[SAVE_STATE_OF_PLAYBACK] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SAVE_STATE_OF_PLAYBACK] = FALSE
                }
            }
        }
    }

    override val shuffleKey: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SHUFFLE_KEY] ?: FALSE
        }
    override val repeatKey: Flow<String> =
        preferenceFlow { preferences ->
            preferences[REPEAT_KEY] ?: REPEAT_MODE_OFF
        }

    override suspend fun recoverShuffleAndRepeatKey(
        shuffle: Boolean,
        repeat: Int,
    ) {
        withContext(Dispatchers.IO) {
            if (shuffle) {
                settingsDataStore.edit { settings ->
                    settings[SHUFFLE_KEY] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SHUFFLE_KEY] = FALSE
                }
            }
            settingsDataStore.edit { settings ->
                settings[REPEAT_KEY] =
                    when (repeat) {
                        1 -> REPEAT_ONE
                        2 -> REPEAT_ALL
                        0 -> REPEAT_MODE_OFF
                        else -> REPEAT_MODE_OFF
                    }
            }
        }
    }

    override val saveRecentSongAndQueue: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SAVE_RECENT_SONG] ?: FALSE
        }

    override suspend fun setSaveRecentSongAndQueue(save: Boolean) {
        withContext(Dispatchers.IO) {
            if (save) {
                settingsDataStore.edit { settings ->
                    settings[SAVE_RECENT_SONG] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SAVE_RECENT_SONG] = FALSE
                }
            }
        }
    }

    override val recentMediaId =
        preferenceFlow { preferences ->
            preferences[RECENT_SONG_MEDIA_ID_KEY] ?: ""
        }
    override val recentPosition =
        preferenceFlow { preferences ->
            preferences[RECENT_SONG_POSITION_KEY] ?: "0"
        }

    override suspend fun saveRecentSong(
        mediaId: String,
        position: Long,
    ) {
        Logger.w("saveRecentSong", "$mediaId $position")
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[RECENT_SONG_MEDIA_ID_KEY] = mediaId
                settings[RECENT_SONG_POSITION_KEY] = position.toString()
            }
        }
    }

    override val playlistFromSaved =
        preferenceFlow { preferences ->
            preferences[FROM_SAVED_PLAYLIST] ?: ""
        }

    override suspend fun setPlaylistFromSaved(playlist: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[FROM_SAVED_PLAYLIST] = playlist
            }
        }
    }

    override val sendBackToGoogle =
        preferenceFlow { preferences ->
            preferences[SEND_BACK_TO_GOOGLE] ?: TRUE
        }

    override suspend fun setSendBackToGoogle(send: Boolean) {
        withContext(Dispatchers.IO) {
            if (send) {
                settingsDataStore.edit { settings ->
                    settings[SEND_BACK_TO_GOOGLE] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SEND_BACK_TO_GOOGLE] = FALSE
                }
            }
        }
    }

    override val sponsorBlockEnabled =
        preferenceFlow { preferences ->
            preferences[SPONSOR_BLOCK_ENABLED] ?: FALSE
        }

    override suspend fun setSponsorBlockEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            if (enabled) {
                settingsDataStore.edit { settings ->
                    settings[SPONSOR_BLOCK_ENABLED] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SPONSOR_BLOCK_ENABLED] = FALSE
                }
            }
        }
    }

    override suspend fun getSponsorBlockCategories(): ArrayList<String> {
        val list: ArrayList<String> = arrayListOf()
        for (category in SponsorBlockType.toList()) {
            if (getString(category.value).first() == TRUE) list.add(category.value)
        }
        return list
    }

    override suspend fun setSponsorBlockCategories(categories: ArrayList<String>) {
        withContext(Dispatchers.IO) {
            Logger.w("setSponsorBlockCategories", categories.toString())
            // Every category is written in ONE edit, keyed by `value` on both branches.
            //
            // The clearing branch used to key on `category.toString()`. SponsorBlockType is a sealed
            // class of data objects, so that is the object's NAME — "SPONSOR" — while the enabled
            // branch and [getSponsorBlockCategories] both use `value`, "sponsor". Unticking a
            // category therefore wrote FALSE to a key nobody reads and left the real one at TRUE:
            // the choice came back unchanged every time the dialog was reopened, and all nine
            // categories stayed on forever. Confirmed against a real settings store where every one
            // of the nine read TRUE.
            settingsDataStore.edit { settings ->
                SponsorBlockType.toList().forEach { category ->
                    settings[stringPreferencesKey(category.value)] =
                        if (categories.contains(category.value)) TRUE else FALSE
                }
            }
        }
    }

    override val enableTranslateLyric =
        preferenceFlow { preferences ->
            preferences[USE_TRANSLATION_LANGUAGE] ?: FALSE
        }

    override suspend fun setEnableTranslateLyric(enable: Boolean) {
        withContext(Dispatchers.IO) {
            if (enable) {
                settingsDataStore.edit { settings ->
                    settings[USE_TRANSLATION_LANGUAGE] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[USE_TRANSLATION_LANGUAGE] = FALSE
                }
            }
        }
    }

    override val lyricsProvider =
        preferenceFlow { preferences ->
            preferences[LYRICS_PROVIDER] ?: SIMPMUSIC
        }

    override suspend fun setLyricsProvider(provider: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LYRICS_PROVIDER] = provider
            }
        }
    }

    override val prioritizeWordSyncedLyrics =
        preferenceFlow { preferences ->
            preferences[PRIORITIZE_WORD_SYNCED_LYRICS] ?: TRUE
        }

    override suspend fun setPrioritizeWordSyncedLyrics(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PRIORITIZE_WORD_SYNCED_LYRICS] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val translationLanguage =
        preferenceFlow { preferences ->
            val languageValue = language.first()
            preferences[TRANSLATION_LANGUAGE] ?: if (languageValue.length >= 2) {
                languageValue
                    .substring(0..1)
            } else {
                "en"
            }
        }

    override suspend fun setTranslationLanguage(language: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[TRANSLATION_LANGUAGE] = language
            }
        }
    }

    override val maxSongCacheSize =
        preferenceFlow { preferences ->
            preferences[MAX_SONG_CACHE_SIZE] ?: -1
        }

    override suspend fun setMaxSongCacheSize(size: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[MAX_SONG_CACHE_SIZE] = size
            }
        }
    }

    override val watchVideoInsteadOfPlayingAudio =
        preferenceFlow { preferences ->
            preferences[WATCH_VIDEO_INSTEAD_OF_PLAYING_AUDIO] ?: FALSE
        }

    override suspend fun setWatchVideoInsteadOfPlayingAudio(watch: Boolean) {
        withContext(Dispatchers.IO) {
            if (watch) {
                settingsDataStore.edit { settings ->
                    settings[WATCH_VIDEO_INSTEAD_OF_PLAYING_AUDIO] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[WATCH_VIDEO_INSTEAD_OF_PLAYING_AUDIO] = FALSE
                }
            }
        }
    }

    override val radioAudioOnly =
        preferenceFlow { preferences ->
            preferences[RADIO_AUDIO_ONLY] ?: TRUE
        }

    override suspend fun setRadioAudioOnly(audioOnly: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[RADIO_AUDIO_ONLY] = if (audioOnly) TRUE else FALSE
            }
        }
    }

    override val playerVolume: Flow<Float> =
        preferenceFlow { preferences ->
            preferences[PLAYER_VOLUME] ?: 1.0f
        }

    override suspend fun setPlayerVolume(volume: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PLAYER_VOLUME] = volume.coerceIn(0f, 1f)
            }
        }
    }

    override val videoQuality =
        preferenceFlow { preferences ->
            preferences[VIDEO_QUALITY] ?: "720p"
        }

    override suspend fun setVideoQuality(quality: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[VIDEO_QUALITY] = quality
            }
        }
    }

    override val spdc =
        preferenceFlow { preferences ->
            preferences[SPDC] ?: ""
        }

    override suspend fun setSpdc(spdc: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SPDC] = spdc
            }
        }
    }

    override val equalizerEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[EQUALIZER_ENABLED] ?: FALSE
        }

    override suspend fun setEqualizerEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[EQUALIZER_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val equalizerBands: Flow<String> =
        preferenceFlow { preferences ->
            preferences[EQUALIZER_BANDS] ?: ""
        }

    override suspend fun setEqualizerBands(bandsDb: List<Float>) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                // Blank when flat, so "no equalizer" and "an equalizer set to zero" are the same
                // stored state and neither installs a filter chain.
                settings[EQUALIZER_BANDS] =
                    if (bandsDb.all { it == 0f }) "" else bandsDb.joinToString(",")
            }
        }
    }

    override val equalizerPreamp: Flow<Float> =
        preferenceFlow { preferences ->
            preferences[EQUALIZER_PREAMP]?.toFloatOrNull() ?: 0f
        }

    override suspend fun setEqualizerPreamp(preampDb: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[EQUALIZER_PREAMP] = preampDb.toString()
            }
        }
    }

    override val equalizerType =
        preferenceFlow { preferences ->
            preferences[EQUALIZER_TYPE] ?: DataStoreManager.EQUALIZER_TYPE_BUILT_IN
        }

    override suspend fun setEqualizerType(type: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[EQUALIZER_TYPE] = type
            }
        }
    }

    override val equalizerAutoEqProfile: Flow<String> =
        preferenceFlow { preferences ->
            preferences[EQUALIZER_AUTOEQ_PROFILE] ?: ""
        }

    override suspend fun setEqualizerAutoEqProfile(
        label: String,
        bandsDb: List<Float>,
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[EQUALIZER_AUTOEQ_PROFILE] =
                    if (label.isBlank()) "" else label + "\n" + bandsDb.joinToString(",")
            }
        }
    }

    override val delayEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[DELAY_ENABLED] ?: FALSE
        }

    override suspend fun setDelayEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DELAY_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val delayTimeMs: Flow<Int> =
        preferenceFlow { preferences ->
            // Written as text like the preamp is, so a value that cannot be read back — hand
            // edited, or written by a build that stored something else here — falls to the
            // default instead of failing the whole preferences read on a typed key.
            preferences[DELAY_TIME_MS]?.toIntOrNull() ?: 400
        }

    override suspend fun setDelayTimeMs(timeMs: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DELAY_TIME_MS] = timeMs.toString()
            }
        }
    }

    override val delayFeedback: Flow<Float> =
        preferenceFlow { preferences ->
            preferences[DELAY_FEEDBACK]?.toFloatOrNull() ?: 0.45f
        }

    override suspend fun setDelayFeedback(feedback: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DELAY_FEEDBACK] = feedback.toString()
            }
        }
    }

    override val delayMix: Flow<Float> =
        preferenceFlow { preferences ->
            preferences[DELAY_MIX]?.toFloatOrNull() ?: 0.3f
        }

    override suspend fun setDelayMix(mix: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DELAY_MIX] = mix.toString()
            }
        }
    }

    override val reverbEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[REVERB_ENABLED] ?: FALSE
        }

    override suspend fun setReverbEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[REVERB_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val reverbPreset: Flow<String> =
        preferenceFlow { preferences ->
            // Handed back as the raw name: this layer has no business deciding what an unknown
            // room means, and the readers that build a filter out of it already have a default.
            preferences[REVERB_PRESET] ?: ReverbPreset.HALL.name
        }

    override suspend fun setReverbPreset(preset: ReverbPreset) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[REVERB_PRESET] = preset.name
            }
        }
    }

    override val reverbMix: Flow<Float> =
        preferenceFlow { preferences ->
            preferences[REVERB_MIX]?.toFloatOrNull() ?: 0.35f
        }

    override suspend fun setReverbMix(mix: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[REVERB_MIX] = mix.toString()
            }
        }
    }

    override val syncFollowToYouTube: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SYNC_FOLLOW_TO_YOUTUBE] ?: FALSE
        }

    override suspend fun setSyncFollowToYouTube(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SYNC_FOLLOW_TO_YOUTUBE] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val spotifyLyrics: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_LYRICS] ?: FALSE
        }

    override suspend fun setSpotifyLyrics(spotifyLyrics: Boolean) {
        withContext(Dispatchers.IO) {
            if (spotifyLyrics) {
                settingsDataStore.edit { settings ->
                    settings[SPOTIFY_LYRICS] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SPOTIFY_LYRICS] = FALSE
                }
            }
        }
    }

    override val spotifyCanvas: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_CANVAS] ?: FALSE
        }

    override suspend fun setSpotifyCanvas(spotifyCanvas: Boolean) {
        withContext(Dispatchers.IO) {
            if (spotifyCanvas) {
                settingsDataStore.edit { settings ->
                    settings[SPOTIFY_CANVAS] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SPOTIFY_CANVAS] = FALSE
                }
            }
        }
    }

    override val amAnimatedArtwork: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AM_ANIMATED_ARTWORK] ?: TRUE
        }

    override suspend fun setAMAnimatedArtwork(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AM_ANIMATED_ARTWORK] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val spotifyClientToken: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_CLIENT_TOKEN] ?: ""
        }

    override suspend fun setSpotifyClientToken(token: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SPOTIFY_CLIENT_TOKEN] = token
            }
        }
    }

    override val spotifyClientTokenExpires: Flow<Long> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_CLIENT_TOKEN_EXPIRES] ?: 0
        }

    override suspend fun setSpotifyClientTokenExpires(expires: Long) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SPOTIFY_CLIENT_TOKEN_EXPIRES] = expires
            }
        }
    }

    // Fallback "" (blank) on purpose: credentials are not hard-coded in source — they come
    // only from the remote config. CommonRepositoryImpl pushes a value into YouTube only when
    // non-blank, so an empty cache simply leaves TIDAL disabled until the first fetch.
    override val tidalClientId: Flow<String> =
        preferenceFlow { preferences ->
            preferences[TIDAL_CLIENT_ID] ?: ""
        }

    override suspend fun setTidalClientId(value: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[TIDAL_CLIENT_ID] = value
            }
        }
    }

    override val tidalClientSecret: Flow<String> =
        preferenceFlow { preferences ->
            preferences[TIDAL_CLIENT_SECRET] ?: ""
        }

    override suspend fun setTidalClientSecret(value: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[TIDAL_CLIENT_SECRET] = value
            }
        }
    }

    override val spotifyPersonalToken: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_PERSONAL_TOKEN] ?: ""
        }

    override suspend fun setSpotifyPersonalToken(token: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SPOTIFY_PERSONAL_TOKEN] = token
            }
        }
    }

    override val spotifyPersonalTokenExpires: Flow<Long> =
        preferenceFlow { preferences ->
            preferences[SPOTIFY_PERSONAL_TOKEN_EXPIRES] ?: 0
        }

    override suspend fun setSpotifyPersonalTokenExpires(expires: Long) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SPOTIFY_PERSONAL_TOKEN_EXPIRES] = expires
            }
        }
    }

    override val homeLimit: Flow<Int> =
        preferenceFlow { preferences ->
            preferences[HOME_LIMIT] ?: 5
        }

    override suspend fun setHomeLimit(limit: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[HOME_LIMIT] = limit
            }
        }
    }

    override val chartKey =
        preferenceFlow { preferences ->
            preferences[CHART_KEY] ?: "ZZ"
        }

    override suspend fun setChartKey(key: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CHART_KEY] = key
            }
        }
    }

    override val themeMode =
        preferenceFlow { preferences ->
            preferences[THEME_MODE] ?: DataStoreManager.THEME_MODE_SYSTEM
        }

    override suspend fun setThemeMode(mode: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[THEME_MODE] = mode
            }
        }
    }

    override val pageTransitionStyle =
        preferenceFlow { preferences ->
            preferences[PAGE_TRANSITION_STYLE] ?: DataStoreManager.PAGE_TRANSITION_SLIDE_V2
        }

    override suspend fun setPageTransitionStyle(style: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PAGE_TRANSITION_STYLE] = style
            }
        }
    }

    override val advancedDialogAnimations: Flow<String> =
        preferenceFlow { preferences ->
            preferences[ADVANCED_DIALOG_ANIMATIONS] ?: FALSE
        }

    override suspend fun setAdvancedDialogAnimations(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[ADVANCED_DIALOG_ANIMATIONS] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val reduceVisualEffects: Flow<String> =
        preferenceFlow { preferences ->
            preferences[REDUCE_VISUAL_EFFECTS] ?: if (prefersReducedVisualEffects()) TRUE else FALSE
        }

    override suspend fun setReduceVisualEffects(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[REDUCE_VISUAL_EFFECTS] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val slimNavigationBar: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SLIM_NAVIGATION_BAR] ?: FALSE
        }

    override suspend fun setSlimNavigationBar(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[SLIM_NAVIGATION_BAR] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val appBackgroundPreset: Flow<String> =
        preferenceFlow { preferences ->
            preferences[APP_BACKGROUND_PRESET]
                ?.takeIf { it in DataStoreManager.APP_BACKGROUND_PRESETS }
                ?: if (preferences[AMOLED_BACKGROUND] == TRUE) {
                    DataStoreManager.APP_BACKGROUND_AMOLED_BLACK
                } else {
                    DataStoreManager.APP_BACKGROUND_AURORA
                }
        }

    override suspend fun setAppBackgroundPreset(preset: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[APP_BACKGROUND_PRESET] =
                    preset.takeIf { it in DataStoreManager.APP_BACKGROUND_PRESETS }
                        ?: DataStoreManager.APP_BACKGROUND_AURORA
            }
        }
    }

    override val themeColorSource =
        preferenceFlow { preferences ->
            // Retired wallpaper selections and older unknown values use the default palette.
            preferences[THEME_COLOR_SOURCE]
                ?.takeIf { it == DataStoreManager.THEME_COLOR_CUSTOM }
                ?: DataStoreManager.THEME_COLOR_DEFAULT
        }

    override suspend fun setThemeColorSource(source: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[THEME_COLOR_SOURCE] =
                    if (source == DataStoreManager.THEME_COLOR_CUSTOM) source else DataStoreManager.THEME_COLOR_DEFAULT
            }
        }
    }

    override val customThemeColor =
        preferenceFlow { preferences ->
            preferences[CUSTOM_THEME_COLOR] ?: DataStoreManager.DEFAULT_THEME_COLOR_HEX
        }

    override suspend fun setCustomThemeColor(argbHex: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CUSTOM_THEME_COLOR] = argbHex
            }
        }
    }

    override val nowPlayingStyle =
        preferenceFlow { preferences ->
            preferences[NOW_PLAYING_STYLE] ?: DataStoreManager.NOW_PLAYING_STYLE_APPLE_MUSIC
        }

    override suspend fun setNowPlayingStyle(style: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[NOW_PLAYING_STYLE] = style
            }
        }
    }

    override val lyricsStyle =
        preferenceFlow { preferences ->
            when (val storedStyle = preferences[LYRICS_STYLE]) {
                null -> DataStoreManager.LYRICS_STYLE_CLASSIC
                DataStoreManager.LYRICS_STYLE_LEGACY_CLASSIC -> DataStoreManager.LYRICS_STYLE_ENHANCED
                else -> storedStyle
            }
        }

    override suspend fun setLyricsStyle(style: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LYRICS_STYLE] = style
            }
        }
    }

    override val lyricsFontSizeTenthsPt =
        preferenceFlow { preferences ->
            preferences[LYRICS_FONT_SIZE_TENTHS_PT] ?: DataStoreManager.LYRICS_FONT_SIZE_DEFAULT_TENTHS_PT
        }

    override suspend fun setLyricsFontSizeTenthsPt(sizeTenthsPt: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LYRICS_FONT_SIZE_TENTHS_PT] = sizeTenthsPt.coerceIn(100, 400)
            }
        }
    }

    override val translatedLyricsFontSizeTenthsPt =
        preferenceFlow { preferences ->
            preferences[TRANSLATED_LYRICS_FONT_SIZE_TENTHS_PT] ?: DataStoreManager.TRANSLATED_LYRICS_FONT_SIZE_DEFAULT_TENTHS_PT
        }

    override suspend fun setTranslatedLyricsFontSizeTenthsPt(sizeTenthsPt: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[TRANSLATED_LYRICS_FONT_SIZE_TENTHS_PT] = sizeTenthsPt.coerceIn(80, 300)
            }
        }
    }

    override val romanizationLanguages =
        preferenceFlow { preferences ->
            preferences[ROMANIZATION_LANGUAGES] ?: ""
        }

    override suspend fun setRomanizationLanguages(languages: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[ROMANIZATION_LANGUAGES] = languages
            }
        }
    }

    override val lyricsOffsetMs =
        preferenceFlow { preferences ->
            preferences[LYRICS_OFFSET_MS] ?: 0
        }

    override suspend fun setLyricsOffsetMs(offsetMs: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LYRICS_OFFSET_MS] = offsetMs
            }
        }
    }

    override suspend fun adjustLyricsOffsetMs(deltaMs: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                val current = (settings[LYRICS_OFFSET_MS] ?: 0).toLong()
                settings[LYRICS_OFFSET_MS] = (current + deltaMs.toLong())
                    .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
                    .toInt()
            }
        }
    }

    private fun songLyricsOffsetKey(videoId: String) = intPreferencesKey("lyrics_offset_song:$videoId")

    override fun getSongLyricsOffsetMs(videoId: String): Flow<Int?> =
        preferenceFlow { settings ->
            if (videoId.isBlank()) null else settings[songLyricsOffsetKey(videoId)]
        }

    override fun getEffectiveLyricsOffsetMs(videoId: String?): Flow<Int> =
        preferenceFlow { settings ->
            val local = videoId?.takeIf { it.isNotBlank() }?.let { settings[songLyricsOffsetKey(it)] }
            local ?: settings[LYRICS_OFFSET_MS] ?: 0
        }

    override suspend fun adjustSongLyricsOffsetMs(videoId: String, deltaMs: Int) {
        if (videoId.isBlank()) return
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                val key = songLyricsOffsetKey(videoId)
                val current = (settings[key] ?: settings[LYRICS_OFFSET_MS] ?: 0).toLong()
                settings[key] = (current + deltaMs.toLong())
                    .coerceIn(Int.MIN_VALUE.toLong(), Int.MAX_VALUE.toLong())
                    .toInt()
            }
        }
    }

    override suspend fun clearSongLyricsOffsetMs(videoId: String) {
        if (videoId.isBlank()) return
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings -> settings.remove(songLyricsOffsetKey(videoId)) }
        }
    }

    override val usingProxy =
        preferenceFlow { preferences ->
            preferences[USING_PROXY] ?: FALSE
        }

    override suspend fun setUsingProxy(usingProxy: Boolean) {
        withContext(Dispatchers.IO) {
            if (usingProxy) {
                settingsDataStore.edit { settings ->
                    settings[USING_PROXY] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[USING_PROXY] = FALSE
                }
            }
        }
    }

    override val proxyType =
        settingsDataStore.data
            .map { preferences ->
                preferences[PROXY_TYPE]
            }.map {
                when (it) {
                    PROXY_TYPE_HTTP -> DataStoreManager.ProxyType.PROXY_TYPE_HTTP
                    PROXY_TYPE_SOCKS -> DataStoreManager.ProxyType.PROXY_TYPE_SOCKS
                    else -> DataStoreManager.ProxyType.PROXY_TYPE_HTTP
                }
            }

    override suspend fun setProxyType(proxyType: DataStoreManager.ProxyType) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PROXY_TYPE] =
                    when (proxyType) {
                        DataStoreManager.ProxyType.PROXY_TYPE_HTTP -> PROXY_TYPE_HTTP
                        DataStoreManager.ProxyType.PROXY_TYPE_SOCKS -> PROXY_TYPE_SOCKS
                    }
            }
        }
    }

    override val proxyHost =
        preferenceFlow { preferences ->
            preferences[PROXY_HOST] ?: ""
        }

    override suspend fun setProxyHost(proxyHost: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PROXY_HOST] = proxyHost
            }
        }
    }

    override val proxyPort =
        preferenceFlow { preferences ->
            preferences[PROXY_PORT] ?: 8000
        }

    override suspend fun setProxyPort(proxyPort: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PROXY_PORT] = proxyPort
            }
        }
    }

    override val proxyUsername =
        preferenceFlow { preferences ->
            preferences[PROXY_USERNAME] ?: ""
        }

    override suspend fun setProxyUsername(proxyUsername: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PROXY_USERNAME] = proxyUsername
            }
        }
    }

    override val proxyPassword =
        preferenceFlow { preferences ->
            preferences[PROXY_PASSWORD] ?: ""
        }

    override suspend fun setProxyPassword(proxyPassword: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[PROXY_PASSWORD] = proxyPassword
            }
        }
    }

    override fun getJVMProxy(): ProxyConfiguration? =
        runBlocking {
            try {
                if (usingProxy.first() == TRUE) {
                    val proxyType = proxyType.first()
                    val proxyHost = proxyHost.first()
                    val proxyPort = proxyPort.first()
                    val proxyUsername = proxyUsername.first()
                    val proxyPassword = proxyPassword.first()
                    return@runBlocking ProxyConfiguration(
                        proxyHost,
                        proxyPort,
                        proxyType,
                        proxyUsername.ifEmpty { null },
                        proxyPassword.ifEmpty { null },
                    )
                } else {
                    return@runBlocking null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                return@runBlocking null
            }
        }

    override val endlessQueue =
        preferenceFlow { preferences ->
            preferences[ENDLESS_QUEUE] ?: FALSE
        }

    override suspend fun setEndlessQueue(endlessQueue: Boolean) {
        withContext(Dispatchers.IO) {
            if (endlessQueue) {
                settingsDataStore.edit { settings ->
                    settings[ENDLESS_QUEUE] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[ENDLESS_QUEUE] = FALSE
                }
            }
        }
    }

    override val keepYouTubePlaylistOffline: Flow<String> =
        preferenceFlow { preferences ->
            preferences[KEEP_YOUTUBE_PLAYLIST_OFFLINE] ?: FALSE
        }

    override suspend fun setKeepYouTubePlaylistOffline(keep: Boolean) {
        withContext(Dispatchers.IO) {
            if (keep) {
                settingsDataStore.edit { settings ->
                    settings[KEEP_YOUTUBE_PLAYLIST_OFFLINE] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[KEEP_YOUTUBE_PLAYLIST_OFFLINE] = FALSE
                }
            }
        }
    }

    override val combineLocalAndYouTubeLiked: Flow<String> =
        preferenceFlow { preferences ->
            preferences[COMBINE_LOCAL_AND_YOUTUBE_LIKED] ?: FALSE
        }

    override suspend fun setCombineLocalAndYouTubeLiked(combine: Boolean) {
        withContext(Dispatchers.IO) {
            if (combine) {
                settingsDataStore.edit { settings ->
                    settings[COMBINE_LOCAL_AND_YOUTUBE_LIKED] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[COMBINE_LOCAL_AND_YOUTUBE_LIKED] = FALSE
                }
            }
        }
    }

    override val shouldShowLogInRequiredAlert =
        preferenceFlow { preferences ->
            preferences[SHOULD_SHOW_LOG_IN_REQUIRED_ALERT] ?: TRUE
        }

    override suspend fun setShouldShowLogInRequiredAlert(shouldShow: Boolean) {
        withContext(Dispatchers.IO) {
            if (shouldShow) {
                settingsDataStore.edit { settings ->
                    settings[SHOULD_SHOW_LOG_IN_REQUIRED_ALERT] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SHOULD_SHOW_LOG_IN_REQUIRED_ALERT] = FALSE
                }
            }
        }
    }

    override val autoCheckForUpdates =
        preferenceFlow { preferences ->
            preferences[AUTO_CHECK_FOR_UPDATES] ?: TRUE
        }

    override suspend fun setAutoCheckForUpdates(autoCheck: Boolean) {
        withContext(Dispatchers.IO) {
            if (autoCheck) {
                settingsDataStore.edit { settings ->
                    settings[AUTO_CHECK_FOR_UPDATES] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[AUTO_CHECK_FOR_UPDATES] = FALSE
                }
            }
        }
    }

    override val updateChannel =
        preferenceFlow { preferences ->
            preferences[UPDATE_CHANNEL] ?: GITHUB
        }

    override suspend fun setUpdateChannel(channel: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[UPDATE_CHANNEL] = channel
            }
        }
    }

    override val playbackSpeed =
        preferenceFlow { preferences ->
            preferences[PLAYBACK_SPEED] ?: 1.0f
        }

    override fun setPlaybackSpeed(speed: Float) {
        runBlocking {
            settingsDataStore.edit { settings ->
                settings[PLAYBACK_SPEED] = speed
            }
        }
    }

    override val pitch =
        preferenceFlow { preferences ->
            preferences[PITCH] ?: 0
        }

    override fun setPitch(pitch: Int) {
        runBlocking {
            settingsDataStore.edit { settings ->
                settings[PITCH] = pitch
            }
        }
    }

    override val dataSyncId =
        preferenceFlow { preferences ->
            preferences[DATA_SYNC_ID] ?: ""
        }

    override suspend fun setDataSyncId(dataSyncId: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DATA_SYNC_ID] = dataSyncId
            }
        }
    }

    override val visitorData =
        preferenceFlow { preferences ->
            preferences[VISITOR_DATA] ?: ""
        }

    override suspend fun setVisitorData(visitorData: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[VISITOR_DATA] = visitorData
            }
        }
    }

    override suspend fun setAIProvider(provider: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[stringPreferencesKey("ai_provider")] = provider
            }
        }
    }

    override val aiProvider: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AI_PROVIDER] ?: DataStoreManager.AI_PROVIDER_GROQ
        }

    override suspend fun setAIApiKey(apiKey: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AI_API_KEY] = apiKey
            }
        }
    }

    override val aiApiKey: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AI_API_KEY] ?: ""
        }

    override val openRouterApiKey: Flow<String> =
        preferenceFlow { preferences ->
            preferences[OPENROUTER_API_KEY] ?: ""
        }

    override suspend fun setOpenRouterApiKey(apiKey: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[OPENROUTER_API_KEY] = apiKey
            }
        }
    }

    override val showLyricsTranslationButton: Flow<String> =
        preferenceFlow { preferences ->
            preferences[SHOW_LYRICS_TRANSLATION_BUTTON] ?: TRUE
        }

    override fun getSongLyricsTranslationEnabled(videoId: String): Flow<Boolean> =
        preferenceFlow { preferences ->
            videoId.isNotBlank() && preferences[booleanPreferencesKey("lyrics_translation_enabled_song:$videoId")] == true
        }

    override suspend fun setSongLyricsTranslationEnabled(videoId: String, enabled: Boolean) {
        if (videoId.isBlank()) return
        settingsDataStore.edit { preferences ->
            preferences[booleanPreferencesKey("lyrics_translation_enabled_song:$videoId")] = enabled
        }
    }

    override suspend fun setShowLyricsTranslationButton(use: Boolean) {
        withContext(Dispatchers.IO) {
            if (use) {
                settingsDataStore.edit { settings ->
                    settings[SHOW_LYRICS_TRANSLATION_BUTTON] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[SHOW_LYRICS_TRANSLATION_BUTTON] = FALSE
                }
            }
        }
    }

    override val customModelId =
        preferenceFlow { preferences ->
            preferences[CUSTOM_MODEL_ID] ?: ""
        }

    override suspend fun setCustomModelId(modelId: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CUSTOM_MODEL_ID] = modelId
            }
        }
    }

    override val openRouterModelId: Flow<String> =
        preferenceFlow { preferences ->
            preferences[OPENROUTER_MODEL_ID] ?: ""
        }

    override suspend fun setOpenRouterModelId(modelId: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[OPENROUTER_MODEL_ID] = modelId
            }
        }
    }

    override val customOpenAIBaseUrl: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CUSTOM_OPENAI_BASE_URL] ?: ""
        }

    override suspend fun setCustomOpenAIBaseUrl(baseUrl: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CUSTOM_OPENAI_BASE_URL] = baseUrl
            }
        }
    }

    override val customOpenAIHeaders: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CUSTOM_OPENAI_HEADERS] ?: ""
        }

    override suspend fun setCustomOpenAIHeaders(headers: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CUSTOM_OPENAI_HEADERS] = headers
            }
        }
    }

    override val localPlaylistFilter: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LOCAL_PLAYLIST_FILTER] ?: LOCAL_PLAYLIST_FILTER_OLDER_FIRST
        }

    override suspend fun setLocalPlaylistFilter(filter: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LOCAL_PLAYLIST_FILTER] = filter
            }
        }
    }

    override val killServiceOnExit: Flow<String> =
        preferenceFlow { preferences ->
            preferences[KILL_SERVICE_ON_EXIT] ?: FALSE
        }

    override suspend fun setKillServiceOnExit(kill: Boolean) {
        withContext(Dispatchers.IO) {
            if (kill) {
                settingsDataStore.edit { settings ->
                    settings[KILL_SERVICE_ON_EXIT] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[KILL_SERVICE_ON_EXIT] = FALSE
                }
            }
        }
    }

    override val crossfadeEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CROSSFADE_ENABLED] ?: TRUE
        }

    override suspend fun setCrossfadeEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            if (enabled) {
                settingsDataStore.edit { settings ->
                    settings[CROSSFADE_ENABLED] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[CROSSFADE_ENABLED] = FALSE
                }
            }
        }
    }

    override val crossfadeDuration: Flow<Int> =
        preferenceFlow { preferences ->
            preferences[CROSSFADE_DURATION] ?: DataStoreManager.CROSSFADE_DEFAULT_DURATION_MS
        }

    override suspend fun setCrossfadeDuration(duration: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CROSSFADE_DURATION] = duration
            }
        }
    }

    override val crossfadeDjMode: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CROSSFADE_DJ_MODE] ?: TRUE
        }

    override suspend fun setCrossfadeDjMode(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CROSSFADE_DJ_MODE] = if (enabled) TRUE else FALSE
            }
        }
    }

    // Defaults to FALSE: anyone already running crossfade would otherwise find it silently absent
    // on albums after updating.
    override val crossfadeSkipAlbum: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CROSSFADE_SKIP_ALBUM] ?: FALSE
        }

    override suspend fun setCrossfadeSkipAlbum(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[CROSSFADE_SKIP_ALBUM] = if (enabled) TRUE else FALSE
            }
        }
    }

    // Defaults to FALSE: it spends storage and mobile data on the user's behalf.
    override val autoDownloadLikedSongs: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AUTO_DOWNLOAD_LIKED_SONGS] ?: FALSE
        }

    override suspend fun setAutoDownloadLikedSongs(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AUTO_DOWNLOAD_LIKED_SONGS] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val helpBuildLyricsDatabase: Flow<String> =
        preferenceFlow { preferences ->
            preferences[HELP_BUILD_LYRICS_DATABASE] ?: FALSE
        }

    override suspend fun setHelpBuildLyricsDatabase(help: Boolean) {
        withContext(Dispatchers.IO) {
            if (help) {
                settingsDataStore.edit { settings ->
                    settings[HELP_BUILD_LYRICS_DATABASE] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[HELP_BUILD_LYRICS_DATABASE] = FALSE
                }
            }
        }
    }

    override val contributorName: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CONTRIBUTOR_NAME] ?: ""
        }

    override val contributorEmail: Flow<String> =
        preferenceFlow { preferences ->
            preferences[CONTRIBUTOR_EMAIL] ?: ""
        }

    override suspend fun setContributorLyricsDatabase(
        contributor: Pair<String, String>?, // contributor name and email, null if anonymous
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                if (contributor == null) {
                    settings[CONTRIBUTOR_NAME] = ""
                    settings[CONTRIBUTOR_EMAIL] = ""
                } else {
                    settings[CONTRIBUTOR_NAME] = contributor.first
                    settings[CONTRIBUTOR_EMAIL] = contributor.second
                }
            }
        }
    }

    override val backupDownloaded: Flow<String> =
        preferenceFlow { preferences ->
            preferences[BACKUP_DOWNLOADED] ?: FALSE
        }

    override suspend fun setBackupDownloaded(backupDownloaded: Boolean) {
        withContext(Dispatchers.IO) {
            if (backupDownloaded) {
                settingsDataStore.edit { settings ->
                    settings[BACKUP_DOWNLOADED] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[BACKUP_DOWNLOADED] = FALSE
                }
            }
        }
    }

    override val liquidGlassIntensity: Flow<Float>
        get() = preferenceFlow { preferences ->
            LiquidGlassIntensity.normalize(preferences[LIQUID_GLASS_INTENSITY] ?: LiquidGlassIntensity.Default)
        }

    override suspend fun setLiquidGlassIntensity(intensity: Float) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[LIQUID_GLASS_INTENSITY] = LiquidGlassIntensity.normalize(intensity)
            }
        }
    }

    override val persistentLiquidGlassHeader: Flow<String>
        get() = persistentHeaderButtons

    override suspend fun setPersistentLiquidGlassHeader(enabled: Boolean) {
        setPersistentHeaderButtons(enabled)
    }

    override val persistentHeaderButtons: Flow<String>
        get() = preferenceFlow { preferences ->
            preferences[PERSISTENT_HEADER_BUTTONS] ?: TRUE
        }

    override suspend fun setPersistentHeaderButtons(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { preferences ->
                preferences[PERSISTENT_HEADER_BUTTONS] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val enableLiquidGlass: Flow<String>
        get() =
            preferenceFlow { preferences ->
                preferences[LIQUID_GLASS] ?: TRUE
            }

    override suspend fun setEnableLiquidGlass(enable: Boolean) {
        withContext(Dispatchers.IO) {
            if (enable) {
                settingsDataStore.edit { settings ->
                    settings[LIQUID_GLASS] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[LIQUID_GLASS] = FALSE
                }
            }
        }
    }

    override val explicitContentEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[EXPLICIT_CONTENT_ENABLED] ?: TRUE
        }

    override suspend fun setExplicitContentEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            if (enabled) {
                settingsDataStore.edit { settings ->
                    settings[EXPLICIT_CONTENT_ENABLED] = TRUE
                }
            } else {
                settingsDataStore.edit { settings ->
                    settings[EXPLICIT_CONTENT_ENABLED] = FALSE
                }
            }
        }
    }

    override val discordToken: Flow<String> =
        preferenceFlow { preferences ->
            preferences[DISCORD_TOKEN] ?: ""
        }

    override suspend fun setDiscordToken(token: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[DISCORD_TOKEN] = token
            }
        }
    }

    override val richPresenceEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[RICH_PRESENCE] ?: FALSE
        }

    override suspend fun setRichPresenceEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[RICH_PRESENCE] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val lastfmSessionKey: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LASTFM_SESSION_KEY] ?: ""
        }

    override val lastfmUsername: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LASTFM_USERNAME] ?: ""
        }

    /**
     * Key and username are written together, and an empty key clears both.
     *
     * They only mean anything as a pair: a username with no key cannot scrobble, and a key with no
     * username leaves settings unable to say whose account is connected. One edit also means
     * logging out cannot leave half the pair behind if the process dies mid-way.
     */
    override suspend fun setLastfmSession(
        sessionKey: String,
        username: String,
    ) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LASTFM_SESSION_KEY] = sessionKey
                settings[LASTFM_USERNAME] = if (sessionKey.isEmpty()) "" else username
            }
        }
    }

    override val lastfmScrobbleEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LASTFM_SCROBBLE_ENABLED] ?: TRUE
        }

    override suspend fun setLastfmScrobbleEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LASTFM_SCROBBLE_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val localTrackingEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[LOCAL_TRACKING_ENABLED] ?: TRUE
        }

    override suspend fun setLocalTrackingEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[LOCAL_TRACKING_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val blogNotificationEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[BLOG_NOTIFICATION_ENABLED] ?: TRUE
        }

    override suspend fun setBlogNotificationEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[BLOG_NOTIFICATION_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    // Auto Backup
    override val autoBackupEnabled: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AUTO_BACKUP_ENABLED] ?: FALSE
        }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AUTO_BACKUP_ENABLED] = if (enabled) TRUE else FALSE
            }
        }
    }

    override val autoBackupFrequency: Flow<String> =
        preferenceFlow { preferences ->
            preferences[AUTO_BACKUP_FREQUENCY] ?: DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
        }

    override suspend fun setAutoBackupFrequency(frequency: String) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AUTO_BACKUP_FREQUENCY] = frequency
            }
        }
    }

    override val autoBackupMaxFiles: Flow<Int> =
        preferenceFlow { preferences ->
            preferences[AUTO_BACKUP_MAX_FILES] ?: 5
        }

    override suspend fun setAutoBackupMaxFiles(max: Int) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AUTO_BACKUP_MAX_FILES] = max
            }
        }
    }

    override val autoBackupLastTime: Flow<Long> =
        preferenceFlow { preferences ->
            preferences[AUTO_BACKUP_LAST_TIME] ?: 0L
        }

    override suspend fun setAutoBackupLastTime(time: Long) {
        withContext(Dispatchers.IO) {
            settingsDataStore.edit { settings ->
                settings[AUTO_BACKUP_LAST_TIME] = time
            }
        }
    }

    companion object Settings {
        val APP_VERSION = stringPreferencesKey("app_version")
        val COOKIE = stringPreferencesKey("cookie")

        val PAGE_ID = stringPreferencesKey("page_id")
        val AUTH_USER = intPreferencesKey("auth_user")
        val LOGGED_IN = stringPreferencesKey("logged_in")
        val LOCATION = stringPreferencesKey("location")
        val MOOD_AND_GENRES_CACHE = stringPreferencesKey("mood_and_genres_cache")
        val MOOD_ARTWORK_CACHE = stringPreferencesKey("mood_artwork_cache")
        val QUALITY = stringPreferencesKey("quality")
        val DOWNLOAD_QUALITY = stringPreferencesKey("download_quality")
        val VIDEO_DOWNLOAD_QUALITY = stringPreferencesKey("video_download_quality")
        val NORMALIZE_VOLUME = stringPreferencesKey("normalize_volume")
        val SKIP_SILENT = stringPreferencesKey("skip_silent")
        val SAVE_STATE_OF_PLAYBACK = stringPreferencesKey("save_state_of_playback")
        val SAVE_RECENT_SONG = stringPreferencesKey("save_recent_song")
        val RECENT_SONG_MEDIA_ID_KEY = stringPreferencesKey("recent_song_media_id")
        val RECENT_SONG_POSITION_KEY = stringPreferencesKey("recent_song_position")
        val SHUFFLE_KEY = stringPreferencesKey("shuffle_key")
        val REPEAT_KEY = stringPreferencesKey("repeat_key")
        val SEND_BACK_TO_GOOGLE = stringPreferencesKey("send_back_to_google")
        val FROM_SAVED_PLAYLIST = stringPreferencesKey("from_saved_playlist")

        val KILL_SERVICE_ON_EXIT = stringPreferencesKey("kill_service_on_exit")
        val CROSSFADE_ENABLED = stringPreferencesKey("crossfade_enabled")
        val CROSSFADE_DURATION = intPreferencesKey("crossfade_duration")
        val CROSSFADE_DJ_MODE = stringPreferencesKey("crossfade_dj_mode")
        val CROSSFADE_SKIP_ALBUM = stringPreferencesKey("crossfade_skip_album")
        val AUTO_DOWNLOAD_LIKED_SONGS = stringPreferencesKey("auto_download_liked_songs")
        val LYRICS_PROVIDER = stringPreferencesKey("lyrics_provider")
        val PRIORITIZE_WORD_SYNCED_LYRICS = stringPreferencesKey("prioritize_word_synced_lyrics")
        val TRANSLATION_LANGUAGE = stringPreferencesKey("translation_language")
        val USE_TRANSLATION_LANGUAGE = stringPreferencesKey("use_translation_language")

        val SPONSOR_BLOCK_ENABLED = stringPreferencesKey("sponsor_block_enabled")
        val MAX_SONG_CACHE_SIZE = intPreferencesKey("maxSongCacheSize")
        val WATCH_VIDEO_INSTEAD_OF_PLAYING_AUDIO =
            stringPreferencesKey("watch_video_instead_of_playing_audio")
        val RADIO_AUDIO_ONLY = stringPreferencesKey("radio_audio_only")
        val VIDEO_QUALITY = stringPreferencesKey("video_quality")
        val PLAYER_VOLUME = floatPreferencesKey("player_volume")
        val SPDC = stringPreferencesKey("sp_dc")
        val SPOTIFY_LYRICS = stringPreferencesKey("spotify_lyrics")
        val SYNC_FOLLOW_TO_YOUTUBE = stringPreferencesKey("sync_follow_to_youtube")
        val EQUALIZER_AUTOEQ_PROFILE = stringPreferencesKey("equalizer_autoeq_profile")
        val EQUALIZER_BANDS = stringPreferencesKey("equalizer_bands")
        val EQUALIZER_ENABLED = stringPreferencesKey("equalizer_enabled")
        val EQUALIZER_TYPE = stringPreferencesKey("equalizer_type")
        val EQUALIZER_PREAMP = stringPreferencesKey("equalizer_preamp")
        val DELAY_ENABLED = stringPreferencesKey("delay_enabled")
        val DELAY_TIME_MS = stringPreferencesKey("delay_time_ms")
        val DELAY_FEEDBACK = stringPreferencesKey("delay_feedback")
        val DELAY_MIX = stringPreferencesKey("delay_mix")
        val REVERB_ENABLED = stringPreferencesKey("reverb_enabled")
        val REVERB_PRESET = stringPreferencesKey("reverb_preset")
        val REVERB_MIX = stringPreferencesKey("reverb_mix")
        val SPOTIFY_CANVAS = stringPreferencesKey("spotify_canvas")
        val AM_ANIMATED_ARTWORK = stringPreferencesKey("am_animated_artwork")
        val SPOTIFY_CLIENT_TOKEN = stringPreferencesKey("spotify_client_token")
        val SPOTIFY_CLIENT_TOKEN_EXPIRES = longPreferencesKey("spotify_client_token_expires")
        val SPOTIFY_PERSONAL_TOKEN = stringPreferencesKey("spotify_personal_token")
        val SPOTIFY_PERSONAL_TOKEN_EXPIRES = longPreferencesKey("spotify_personal_token_expires")
        val TIDAL_CLIENT_ID = stringPreferencesKey("tidal_client_id")
        val TIDAL_CLIENT_SECRET = stringPreferencesKey("tidal_client_secret")
        val HOME_LIMIT = intPreferencesKey("home_limit")
        val CHART_KEY = stringPreferencesKey("chart_key")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val PAGE_TRANSITION_STYLE = stringPreferencesKey("page_transition_style")
        val ADVANCED_DIALOG_ANIMATIONS = stringPreferencesKey("advanced_dialog_animations")
        val REDUCE_VISUAL_EFFECTS = stringPreferencesKey("reduce_visual_effects")
        val SLIM_NAVIGATION_BAR = stringPreferencesKey("slim_navigation_bar")
        // AMOLED_BACKGROUND is retained only to migrate the old on/off preference.
        val AMOLED_BACKGROUND = stringPreferencesKey("amoled_background")
        val APP_BACKGROUND_PRESET = stringPreferencesKey("app_background_preset")
        val THEME_COLOR_SOURCE = stringPreferencesKey("theme_color_source")
        val CUSTOM_THEME_COLOR = stringPreferencesKey("custom_theme_color")
        val NOW_PLAYING_STYLE = stringPreferencesKey("now_playing_style")
        val LYRICS_STYLE = stringPreferencesKey("lyrics_style")
        val LYRICS_FONT_SIZE_TENTHS_PT = intPreferencesKey("lyrics_font_size_tenths_pt")
        val TRANSLATED_LYRICS_FONT_SIZE_TENTHS_PT = intPreferencesKey("translated_lyrics_font_size_tenths_pt")
        val ROMANIZATION_LANGUAGES = stringPreferencesKey("romanization_languages")
        val LYRICS_OFFSET_MS = intPreferencesKey("lyrics_offset_ms")
        val USING_PROXY = stringPreferencesKey("using_proxy")
        val PROXY_TYPE = stringPreferencesKey("proxy_type")
        val PROXY_HOST = stringPreferencesKey("proxy_host")
        val PROXY_PORT = intPreferencesKey("proxy_port")
        val PROXY_USERNAME = stringPreferencesKey("proxy_username")
        val PROXY_PASSWORD = stringPreferencesKey("proxy_password")
        val ENDLESS_QUEUE = stringPreferencesKey("endless_queue")
        val KEEP_YOUTUBE_PLAYLIST_OFFLINE = stringPreferencesKey("keep_youtube_playlist_offline")
        val COMBINE_LOCAL_AND_YOUTUBE_LIKED = stringPreferencesKey("combine_local_and_youtube_liked")
        val SHOULD_SHOW_LOG_IN_REQUIRED_ALERT = stringPreferencesKey("should_show_log_in_required_alert")
        val AUTO_CHECK_FOR_UPDATES = stringPreferencesKey("auto_check_for_updates")
        val UPDATE_CHANNEL = stringPreferencesKey("update_channel")
        val PLAYBACK_SPEED = floatPreferencesKey("playback_speed")
        val PITCH = intPreferencesKey("pitch")
        val OPEN_APP_TIME = intPreferencesKey("open_app_time")
        val DATA_SYNC_ID = stringPreferencesKey("data_sync_id")
        val VISITOR_DATA = stringPreferencesKey("visitor_data")
        val AI_PROVIDER = stringPreferencesKey("ai_provider")
        val AI_API_KEY = stringPreferencesKey("ai_gemini_api_key")
        val OPENROUTER_API_KEY = stringPreferencesKey("openrouter_api_key")

        val CUSTOM_MODEL_ID = stringPreferencesKey("custom_model_id")
        val OPENROUTER_MODEL_ID = stringPreferencesKey("openrouter_model_id")
        val CUSTOM_OPENAI_BASE_URL = stringPreferencesKey("custom_openai_base_url")
        val CUSTOM_OPENAI_HEADERS = stringPreferencesKey("custom_openai_headers")

        // Preserve existing checkbox choices when repurposing the preference for player controls.
        val SHOW_LYRICS_TRANSLATION_BUTTON = stringPreferencesKey("use_ai_translation")

        val LOCAL_PLAYLIST_FILTER = stringPreferencesKey("local_playlist_filter")
        val HELP_BUILD_LYRICS_DATABASE = stringPreferencesKey("help_build_lyrics_database")
        val CONTRIBUTOR_NAME = stringPreferencesKey("contributor_name")
        val CONTRIBUTOR_EMAIL = stringPreferencesKey("contributor_email")

        val BACKUP_DOWNLOADED = stringPreferencesKey("backup_downloaded")

        val LIQUID_GLASS = stringPreferencesKey("liquid_glass")
        val LIQUID_GLASS_INTENSITY = floatPreferencesKey("liquid_glass_intensity")
        // Reuse the saved controls choice for the combined Fixed header option.
        val PERSISTENT_HEADER_BUTTONS = stringPreferencesKey("persistent_header_buttons")

        val EXPLICIT_CONTENT_ENABLED = stringPreferencesKey("explicit_content_enabled")

        val DISCORD_TOKEN = stringPreferencesKey("discord_token")
        val RICH_PRESENCE = stringPreferencesKey("rich_presence")

        val LASTFM_SESSION_KEY = stringPreferencesKey("lastfm_session_key")
        val LASTFM_USERNAME = stringPreferencesKey("lastfm_username")
        val LASTFM_SCROBBLE_ENABLED = stringPreferencesKey("lastfm_scrobble_enabled")

        val LOCAL_TRACKING_ENABLED = stringPreferencesKey("local_tracking_enabled")

        val BLOG_NOTIFICATION_ENABLED = stringPreferencesKey("blog_notification_enabled")

        // Auto Backup
        val AUTO_BACKUP_ENABLED = stringPreferencesKey("auto_backup_enabled")
        val AUTO_BACKUP_FREQUENCY = stringPreferencesKey("auto_backup_frequency")
        val AUTO_BACKUP_MAX_FILES = intPreferencesKey("auto_backup_max_files")
        val AUTO_BACKUP_LAST_TIME = longPreferencesKey("auto_backup_last_time")
    }
}

expect fun createDataStoreInstance(): DataStore<Preferences>

/** Device default only; an explicit saved effects preference always takes precedence. */
expect fun prefersReducedVisualEffects(): Boolean
