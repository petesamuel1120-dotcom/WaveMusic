package com.maxrave.simpmusic.ui.screen.home

import simpmusic.composeapp.generated.resources.base_url
import simpmusic.composeapp.generated.resources.configured
import simpmusic.composeapp.generated.resources.custom_base_url
import simpmusic.composeapp.generated.resources.custom_base_url_prompt
import simpmusic.composeapp.generated.resources.custom_headers
import simpmusic.composeapp.generated.resources.custom_headers_json
import simpmusic.composeapp.generated.resources.custom_headers_prompt
import simpmusic.composeapp.generated.resources.github_releases
import simpmusic.composeapp.generated.resources.headers_json
import simpmusic.composeapp.generated.resources.invalid_json_format
import simpmusic.composeapp.generated.resources.invalid_url_format
import simpmusic.composeapp.generated.resources.not_set
import simpmusic.composeapp.generated.resources.openrouter_model_id
import simpmusic.composeapp.generated.resources.required_field

import com.maxrave.simpmusic.ui.component.AppContentHorizontalInset
import com.maxrave.simpmusic.ui.component.dialogMotionSource
import com.maxrave.simpmusic.ui.component.CompactHeaderContentHeight
import com.maxrave.simpmusic.ui.component.AmoledAlertDialog

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MarqueeAnimationMode
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import com.maxrave.simpmusic.ui.component.DialogTextButton as TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.sp
import com.maxrave.simpmusic.ui.component.SettingsSection
import com.maxrave.simpmusic.ui.component.SettingsNested
import com.maxrave.simpmusic.ui.component.SettingsDivider
import com.maxrave.simpmusic.ui.component.SettingsTheme
import com.maxrave.simpmusic.ui.component.BOTTOM_NAV_TRANSITION_DURATION_MILLIS
import com.maxrave.simpmusic.ui.component.slideV2EnterTransition
import com.maxrave.simpmusic.ui.component.slideV2ExitTransition
import com.maxrave.simpmusic.ui.component.finishSettingsSubpageBack
import com.maxrave.simpmusic.ui.component.cancelSettingsSubpageBack
import com.maxrave.simpmusic.ui.component.settingsBackTranslationX
import com.maxrave.simpmusic.ui.component.settingsBackAlpha
import com.maxrave.simpmusic.ui.icon.Tune
import com.maxrave.simpmusic.ui.icon.Sync
import com.maxrave.simpmusic.ui.icon.PlayCircle
import com.maxrave.simpmusic.ui.icon.Lyrics
import com.maxrave.simpmusic.ui.icon.Search
import com.maxrave.simpmusic.ui.icon.Download
import com.maxrave.simpmusic.ui.icon.GraphicEq
import com.maxrave.simpmusic.ui.icon.History
import com.maxrave.simpmusic.ui.icon.Shuffle
import com.maxrave.simpmusic.ui.icon.AccessAlarm
import com.maxrave.simpmusic.ui.icon.Translate
import com.maxrave.simpmusic.ui.icon.Settings
import com.maxrave.simpmusic.ui.icon.Delete
import com.maxrave.simpmusic.ui.icon.Info
import com.maxrave.simpmusic.ui.icon.CalendarToday
import simpmusic.composeapp.generated.resources.advanced_dialog_animations
import simpmusic.composeapp.generated.resources.advanced_dialog_animations_description
import simpmusic.composeapp.generated.resources.slim_navigation_bar
import simpmusic.composeapp.generated.resources.slim_navigation_bar_description
import simpmusic.composeapp.generated.resources.reduce_visual_effects
import simpmusic.composeapp.generated.resources.reduce_visual_effects_description
import simpmusic.composeapp.generated.resources.settings_personalize
import simpmusic.composeapp.generated.resources.settings_sound_playback
import simpmusic.composeapp.generated.resources.settings_library_services
import simpmusic.composeapp.generated.resources.settings_data_app
import simpmusic.composeapp.generated.resources.settings_appearance
import simpmusic.composeapp.generated.resources.settings_motion
import simpmusic.composeapp.generated.resources.settings_player
import simpmusic.composeapp.generated.resources.settings_lyrics_display
import simpmusic.composeapp.generated.resources.settings_account
import simpmusic.composeapp.generated.resources.settings_discovery
import simpmusic.composeapp.generated.resources.settings_streaming
import simpmusic.composeapp.generated.resources.settings_offline
import simpmusic.composeapp.generated.resources.settings_network
import simpmusic.composeapp.generated.resources.settings_volume
import simpmusic.composeapp.generated.resources.settings_equalizer
import simpmusic.composeapp.generated.resources.settings_effects
import simpmusic.composeapp.generated.resources.settings_session
import simpmusic.composeapp.generated.resources.settings_crossfade
import simpmusic.composeapp.generated.resources.settings_activity
import simpmusic.composeapp.generated.resources.settings_timing
import simpmusic.composeapp.generated.resources.settings_lastfm
import simpmusic.composeapp.generated.resources.settings_cache
import simpmusic.composeapp.generated.resources.settings_device
import simpmusic.composeapp.generated.resources.settings_backup_contents
import simpmusic.composeapp.generated.resources.settings_automatic_backup
import simpmusic.composeapp.generated.resources.settings_transfer
import simpmusic.composeapp.generated.resources.settings_updates
import simpmusic.composeapp.generated.resources.settings_credits
import simpmusic.composeapp.generated.resources.settings_appearance_description
import simpmusic.composeapp.generated.resources.settings_motion_description
import simpmusic.composeapp.generated.resources.settings_player_description
import simpmusic.composeapp.generated.resources.settings_lyrics_display_description
import simpmusic.composeapp.generated.resources.settings_network_description
import simpmusic.composeapp.generated.resources.settings_effects_description
import simpmusic.composeapp.generated.resources.settings_crossfade_description
import simpmusic.composeapp.generated.resources.settings_device_description
import simpmusic.composeapp.generated.resources.settings_automatic_backup_description
import simpmusic.composeapp.generated.resources.settings_transfer_description
import simpmusic.composeapp.generated.resources.settings_provider_description
import simpmusic.composeapp.generated.resources.settings_page_user_interface_description
import simpmusic.composeapp.generated.resources.settings_page_content_description
import simpmusic.composeapp.generated.resources.settings_page_audio_description
import simpmusic.composeapp.generated.resources.settings_page_playback_description
import simpmusic.composeapp.generated.resources.settings_page_listening_history_description
import simpmusic.composeapp.generated.resources.settings_page_lyrics_description
import simpmusic.composeapp.generated.resources.settings_page_lastfm_description
import simpmusic.composeapp.generated.resources.settings_page_storage_description
import simpmusic.composeapp.generated.resources.settings_page_backup_description
import simpmusic.composeapp.generated.resources.settings_page_about_us_description
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import com.kyant.backdrop.highlight.Highlight
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.eygraber.uri.toKmpUri
import com.maxrave.common.LIMIT_CACHE_SIZE
import com.maxrave.common.QUALITY
import com.maxrave.common.VIDEO_QUALITY
import com.maxrave.common.SUPPORTED_LANGUAGE
import com.maxrave.common.SUPPORTED_LOCATION
import com.maxrave.domain.extension.now
import com.maxrave.domain.data.model.lyrics.RomanizationDictionaryState
import com.maxrave.domain.data.model.lyrics.RomanizationLanguage
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.repository.ImportProgress
import com.maxrave.domain.utils.LocalResource
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.Platform
import com.maxrave.simpmusic.expect.ui.PredictiveBackHandler
import com.maxrave.simpmusic.expect.ui.fileSaverResult
import com.maxrave.simpmusic.expect.ui.isLyricsBlurSupported
import com.maxrave.simpmusic.expect.ui.openEqResult
import com.maxrave.simpmusic.extension.bytesToMB
import com.maxrave.simpmusic.extension.displayString
import com.maxrave.simpmusic.extension.isValidProxyHost
import com.maxrave.simpmusic.getPlatform
import com.maxrave.simpmusic.ui.component.ActionButton
import com.maxrave.simpmusic.ui.component.auroraGradientBackground
import com.maxrave.simpmusic.ui.component.CenterLoadingBox
import com.maxrave.simpmusic.ui.component.EndOfPage
import com.maxrave.simpmusic.ui.component.FrostedHeader
import com.maxrave.simpmusic.ui.component.LocalSharedHeaderState
import com.maxrave.simpmusic.ui.component.rememberSettingsHeaderScrollOffset
import com.maxrave.simpmusic.ui.component.PageTopAppBar
import com.maxrave.simpmusic.ui.component.pageHeaderContentHeight
import com.maxrave.simpmusic.ui.component.frostedHeaderSource
import com.maxrave.simpmusic.expect.ui.rememberBackdrop
import com.maxrave.simpmusic.ui.component.LoadingDialog
import com.maxrave.simpmusic.ui.component.RippleIconButton
import com.maxrave.simpmusic.ui.component.SettingItem
import com.maxrave.simpmusic.ui.component.AccentColorSetting
import com.maxrave.simpmusic.ui.component.BackgroundColorSetting
import com.maxrave.simpmusic.ui.navigation.destination.home.LiquidGlassSettingsDestination
import simpmusic.composeapp.generated.resources.liquid_glass_intensity
import simpmusic.composeapp.generated.resources.liquid_glass_intensity_description
import simpmusic.composeapp.generated.resources.persistent_header_buttons
import simpmusic.composeapp.generated.resources.persistent_header_buttons_description
import com.maxrave.simpmusic.ui.component.liquidGlass
import com.maxrave.simpmusic.ui.icon.ArrowBackIosNew
import com.maxrave.simpmusic.ui.icon.Close
import com.maxrave.simpmusic.ui.icon.Error
import com.maxrave.simpmusic.ui.icon.PeopleAlt
import com.maxrave.simpmusic.ui.icon.PlaylistAdd
import com.maxrave.simpmusic.ui.icon.SimpIcons
import com.maxrave.simpmusic.ui.navigation.destination.login.LastfmLoginDestination
import com.maxrave.simpmusic.ui.navigation.destination.login.LoginDestination
import com.maxrave.simpmusic.ui.theme.md_theme_dark_primary
import com.maxrave.simpmusic.ui.theme.typo
import com.maxrave.simpmusic.ui.theme.desktopPanelDark
import com.maxrave.simpmusic.utils.VersionManager
import com.maxrave.simpmusic.viewModel.ImportViewModel
import com.maxrave.simpmusic.viewModel.SettingAlertState
import com.maxrave.simpmusic.viewModel.SettingBasicAlertState
import com.maxrave.simpmusic.viewModel.SettingsViewModel
import com.maxrave.simpmusic.viewModel.SharedViewModel
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.ChipColors
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import com.mikepenz.aboutlibraries.ui.compose.m3.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.m3.libraryColors
import com.mikepenz.aboutlibraries.ui.compose.produceLibraries
import com.mohamedrejeb.calf.core.ExperimentalCalfApi
import com.mohamedrejeb.calf.io.getPath
import com.mohamedrejeb.calf.picker.FilePickerFileType
import com.mohamedrejeb.calf.picker.FilePickerSelectionMode
import com.mohamedrejeb.calf.picker.rememberFilePickerLauncher
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.FormatStringsInDatetimeFormats
import kotlinx.datetime.format.byUnicodePattern
import dev.chrisbanes.haze.hazeSource
import dev.chrisbanes.haze.rememberHazeState
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.about_us
import simpmusic.composeapp.generated.resources.add_an_account
import simpmusic.composeapp.generated.resources.ai
import simpmusic.composeapp.generated.resources.ai_lyrics_translation
import simpmusic.composeapp.generated.resources.ai_api_key
import simpmusic.composeapp.generated.resources.ai_provider
import simpmusic.composeapp.generated.resources.anonymous
import simpmusic.composeapp.generated.resources.app_name
import simpmusic.composeapp.generated.resources.audio
import simpmusic.composeapp.generated.resources.audio_delay
import simpmusic.composeapp.generated.resources.audio_delay_description
import simpmusic.composeapp.generated.resources.audio_reverb
import simpmusic.composeapp.generated.resources.audio_reverb_description
import simpmusic.composeapp.generated.resources.author
import simpmusic.composeapp.generated.resources.auto_backup
import simpmusic.composeapp.generated.resources.auto_backup_description
import simpmusic.composeapp.generated.resources.auto_check_for_update
import simpmusic.composeapp.generated.resources.auto_check_for_update_description
import simpmusic.composeapp.generated.resources.auto_download_liked_songs
import simpmusic.composeapp.generated.resources.auto_download_liked_songs_description
import simpmusic.composeapp.generated.resources.backup
import simpmusic.composeapp.generated.resources.backup_downloaded
import simpmusic.composeapp.generated.resources.backup_downloaded_description
import simpmusic.composeapp.generated.resources.backup_frequency
import simpmusic.composeapp.generated.resources.balance_media_loudness
import simpmusic.composeapp.generated.resources.better_lyrics
import simpmusic.composeapp.generated.resources.cancel
import simpmusic.composeapp.generated.resources.change
import simpmusic.composeapp.generated.resources.change_language_warning
import simpmusic.composeapp.generated.resources.check_for_update
import simpmusic.composeapp.generated.resources.checking
import simpmusic.composeapp.generated.resources.clear
import simpmusic.composeapp.generated.resources.clear_downloaded_cache
import simpmusic.composeapp.generated.resources.clear_listening_history
import simpmusic.composeapp.generated.resources.clear_listening_history_confirm
import simpmusic.composeapp.generated.resources.clear_listening_history_description
import simpmusic.composeapp.generated.resources.clear_player_cache
import simpmusic.composeapp.generated.resources.clear_thumbnail_cache
import simpmusic.composeapp.generated.resources.content
import simpmusic.composeapp.generated.resources.content_country
import simpmusic.composeapp.generated.resources.crossfade
import simpmusic.composeapp.generated.resources.crossfade_auto
import simpmusic.composeapp.generated.resources.crossfade_description
import simpmusic.composeapp.generated.resources.crossfade_dj_mode
import simpmusic.composeapp.generated.resources.crossfade_dj_mode_description
import simpmusic.composeapp.generated.resources.crossfade_duration
import simpmusic.composeapp.generated.resources.crossfade_skip_album
import simpmusic.composeapp.generated.resources.crossfade_skip_album_description
import simpmusic.composeapp.generated.resources.custom_ai_model_id
import simpmusic.composeapp.generated.resources.custom_model_id_messages
import simpmusic.composeapp.generated.resources.daily
import simpmusic.composeapp.generated.resources.database
import simpmusic.composeapp.generated.resources.default_models
import simpmusic.composeapp.generated.resources.description_and_licenses
import simpmusic.composeapp.generated.resources.tracks_download_quality
import simpmusic.composeapp.generated.resources.video_download_quality
import simpmusic.composeapp.generated.resources.video_quality
import simpmusic.composeapp.generated.resources.downloaded_cache
import simpmusic.composeapp.generated.resources.enable_liquid_glass_effect
import simpmusic.composeapp.generated.resources.enable_liquid_glass_effect_description
import simpmusic.composeapp.generated.resources.enable_scrobbling
import simpmusic.composeapp.generated.resources.equalizer
import simpmusic.composeapp.generated.resources.equalizer_description
import simpmusic.composeapp.generated.resources.equalizer_type
import simpmusic.composeapp.generated.resources.equalizer_type_built_in
import simpmusic.composeapp.generated.resources.equalizer_type_system
import simpmusic.composeapp.generated.resources.free_space
import simpmusic.composeapp.generated.resources.gemini
import simpmusic.composeapp.generated.resources.guest
import simpmusic.composeapp.generated.resources.http
import simpmusic.composeapp.generated.resources.import_data
import simpmusic.composeapp.generated.resources.import_data_intro
import simpmusic.composeapp.generated.resources.import_failed
import simpmusic.composeapp.generated.resources.import_playlists_from_other_apps
import simpmusic.composeapp.generated.resources.playlist_converter
import simpmusic.composeapp.generated.resources.import_progress_songs
import simpmusic.composeapp.generated.resources.import_reading_file
import simpmusic.composeapp.generated.resources.import_result
import simpmusic.composeapp.generated.resources.import_result_skipped
import simpmusic.composeapp.generated.resources.intro_login_to_lastfm
import simpmusic.composeapp.generated.resources.invalid
import simpmusic.composeapp.generated.resources.invalid_api_key
import simpmusic.composeapp.generated.resources.invalid_host
import simpmusic.composeapp.generated.resources.invalid_port
import simpmusic.composeapp.generated.resources.keep_backups
import simpmusic.composeapp.generated.resources.keep_backups_format
import simpmusic.composeapp.generated.resources.keep_your_youtube_playlist_offline
import simpmusic.composeapp.generated.resources.keep_your_youtube_playlist_offline_description
import simpmusic.composeapp.generated.resources.kill_service_on_exit
import simpmusic.composeapp.generated.resources.kill_service_on_exit_description
import simpmusic.composeapp.generated.resources.language
import simpmusic.composeapp.generated.resources.last_backup
import simpmusic.composeapp.generated.resources.last_checked_at
import simpmusic.composeapp.generated.resources.lastfm_integration
import simpmusic.composeapp.generated.resources.limit_player_cache
import simpmusic.composeapp.generated.resources.listening_history
import simpmusic.composeapp.generated.resources.local_tracking_description
import simpmusic.composeapp.generated.resources.local_tracking_title
import simpmusic.composeapp.generated.resources.log_in_to_lastfm
import simpmusic.composeapp.generated.resources.log_out
import simpmusic.composeapp.generated.resources.log_out_from_lastfm
import simpmusic.composeapp.generated.resources.log_out_warning
import simpmusic.composeapp.generated.resources.logged_in
import simpmusic.composeapp.generated.resources.logged_in_as
import simpmusic.composeapp.generated.resources.lrclib
import simpmusic.composeapp.generated.resources.lyrics
import simpmusic.composeapp.generated.resources.lyrics_style
import com.maxrave.simpmusic.ui.component.LyricsOffsetControl
import simpmusic.composeapp.generated.resources.lyrics_offset_global_scope
import simpmusic.composeapp.generated.resources.lyrics_romanization
import simpmusic.composeapp.generated.resources.lyrics_romanization_description
import simpmusic.composeapp.generated.resources.romanization_belarusian
import simpmusic.composeapp.generated.resources.romanization_bulgarian
import simpmusic.composeapp.generated.resources.romanization_chinese
import simpmusic.composeapp.generated.resources.romanization_hindi
import simpmusic.composeapp.generated.resources.romanization_japanese
import simpmusic.composeapp.generated.resources.romanization_japanese_dict_downloading
import simpmusic.composeapp.generated.resources.romanization_japanese_dict_failed
import simpmusic.composeapp.generated.resources.romanization_korean
import simpmusic.composeapp.generated.resources.romanization_kyrgyz
import simpmusic.composeapp.generated.resources.romanization_macedonian
import simpmusic.composeapp.generated.resources.romanization_punjabi
import simpmusic.composeapp.generated.resources.romanization_russian
import simpmusic.composeapp.generated.resources.romanization_serbian
import simpmusic.composeapp.generated.resources.romanization_ukrainian
import simpmusic.composeapp.generated.resources.lyrics_style_apple_music
import simpmusic.composeapp.generated.resources.lyrics_style_classic
import simpmusic.composeapp.generated.resources.lyrics_style_enhanced
import simpmusic.composeapp.generated.resources.lyrics_font_size
import simpmusic.composeapp.generated.resources.translation_lyrics_font_size
import simpmusic.composeapp.generated.resources.manage_your_youtube_accounts
import simpmusic.composeapp.generated.resources.monthly
import simpmusic.composeapp.generated.resources.never
import simpmusic.composeapp.generated.resources.no_account
import simpmusic.composeapp.generated.resources.normalize_volume
import simpmusic.composeapp.generated.resources.not_available_while_casting
import simpmusic.composeapp.generated.resources.now_playing_style
import simpmusic.composeapp.generated.resources.now_playing_style_apple_music
import simpmusic.composeapp.generated.resources.ok
import simpmusic.composeapp.generated.resources.open_system_equalizer
import simpmusic.composeapp.generated.resources.openai
import simpmusic.composeapp.generated.resources.openai_api_compatible
import simpmusic.composeapp.generated.resources.other_app
import simpmusic.composeapp.generated.resources.play_explicit_content
import simpmusic.composeapp.generated.resources.play_explicit_content_description
import simpmusic.composeapp.generated.resources.playback
import simpmusic.composeapp.generated.resources.player_cache
import simpmusic.composeapp.generated.resources.proxy
import simpmusic.composeapp.generated.resources.proxy_description
import simpmusic.composeapp.generated.resources.proxy_host
import simpmusic.composeapp.generated.resources.proxy_host_message
import simpmusic.composeapp.generated.resources.proxy_password
import simpmusic.composeapp.generated.resources.proxy_password_message
import simpmusic.composeapp.generated.resources.proxy_port
import simpmusic.composeapp.generated.resources.proxy_port_message
import simpmusic.composeapp.generated.resources.proxy_type
import simpmusic.composeapp.generated.resources.proxy_username
import simpmusic.composeapp.generated.resources.proxy_username_message
import simpmusic.composeapp.generated.resources.tracks_quality
import simpmusic.composeapp.generated.resources.radio_audio_only
import simpmusic.composeapp.generated.resources.radio_audio_only_description
import simpmusic.composeapp.generated.resources.requires_android_12
import simpmusic.composeapp.generated.resources.restore_your_data
import simpmusic.composeapp.generated.resources.restore_your_saved_data
import simpmusic.composeapp.generated.resources.rich_presence_info
import simpmusic.composeapp.generated.resources.save
import simpmusic.composeapp.generated.resources.save_all_your_playlist_data
import simpmusic.composeapp.generated.resources.save_playback_state
import simpmusic.composeapp.generated.resources.save_shuffle_and_repeat_mode
import simpmusic.composeapp.generated.resources.scrobbling_info
import simpmusic.composeapp.generated.resources.send_back_listening_data_to_google
import simpmusic.composeapp.generated.resources.set
import simpmusic.composeapp.generated.resources.settings
import simpmusic.composeapp.generated.resources.signed_in
import simpmusic.composeapp.generated.resources.simpmusic_lyrics
import simpmusic.composeapp.generated.resources.skip_no_music_part
import simpmusic.composeapp.generated.resources.skip_silent
import simpmusic.composeapp.generated.resources.socks
import simpmusic.composeapp.generated.resources.storage
import simpmusic.composeapp.generated.resources.sync_follow_to_youtube
import simpmusic.composeapp.generated.resources.sync_follow_to_youtube_description
import simpmusic.composeapp.generated.resources.theme
import simpmusic.composeapp.generated.resources.theme_mode_dark
import simpmusic.composeapp.generated.resources.theme_mode_light
import simpmusic.composeapp.generated.resources.theme_mode_system
import simpmusic.composeapp.generated.resources.third_party_libraries
import simpmusic.composeapp.generated.resources.thumbnail_cache
import simpmusic.composeapp.generated.resources.transition
import simpmusic.composeapp.generated.resources.transition_fade
import simpmusic.composeapp.generated.resources.transition_slide
import simpmusic.composeapp.generated.resources.transition_slide_v2
import simpmusic.composeapp.generated.resources.translation_language
import simpmusic.composeapp.generated.resources.follow_app_language
import simpmusic.composeapp.generated.resources.unknown
import simpmusic.composeapp.generated.resources.update_channel
import simpmusic.composeapp.generated.resources.upload_your_listening_history_to_youtube_music_server_it_will_make_yt_music_recommendation_system_better_working_only_if_logged_in
import simpmusic.composeapp.generated.resources.use_ai_translation
import simpmusic.composeapp.generated.resources.use_ai_translation_description
import simpmusic.composeapp.generated.resources.use_your_system_equalizer
import simpmusic.composeapp.generated.resources.user_interface
import simpmusic.composeapp.generated.resources.version
import simpmusic.composeapp.generated.resources.warning
import simpmusic.composeapp.generated.resources.weekly
import simpmusic.composeapp.generated.resources.youtube_account
import simpmusic.composeapp.generated.resources.youtube_transcript
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalCoilApi::class,
    FormatStringsInDatetimeFormats::class,
    ExperimentalCalfApi::class,
)
@Composable
fun SettingScreen(
    innerPadding: PaddingValues,
    navController: NavController,
    onOpenYouTubeAccount: () -> Unit = {},
    viewModel: SettingsViewModel = koinViewModel(),
    sharedViewModel: SharedViewModel = koinInject(),
) {
    val platformContext = LocalPlatformContext.current
    val pl = com.mohamedrejeb.calf.core.LocalPlatformContext.current
    val localDensity = LocalDensity.current
    val uriHandler = LocalUriHandler.current
    val coroutineScope = rememberCoroutineScope()

    var width by rememberSaveable { mutableIntStateOf(0) }

    // Backup and restore
    val formatter =
        LocalDateTime.Format {
            byUnicodePattern("yyyyMMddHHmmss")
        }
    val appName = stringResource(Res.string.app_name)

    val backupLauncher =
        fileSaverResult(
            "${appName}_${
                now().format(
                    formatter,
                )
            }.backup",
            "application/octet-stream",
        ) { uri ->
            uri?.let {
                viewModel.backup(it.toKmpUri())
            }
        }

    val restoreLauncher =
        rememberFilePickerLauncher(
            type =
                FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
        ) { file ->
            file.firstOrNull()?.getPath(pl)?.toKmpUri()?.let {
                viewModel.restore(it)
            }
        }

    // Import playlists converted on the web. Unlike restore, the file is read through Calf's
    // KmpFile rather than a Uri, so no expect/actual is needed. The type stays All because a
    // converted .json arrives with whatever MIME its source assigned it, and an application/json
    // filter would hide it on some hosts.
    val importViewModel: ImportViewModel = koinViewModel()
    val importState by importViewModel.importState.collectAsStateWithLifecycle()
    val importLauncher =
        rememberFilePickerLauncher(
            type =
                FilePickerFileType.All,
            selectionMode = FilePickerSelectionMode.Single,
        ) { file ->
            file.firstOrNull()?.let {
                importViewModel.import(it, pl)
            }
        }

    // Open equalizer
    val resultLauncher = openEqResult(viewModel.getAudioSessionId())

    val language by viewModel.language.collectAsStateWithLifecycle()
    val location by viewModel.location.collectAsStateWithLifecycle()
    val quality by viewModel.quality.collectAsStateWithLifecycle()
    val downloadQuality by viewModel.downloadQuality.collectAsStateWithLifecycle()
    val videoQuality by viewModel.videoQuality.collectAsStateWithLifecycle(initialValue = "720p")
    val videoDownloadQuality by viewModel.videoDownloadQuality.collectAsStateWithLifecycle(initialValue = "720p")
    val autoDownloadLikedSongs by viewModel.autoDownloadLikedSongs.collectAsStateWithLifecycle()
    val keepYoutubePlaylistOffline by viewModel.keepYouTubePlaylistOffline.collectAsStateWithLifecycle()
    val localTrackingEnabled by viewModel.localTrackingEnabled.collectAsStateWithLifecycle(initialValue = true)
    val combineLocalAndYouTubeLiked by viewModel.combineLocalAndYouTubeLiked.collectAsStateWithLifecycle()
    val radioAudioOnly by remember { viewModel.radioAudioOnly.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = true)
    val sendData by remember { viewModel.sendBackToGoogle.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = true)
    val normalizeVolume by remember { viewModel.normalizeVolume.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val skipSilent by remember { viewModel.skipSilent.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val savePlaybackState by remember { viewModel.savedPlaybackState.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = false)
    val killServiceOnExit by remember { viewModel.killServiceOnExit.map { it == TRUE } }.collectAsStateWithLifecycle(initialValue = true)
    val lyricsOffsetMs by viewModel.lyricsOffsetMs.collectAsStateWithLifecycle()
    val playerCache by viewModel.cacheSize.collectAsStateWithLifecycle()
    val downloadedCache by viewModel.downloadedCacheSize.collectAsStateWithLifecycle()
    val thumbnailCache by viewModel.thumbCacheSize.collectAsStateWithLifecycle()
    val limitPlayerCache by viewModel.playerCacheLimit.collectAsStateWithLifecycle()
    val fraction by viewModel.fraction.collectAsStateWithLifecycle()
    val lastCheckUpdate by viewModel.lastCheckForUpdate.collectAsStateWithLifecycle()
    val explicitContentEnabled by viewModel.explicitContentEnabled.collectAsStateWithLifecycle()
    val usingProxy by viewModel.usingProxy.collectAsStateWithLifecycle()
    val proxyType by viewModel.proxyType.collectAsStateWithLifecycle()
    val proxyHost by viewModel.proxyHost.collectAsStateWithLifecycle()
    val proxyPort by viewModel.proxyPort.collectAsStateWithLifecycle()
    val proxyUsername by viewModel.proxyUsername.collectAsStateWithLifecycle()
    val proxyPassword by viewModel.proxyPassword.collectAsStateWithLifecycle()
    val autoCheckUpdate by viewModel.autoCheckUpdate.collectAsStateWithLifecycle()
    val aiProvider by viewModel.aiProvider.collectAsStateWithLifecycle()
    val aiProviderName =
        when (aiProvider) {
            DataStoreManager.AI_PROVIDER_OPENAI -> stringResource(Res.string.openai)
            DataStoreManager.AI_PROVIDER_GEMINI -> stringResource(Res.string.gemini)
            DataStoreManager.AI_PROVIDER_OPENROUTER -> "OpenRouter"
            DataStoreManager.AI_PROVIDER_GROQ -> "Groq"
            DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI -> stringResource(Res.string.openai_api_compatible)
            else -> stringResource(Res.string.unknown)
        }
    val isHasApiKey by viewModel.isHasApiKey.collectAsStateWithLifecycle()
    val showLyricsTranslationButton by viewModel.showLyricsTranslationButton.collectAsStateWithLifecycle()
    val translationLanguage by viewModel.translationLanguage.collectAsStateWithLifecycle()
    val followAppLanguage = stringResource(Res.string.follow_app_language)
    val translationLanguageOptions = listOf("" to followAppLanguage) + translationLanguages.toList()
    val selectedTranslationCode = translationLanguageCode(translationLanguage)
    val selectedTranslationName = translationLanguages[selectedTranslationCode] ?: followAppLanguage
    val customModelId by viewModel.customModelId.collectAsStateWithLifecycle()
    val openRouterModelId by viewModel.openRouterModelId.collectAsStateWithLifecycle()
    val openRouterApiKey by viewModel.openRouterApiKey.collectAsStateWithLifecycle()
    val customOpenAIBaseUrl by viewModel.customOpenAIBaseUrl.collectAsStateWithLifecycle()
    val customOpenAIHeaders by viewModel.customOpenAIHeaders.collectAsStateWithLifecycle()
    val backupDownloaded by viewModel.backupDownloaded.collectAsStateWithLifecycle()
    val autoBackupEnabled by viewModel.autoBackupEnabled.collectAsStateWithLifecycle()
    val autoBackupFrequency by viewModel.autoBackupFrequency.collectAsStateWithLifecycle()
    val autoBackupMaxFiles by viewModel.autoBackupMaxFiles.collectAsStateWithLifecycle()
    val autoBackupLastTime by viewModel.autoBackupLastTime.collectAsStateWithLifecycle()
    val updateChannel by viewModel.updateChannel.collectAsStateWithLifecycle()
    val enableLiquidGlass by viewModel.enableLiquidGlass.collectAsStateWithLifecycle()
    val fixedHeader by sharedViewModel.fixedHeader.collectAsStateWithLifecycle()
    val slimNavigationBar by sharedViewModel.getSlimNavigationBar()
        .collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val themeMode by sharedViewModel.getThemeMode().collectAsStateWithLifecycle(DataStoreManager.THEME_MODE_SYSTEM)
    val pageTransitionStyle by sharedViewModel.getPageTransitionStyle().collectAsStateWithLifecycle(DataStoreManager.PAGE_TRANSITION_SLIDE_V2)
    val advancedDialogAnimations by sharedViewModel.getAdvancedDialogAnimations()
        .collectAsStateWithLifecycle(DataStoreManager.FALSE)
    val reduceVisualEffects by sharedViewModel.getReduceVisualEffects()
        .collectAsStateWithLifecycle(
            if (com.maxrave.data.dataStore.prefersReducedVisualEffects()) DataStoreManager.TRUE else DataStoreManager.FALSE,
        )
    val reduceVisualEffectsEnabled = reduceVisualEffects == DataStoreManager.TRUE
    val appBackgroundPreset by sharedViewModel.getAppBackgroundPreset()
        .collectAsStateWithLifecycle(DataStoreManager.APP_BACKGROUND_AURORA)
    val effectivePageTransitionStyle =
        if (reduceVisualEffectsEnabled) DataStoreManager.PAGE_TRANSITION_SLIDE_V2 else pageTransitionStyle
    val themeColorSource by sharedViewModel.getThemeColorSource().collectAsStateWithLifecycle(DataStoreManager.THEME_COLOR_DEFAULT)
    val customThemeColorHex by sharedViewModel.getCustomThemeColor().collectAsStateWithLifecycle(DataStoreManager.DEFAULT_THEME_COLOR_HEX)
    val lyricsStyle by sharedViewModel.getLyricsStyle().collectAsStateWithLifecycle(DataStoreManager.LYRICS_STYLE_CLASSIC)
    val lyricsFontSizeTenthsPt by sharedViewModel.getLyricsFontSizeTenthsPt().collectAsStateWithLifecycle(DataStoreManager.LYRICS_FONT_SIZE_DEFAULT_TENTHS_PT)
    val translatedLyricsFontSizeTenthsPt by sharedViewModel.getTranslatedLyricsFontSizeTenthsPt().collectAsStateWithLifecycle(DataStoreManager.TRANSLATED_LYRICS_FONT_SIZE_DEFAULT_TENTHS_PT)
    var selectedLyricsFontSize by rememberSaveable(lyricsFontSizeTenthsPt) { mutableStateOf(lyricsFontSizeTenthsPt.toFloat()) }
    var selectedTranslatedLyricsFontSize by rememberSaveable(translatedLyricsFontSizeTenthsPt) { mutableStateOf(translatedLyricsFontSizeTenthsPt.toFloat()) }
    val romanizationStored by sharedViewModel.getRomanizationLanguages().collectAsStateWithLifecycle("")
    val japaneseDictionaryState by viewModel.japaneseDictionaryState.collectAsStateWithLifecycle()
    val loggedIn by viewModel.loggedIn.collectAsStateWithLifecycle()
    val syncFollowToYouTube by viewModel.syncFollowToYouTube.collectAsStateWithLifecycle()
    val equalizerEnabled by viewModel.equalizerEnabled.collectAsStateWithLifecycle()
    val equalizerType by viewModel.equalizerType.collectAsStateWithLifecycle()
    val delayEnabled by viewModel.delayEnabled.collectAsStateWithLifecycle()
    val reverbEnabled by viewModel.reverbEnabled.collectAsStateWithLifecycle()
    val lastfmLoggedIn by viewModel.lastfmLoggedIn.collectAsStateWithLifecycle()
    val lastfmUsername by viewModel.lastfmUsername.collectAsStateWithLifecycle()
    val lastfmScrobbleEnabled by viewModel.lastfmScrobbleEnabled.collectAsStateWithLifecycle()

    val crossfadeEnabled by viewModel.crossfadeEnabled.collectAsStateWithLifecycle()
    val crossfadeDuration by viewModel.crossfadeDuration.collectAsStateWithLifecycle()
    val crossfadeDjMode by viewModel.crossfadeDjMode.collectAsStateWithLifecycle()
    val crossfadeSkipAlbum by viewModel.crossfadeSkipAlbum.collectAsStateWithLifecycle()
    val castState by viewModel.castState.collectAsStateWithLifecycle()

    val isCheckingUpdate by sharedViewModel.isCheckingUpdate.collectAsStateWithLifecycle()

    val settingsHeaderTopInset = with(localDensity) { WindowInsets.statusBars.getTop(this).toDp() }
    val settingsHeaderContentHeight = pageHeaderContentHeight()
    val settingsHeaderHazeState = rememberHazeState()
    val settingsHeaderBackdrop = rememberBackdrop(MaterialTheme.colorScheme.background)

    val formattedLastCheck = remember(lastCheckUpdate) {
        val lastCheckLong = lastCheckUpdate?.toLong() ?: 0L
        DateTimeFormatter
            .ofPattern("yyyy-MM-dd HH:mm:ss")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(lastCheckLong))
    }
    // Resource lookup is suspend-backed. Resolve it through Compose instead of runBlocking on the
    // UI thread whenever Settings recomposes.
    val checkForUpdateSubtitle =
        if (isCheckingUpdate) {
            stringResource(Res.string.checking)
        } else {
            stringResource(Res.string.last_checked_at, formattedLastCheck)
        }
    var showThirdPartyLibraries by rememberSaveable {
        mutableStateOf(false)
    }
    var selectedSettingsPage by rememberSaveable {
        mutableStateOf<String?>(null)
    }
    // Keep the overview position alive while AnimatedContent removes it for a subpage.
    val settingsOverviewListState = rememberLazyListState()
    val settingsDetailListState = rememberLazyListState()
    var settingsHeaderSurfaceVisible by remember { mutableStateOf(false) }

    val settingsPages =
        listOf(
            "user_interface" to stringResource(Res.string.user_interface),
            "content" to stringResource(Res.string.content),
        ) +
            (if (getPlatform() == Platform.Android) {
                listOf("audio" to stringResource(Res.string.audio))
            } else {
                emptyList()
            }) +
            listOf(
                "playback" to stringResource(Res.string.playback),
                "listening_history" to stringResource(Res.string.listening_history),
                "lyrics" to stringResource(Res.string.lyrics),
            ) +
            (if (viewModel.lastfmAvailable) {
                listOf("lastfm" to stringResource(Res.string.lastfm_integration))
            } else {
                emptyList()
            }) +
            (if (getPlatform() == Platform.Android) {
                listOf("storage" to stringResource(Res.string.storage))
            } else {
                emptyList()
            }) +
            listOf(
                "backup" to stringResource(Res.string.backup),
                "about_us" to stringResource(Res.string.about_us),
            )

    val predictiveBackProgress = remember { mutableFloatStateOf(0f) }
    var settingsViewportWidthPx by remember { mutableIntStateOf(0) }
    val predictiveBackActive by remember {
        derivedStateOf { predictiveBackProgress.floatValue > 0f }
    }
    var predictiveBackCommitted by remember { mutableStateOf(false) }
    var predictiveBackCommitInProgress by remember { mutableStateOf(false) }
    PredictiveBackHandler(
        enabled =
            selectedSettingsPage != null &&
                !showThirdPartyLibraries &&
                !predictiveBackCommitInProgress,
        onProgress = { progress -> predictiveBackProgress.floatValue = progress.coerceIn(0f, 1f) },
        onCancelled = {
            cancelSettingsSubpageBack(predictiveBackProgress)
            predictiveBackCommitInProgress = false
        },
        onBack = {
            if (!predictiveBackCommitInProgress) {
                // Release this handler as soon as the close is committed. A second back during
                // the finishing animation should be handled by the app screen underneath.
                predictiveBackCommitInProgress = true
                finishSettingsSubpageBack(
                    transitionStyle = effectivePageTransitionStyle,
                    progress = predictiveBackProgress,
                    onPredictiveCommit = { predictiveBackCommitted = true },
                    onBack = { selectedSettingsPage = null },
                )
            }
        },
    )
    LaunchedEffect(selectedSettingsPage) {
        if (selectedSettingsPage == "AI") selectedSettingsPage = "lyrics"
        if (selectedSettingsPage == null && predictiveBackCommitInProgress) {
            if (predictiveBackCommitted) {
                // Let AnimatedContent install its no-transition destination before releasing
                // the outgoing page from its final predictive-back offset.
                withFrameNanos { }
            }
            predictiveBackProgress.floatValue = 0f
            predictiveBackCommitted = false
            predictiveBackCommitInProgress = false
        }
    }

    LaunchedEffect(true) {
        viewModel.getAllGoogleAccount()
    }

    LaunchedEffect(true) {
        viewModel.initializeDataIfNeeded()
        viewModel.getThumbCacheSize(platformContext)
    }

    val settingsPageTitle = settingsPages.firstOrNull { it.first == selectedSettingsPage }?.second
    val settingsHeaderHost = LocalSharedHeaderState.current
    val activeSettingsListState =
        if (selectedSettingsPage == null) settingsOverviewListState else settingsDetailListState
    val settingsHeaderOffset = rememberSettingsHeaderScrollOffset(
        headerHeight = settingsHeaderTopInset + settingsHeaderContentHeight,
        pageKey = selectedSettingsPage,
        keepVisible = predictiveBackActive || predictiveBackCommitted,
    ) {
        activeSettingsListState.firstVisibleItemIndex to activeSettingsListState.firstVisibleItemScrollOffset
    }
    LaunchedEffect(selectedSettingsPage, settingsOverviewListState, settingsDetailListState) {
        val activeListState =
            if (selectedSettingsPage == null) settingsOverviewListState else settingsDetailListState
        snapshotFlow {
            activeListState.firstVisibleItemIndex > 0 || activeListState.firstVisibleItemScrollOffset > 0
        }.collect { settingsHeaderSurfaceVisible = it }
    }
    val isLightTheme = MaterialTheme.colorScheme.background.luminance() > 0.5f
    val settingsBackground =
        if (getPlatform() == Platform.Desktop) {
            if (isLightTheme) MaterialTheme.colorScheme.surfaceContainer else desktopPanelDark
        } else {
            MaterialTheme.colorScheme.background
        }
    SettingsTheme {
    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .onSizeChanged { settingsViewportWidthPx = it.width }
                .background(settingsBackground),
    ) {
    // Capture the whole page, including its aurora background, so the floating header has real
    // pixels behind it to blur. A source on the LazyColumn alone only sees its list content.
    Box(
        Modifier
            .fillMaxSize()
            .frostedHeaderSource(settingsHeaderBackdrop)
            .then(if (enableLiquidGlass) Modifier.hazeSource(settingsHeaderHazeState) else Modifier),
    ) {
    if (selectedSettingsPage != null && predictiveBackActive) {
        val previewListState = rememberLazyListState(
            initialFirstVisibleItemIndex = settingsOverviewListState.firstVisibleItemIndex,
            initialFirstVisibleItemScrollOffset = settingsOverviewListState.firstVisibleItemScrollOffset,
        )
        LazyColumn(
            state = previewListState,
            contentPadding = innerPadding,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        translationX = settingsBackTranslationX(
                            effectivePageTransitionStyle, size.width.toInt(), predictiveBackProgress.floatValue, entering = true,
                        )
                        alpha = settingsBackAlpha(effectivePageTransitionStyle, predictiveBackProgress.floatValue, entering = true)
                    }
                    .background(settingsBackground)
                    .auroraGradientBackground(isLightTheme)
                    .padding(horizontal = AppContentHorizontalInset),
        ) {
            item(key = "settings_predictive_back_preview") {
                Column {
                    Spacer(Modifier.height(settingsHeaderContentHeight + 16.dp))
                    SettingsOverview(settingsPages = settingsPages, onOpenPage = {})
                    EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
                }
            }
        }
    }
    AnimatedContent(
        targetState = selectedSettingsPage,
        modifier = Modifier.fillMaxSize(),
        transitionSpec = {
            if (predictiveBackCommitted) {
                EnterTransition.None togetherWith ExitTransition.None
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                val openingDetailPage = targetState != null
                slideV2EnterTransition(openingDetailPage)
                    .togetherWith(slideV2ExitTransition(openingDetailPage))
                    .using(null)
            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                val openingDetailPage = targetState != null
                slideInHorizontally(
                    tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
                ) { width -> if (openingDetailPage) width else -width }
                    .togetherWith(
                        slideOutHorizontally(
                            tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
                        ) { width -> if (openingDetailPage) -width else width },
                    )
            } else {
                fadeIn(tween(260)).togetherWith(fadeOut(tween(170)))
            }
        },
        label = "settings_subpage_transition",
    ) { displayedSettingsPage ->
    val settingListState =
        if (displayedSettingsPage == null) settingsOverviewListState else settingsDetailListState
    LazyColumn(
        state = settingListState,
        contentPadding = innerPadding,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .fillMaxSize()
                .graphicsLayer {
                    if (displayedSettingsPage != null &&
                        (displayedSettingsPage == selectedSettingsPage || predictiveBackCommitted)
                    ) {
                        translationX = settingsBackTranslationX(
                            effectivePageTransitionStyle, size.width.toInt(), predictiveBackProgress.floatValue, entering = false,
                        )
                        alpha = settingsBackAlpha(effectivePageTransitionStyle, predictiveBackProgress.floatValue, entering = false)
                    }
                }
                .background(settingsBackground)
                .auroraGradientBackground(isLightTheme)
                .padding(horizontal = AppContentHorizontalInset),
    ) {
        if (displayedSettingsPage != null) {
            item(key = "settings_detail_top_space") {
                Spacer(Modifier.height(settingsHeaderContentHeight + 16.dp))
            }
        }
        if (displayedSettingsPage == null) {
            item(key = "settings_categories") {
                Column {
                    Spacer(Modifier.height(settingsHeaderContentHeight + 16.dp))
                    SettingsOverview(settingsPages = settingsPages, onOpenPage = { selectedSettingsPage = it })
                    EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
                }
            }
        }
        if (displayedSettingsPage == "user_interface") item(key = "user_interface") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                val requiresAndroid12 = " (" + stringResource(Res.string.requires_android_12) + ")"
                SettingsSection(
                    title = stringResource(Res.string.settings_appearance),
                    icon = SimpIcons.Tune,
                ) {
                    val themeModeLabels =
                        listOf(
                            DataStoreManager.THEME_MODE_SYSTEM to stringResource(Res.string.theme_mode_system),
                            DataStoreManager.THEME_MODE_DARK to stringResource(Res.string.theme_mode_dark),
                            DataStoreManager.THEME_MODE_LIGHT to stringResource(Res.string.theme_mode_light),
                        )
                    SettingItem(
                        title = stringResource(Res.string.theme),
                        subtitle = themeModeLabels.firstOrNull { it.first == themeMode }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.theme) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = themeModeLabels.map { (it.first == themeMode) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            themeModeLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setThemeMode(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    BackgroundColorSetting(
                        selectedPreset = appBackgroundPreset,
                        onSelectPreset = sharedViewModel::setAppBackgroundPreset,
                    )
                    SettingsDivider()
                    AccentColorSetting(
                        colorSource = themeColorSource,
                        customColorHex = customThemeColorHex,
                        onSelectColor = sharedViewModel::setAccentColor,
                    )
                    SettingsDivider()
                    if (getPlatform() == Platform.Android) {
                        SettingItem(
                            title = stringResource(Res.string.enable_liquid_glass_effect),
                            subtitle = stringResource(Res.string.enable_liquid_glass_effect_description),
                            smallSubtitle = true,
                            switch = (enableLiquidGlass to { viewModel.setEnableLiquidGlass(it) }),
                            isEnable = getPlatform() == Platform.Android && !reduceVisualEffectsEnabled,
                        )
                        SettingsDivider()
                    }
                    SettingItem(
                        title = stringResource(Res.string.persistent_header_buttons),
                        subtitle = stringResource(Res.string.persistent_header_buttons_description),
                        isEnable = true,
                        switch = (fixedHeader to sharedViewModel::setFixedHeader),
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.liquid_glass_intensity),
                        subtitle = stringResource(Res.string.liquid_glass_intensity_description),
                        isEnable = (enableLiquidGlass || getPlatform() == Platform.Desktop) && !reduceVisualEffectsEnabled,
                        onClick = { navController.navigate(LiquidGlassSettingsDestination) },
                    )
                    SettingsDivider()
                    if (getPlatform() == Platform.Android) {
                        SettingItem(
                            title = stringResource(Res.string.slim_navigation_bar),
                            subtitle = stringResource(Res.string.slim_navigation_bar_description),
                            smallSubtitle = true,
                            isEnable = !reduceVisualEffectsEnabled,
                            switch = (
                                (slimNavigationBar == DataStoreManager.TRUE && !reduceVisualEffectsEnabled) to
                                    { enabled: Boolean -> sharedViewModel.setSlimNavigationBar(enabled) }
                            ),
                        )
                        SettingsDivider()
                    }
                    SettingItem(
                        title = stringResource(Res.string.reduce_visual_effects),
                        subtitle = stringResource(Res.string.reduce_visual_effects_description),
                        switch = (
                            reduceVisualEffectsEnabled to
                                { enabled: Boolean -> sharedViewModel.setReduceVisualEffects(enabled) }
                        ),
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_motion),
                    icon = SimpIcons.Sync,
                ) {
                    val pageTransitionLabels =
                        listOf(
                            DataStoreManager.PAGE_TRANSITION_FADE to stringResource(Res.string.transition_fade),
                            DataStoreManager.PAGE_TRANSITION_SLIDE to stringResource(Res.string.transition_slide),
                            DataStoreManager.PAGE_TRANSITION_SLIDE_V2 to stringResource(Res.string.transition_slide_v2),
                        )
                    SettingItem(
                        title = stringResource(Res.string.transition),
                        subtitle = pageTransitionLabels.firstOrNull { it.first == effectivePageTransitionStyle }?.second
                            ?: stringResource(Res.string.transition_slide_v2),
                        isEnable = !reduceVisualEffectsEnabled,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.transition) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = pageTransitionLabels.map {
                                                (it.first == effectivePageTransitionStyle) to it.second
                                            },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            pageTransitionLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setPageTransitionStyle(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.advanced_dialog_animations),
                        subtitle = stringResource(Res.string.advanced_dialog_animations_description),
                        switch = (
                            (advancedDialogAnimations == DataStoreManager.TRUE) to
                                { enabled: Boolean -> sharedViewModel.setAdvancedDialogAnimations(enabled) }
                        ),
                        isEnable = !reduceVisualEffectsEnabled,
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_player),
                    icon = SimpIcons.PlayCircle,
                ) {
                    // The Apple Music treatments ARE the blur — the frosted page behind the player, and
                    // the depth of field on the lyrics — and Modifier.blur is a documented no-op below
                    // Android 12, so on an older device they render as a flat, wrong-looking version of
                    // themselves. The requirement is spelled out on the option itself rather than left
                    // for the user to discover after switching.
                    SettingItem(
                        title = stringResource(Res.string.now_playing_style),
                        subtitle = stringResource(Res.string.now_playing_style_apple_music) + requiresAndroid12,
                    )
                }
            }
        }
        if (displayedSettingsPage == "content") item(key = "content") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_account),
                    icon = SimpIcons.PeopleAlt,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.youtube_account),
                        subtitle = stringResource(Res.string.manage_your_youtube_accounts),
                        onClick = {
                            onOpenYouTubeAccount()
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.sync_follow_to_youtube),
                        subtitle = stringResource(Res.string.sync_follow_to_youtube_description),
                        smallSubtitle = true,
                        switch = (syncFollowToYouTube to { viewModel.setSyncFollowToYouTube(it) }),
                        // Writing to someone's YouTube account needs a session, so the row is dead
                        // while signed out. Clearing the stored flag is NOT done from here: the reset
                        // belongs to the logout itself (SettingsViewModel.setUsedAccount /
                        // logOutAllYouTube), which runs whether or not Settings is ever opened.
                        isEnable = loggedIn == DataStoreManager.TRUE,
                    )
                    SettingItem(
                        title = stringResource(Res.string.send_back_listening_data_to_google),
                        subtitle =
                            stringResource(
                                Res.string
                                    .upload_your_listening_history_to_youtube_music_server_it_will_make_yt_music_recommendation_system_better_working_only_if_logged_in,
                            ),
                        smallSubtitle = true,
                        switch = (sendData to { viewModel.setSendBackToGoogle(it) }),
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_discovery),
                    icon = SimpIcons.Search,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.language),
                        subtitle = if (language.isNullOrBlank()) stringResource(Res.string.theme_mode_system)
                            else SUPPORTED_LANGUAGE.getLanguageFromCode(language),
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.language) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                listOf(language.isNullOrBlank() to runBlocking { getString(Res.string.theme_mode_system) }) +
                                                    SUPPORTED_LANGUAGE.items.map {
                                                        (!language.isNullOrBlank() && it.toString() == SUPPORTED_LANGUAGE.getLanguageFromCode(language)) to it.toString()
                                                    },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            val code = if (selected == runBlocking { getString(Res.string.theme_mode_system) }) ""
                                                else SUPPORTED_LANGUAGE.getCodeFromLanguage(selected)
                                            viewModel.setBasicAlertData(
                                                SettingBasicAlertState(
                                                    title = runBlocking { getString(Res.string.warning) },
                                                    message = runBlocking { getString(Res.string.change_language_warning) },
                                                    confirm =
                                                        runBlocking { getString(Res.string.change) } to {
                                                            viewModel.setBasicAlertData(null)
                                                            viewModel.changeLanguage(code)
                                                        },
                                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                                ),
                                            )
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.content_country),
                        subtitle = location ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.content_country) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                SUPPORTED_LOCATION.items.map { item ->
                                                    (item.toString() == location) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            viewModel.changeLocation(
                                                state.selectOne?.getSelected() ?: "US",
                                            )
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.play_explicit_content),
                        subtitle = stringResource(Res.string.play_explicit_content_description),
                        switch = (explicitContentEnabled to { viewModel.setExplicitContentEnabled(it) }),
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_streaming),
                    icon = SimpIcons.PlayCircle,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.tracks_quality),
                        subtitle = quality ?: "",
                        smallSubtitle = true,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.tracks_quality) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                QUALITY.items.map { item ->
                                                    (item.toString() == quality) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            viewModel.changeQuality(state.selectOne?.getSelected())
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.tracks_download_quality),
                        subtitle = downloadQuality ?: "",
                        smallSubtitle = true,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.tracks_download_quality) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                QUALITY.items.map { item ->
                                                    (item.toString() == downloadQuality) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            state.selectOne?.getSelected()?.let { viewModel.setDownloadQuality(it) }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.video_quality),
                        subtitle = videoQuality,
                        smallSubtitle = true,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.video_quality) },
                                    selectOne = SettingAlertState.SelectData(
                                        listSelect = VIDEO_QUALITY.items.map { item ->
                                            (item.toString() == videoQuality) to item.toString()
                                        },
                                    ),
                                    confirm = runBlocking { getString(Res.string.change) } to { state ->
                                        state.selectOne?.getSelected()?.let { viewModel.setVideoQuality(it) }
                                    },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.video_download_quality),
                        subtitle = videoDownloadQuality,
                        smallSubtitle = true,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.video_download_quality) },
                                    selectOne = SettingAlertState.SelectData(
                                        listSelect = VIDEO_QUALITY.items.map { item ->
                                            (item.toString() == videoDownloadQuality) to item.toString()
                                        },
                                    ),
                                    confirm = runBlocking { getString(Res.string.change) } to { state ->
                                        state.selectOne?.getSelected()?.let { viewModel.setVideoDownloadQuality(it) }
                                    },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.radio_audio_only),
                        subtitle = stringResource(Res.string.radio_audio_only_description),
                        smallSubtitle = true,
                        switch = (radioAudioOnly to { viewModel.setRadioAudioOnly(it) }),
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_offline),
                    icon = SimpIcons.Download,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.auto_download_liked_songs),
                        subtitle = stringResource(Res.string.auto_download_liked_songs_description),
                        smallSubtitle = true,
                        switch = (autoDownloadLikedSongs to { viewModel.setAutoDownloadLikedSongs(it) }),
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.keep_your_youtube_playlist_offline),
                        subtitle = stringResource(Res.string.keep_your_youtube_playlist_offline_description),
                        switch = (keepYoutubePlaylistOffline to { viewModel.setKeepYouTubePlaylistOffline(it) }),
                    )
                    /*
                    SettingItem(
                        title = stringResource(Res.string.combine_local_and_youtube_liked_songs),
                        subtitle = stringResource(Res.string.combine_local_and_youtube_liked_songs_description),
                        switch = (combineLocalAndYouTubeLiked to { viewModel.setCombineLocalAndYouTubeLiked(it) })
                    )
                     */
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_network),
                    icon = SimpIcons.Sync,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.proxy),
                        subtitle = stringResource(Res.string.proxy_description),
                        switch = (usingProxy to { viewModel.setUsingProxy(it) }),
                    )
                    Crossfade(usingProxy) { it ->
                        if (it) {
                            SettingsNested {
                                SettingItem(
                    title = stringResource(Res.string.proxy_type),
                    subtitle =
                        when (proxyType) {
                            DataStoreManager.ProxyType.PROXY_TYPE_HTTP -> stringResource(Res.string.http)
                            DataStoreManager.ProxyType.PROXY_TYPE_SOCKS -> stringResource(Res.string.socks)
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.proxy_type) },
                                selectOne =
                                    SettingAlertState.SelectData(
                                        listSelect =
                                            listOf(
                                                (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_HTTP) to
                                                    runBlocking {
                                                        getString(
                                                            Res.string.http,
                                                        )
                                                    },
                                                (proxyType == DataStoreManager.ProxyType.PROXY_TYPE_SOCKS) to
                                                    runBlocking { getString(Res.string.socks) },
                                            ),
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setProxy(
                                            if (state.selectOne?.getSelected() == runBlocking { getString(Res.string.socks) }) {
                                                DataStoreManager.ProxyType.PROXY_TYPE_SOCKS
                                            } else {
                                                DataStoreManager.ProxyType.PROXY_TYPE_HTTP
                                            },
                                            proxyHost,
                                            proxyPort,
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                                )
                                SettingItem(
                    title = stringResource(Res.string.proxy_host),
                    subtitle = proxyHost,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.proxy_host) },
                                message = runBlocking { getString(Res.string.proxy_host_message) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(Res.string.proxy_host) },
                                        value = proxyHost,
                                        verifyCodeBlock = {
                                            isValidProxyHost(it) to runBlocking { getString(Res.string.invalid_host) }
                                        },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setProxy(
                                            proxyType,
                                            state.textField?.value ?: "",
                                            proxyPort,
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                                )
                                SettingItem(
                    title = stringResource(Res.string.proxy_port),
                    subtitle = proxyPort.toString(),
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.proxy_port) },
                                message = runBlocking { getString(Res.string.proxy_port_message) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(Res.string.proxy_port) },
                                        value = proxyPort.toString(),
                                        verifyCodeBlock = {
                                            (it.toIntOrNull() != null) to runBlocking { getString(Res.string.invalid_port) }
                                        },
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setProxy(
                                            proxyType,
                                            proxyHost,
                                            state.textField?.value?.toIntOrNull() ?: 0,
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                                )
                                SettingItem(
                    title = stringResource(Res.string.proxy_username),
                    subtitle = proxyUsername,
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.proxy_username) },
                                message = runBlocking { getString(Res.string.proxy_username_message) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(Res.string.proxy_username) },
                                        value = proxyUsername,
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setProxyCredentials(
                                            state.textField?.value ?: "",
                                            proxyPassword,
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                                )
                                SettingItem(
                    title = stringResource(Res.string.proxy_password),
                    subtitle =
                        if (proxyPassword.isEmpty()) {
                            ""
                        } else {
                            "\u2022".repeat(proxyPassword.length)
                        },
                    onClick = {
                        viewModel.setAlertData(
                            SettingAlertState(
                                title = runBlocking { getString(Res.string.proxy_password) },
                                message = runBlocking { getString(Res.string.proxy_password_message) },
                                textField =
                                    SettingAlertState.TextFieldData(
                                        label = runBlocking { getString(Res.string.proxy_password) },
                                        value = proxyPassword,
                                    ),
                                confirm =
                                    runBlocking { getString(Res.string.change) } to { state ->
                                        viewModel.setProxyCredentials(
                                            proxyUsername,
                                            state.textField?.value ?: "",
                                        )
                                    },
                                dismiss = runBlocking { getString(Res.string.cancel) },
                            ),
                        )
                    },
                                )
                            }
                        }
                    }
                }
            }
        }

        if (getPlatform() == Platform.Android && displayedSettingsPage == "audio") {
            item(key = "audio") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_volume),
                    icon = SimpIcons.GraphicEq,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.normalize_volume),
                        subtitle = stringResource(Res.string.balance_media_loudness),
                        switch = (normalizeVolume to { viewModel.setNormalizeVolume(it) }),
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.skip_silent),
                        subtitle = stringResource(Res.string.skip_no_music_part),
                        switch = (skipSilent to { viewModel.setSkipSilent(it) }),
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_equalizer),
                    icon = SimpIcons.Tune,
                ) {
                    val equalizerTypeLabels =
                        listOf(
                            DataStoreManager.EQUALIZER_TYPE_BUILT_IN to stringResource(Res.string.equalizer_type_built_in),
                            DataStoreManager.EQUALIZER_TYPE_SYSTEM to stringResource(Res.string.equalizer_type_system),
                        )
                    SettingItem(
                        title = stringResource(Res.string.equalizer_type),
                        subtitle = equalizerTypeLabels.firstOrNull { it.first == equalizerType }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.equalizer_type) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = equalizerTypeLabels.map { (it.first == equalizerType) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            equalizerTypeLabels.firstOrNull { it.second == selected }?.first?.let {
                                                viewModel.setEqualizerType(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    AnimatedVisibility(visible = equalizerType == DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                        SettingsNested {
                            SettingItem(
                                title = stringResource(Res.string.open_system_equalizer),
                                subtitle =
                                    if (castState.isRemote) {
                                        stringResource(Res.string.not_available_while_casting)
                                    } else {
                                        stringResource(Res.string.use_your_system_equalizer)
                                    },
                                isEnable = !castState.isRemote,
                                onClick = {
                                    coroutineScope.launch {
                                        resultLauncher.launch()
                                    }
                                },
                            )
                        }
                    }
                }
            }
            }
        }
        if (displayedSettingsPage == "playback") item(key = "playback") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_effects),
                    icon = SimpIcons.GraphicEq,
                ) {
                    // Under Playback rather than Audio because that whole group sits inside an
                    // Android-only branch — "Open system equalizer" is an Android feature — and this
                    // one is on both platforms: mpv's `af` chain on Desktop, an AudioProcessor in the
                    // Media3 sink on Android, driven from the same stored curve.
                    AnimatedVisibility(visible = getPlatform() != Platform.Android || equalizerType != DataStoreManager.EQUALIZER_TYPE_SYSTEM) {
                        Column {
                            SettingItem(
                                title = stringResource(Res.string.equalizer),
                                subtitle = stringResource(Res.string.equalizer_description),
                                smallSubtitle = true,
                                switch = (equalizerEnabled to { viewModel.setEqualizerEnabled(it) }),
                            )
                            // Only while on. A curve that visibly does nothing is worse than no curve —
                            // and the stored bands survive the switch, so turning it back on returns to
                            // the shape the user built rather than to flat.
                            AnimatedVisibility(visible = equalizerEnabled) {
                                Box(Modifier.padding(horizontal = 12.dp)) { EqualizerSection() }
                            }
                        }
                    }
                    // Beside the equalizer rather than in its own group: all three are the same kind of
                    // thing — one stored setting reshaping the audio on both backends — and a user
                    // hunting for "reverb" looks wherever the sound settings are, not under a heading
                    // they have to guess.
                    SettingItem(
                        title = stringResource(Res.string.audio_delay),
                        subtitle = stringResource(Res.string.audio_delay_description),
                        smallSubtitle = true,
                        switch = (delayEnabled to { viewModel.setDelayEnabled(it) }),
                    )
                    // Only while on, like the curve — and the three values survive the switch, so
                    // turning it back on returns to the echo the user dialled in.
                    AnimatedVisibility(visible = delayEnabled) {
                        Box(Modifier.padding(horizontal = 12.dp)) { DelaySection() }
                    }
                    SettingItem(
                        title = stringResource(Res.string.audio_reverb),
                        subtitle = stringResource(Res.string.audio_reverb_description),
                        smallSubtitle = true,
                        switch = (reverbEnabled to { viewModel.setReverbEnabled(it) }),
                    )
                    // Same again: the room and the wet level outlive the switch.
                    AnimatedVisibility(visible = reverbEnabled) {
                        Box(Modifier.padding(horizontal = 12.dp)) { ReverbSection() }
                    }
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_session),
                    icon = SimpIcons.History,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.save_playback_state),
                        subtitle = stringResource(Res.string.save_shuffle_and_repeat_mode),
                        switch = (savePlaybackState to { viewModel.setSavedPlaybackState(it) }),
                    )
                    if (getPlatform() == Platform.Android) {
                        SettingItem(
                            title = stringResource(Res.string.kill_service_on_exit),
                            subtitle = stringResource(Res.string.kill_service_on_exit_description),
                            switch = (killServiceOnExit to { viewModel.setKillServiceOnExit(it) }),
                        )
                    }
                }
            }
        }
        // Crossfade Settings (all platforms)
        if (displayedSettingsPage == "playback") item(key = "crossfade_settings") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_crossfade),
                    icon = SimpIcons.Shuffle,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.crossfade),
                        subtitle =
                            if (castState.isRemote) {
                                stringResource(Res.string.not_available_while_casting)
                            } else {
                                stringResource(Res.string.crossfade_description)
                            },
                        smallSubtitle = true,
                        switch = (crossfadeEnabled to { viewModel.setCrossfadeEnabled(it) }),
                        isEnable = !castState.isRemote,
                    )
                    AnimatedVisibility(visible = crossfadeEnabled) {
                        SettingsNested {
                            SettingItem(
                                title = stringResource(Res.string.crossfade_duration),
                                subtitle =
                                    if (castState.isRemote) {
                                        stringResource(Res.string.not_available_while_casting)
                                    } else if (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) {
                                        stringResource(Res.string.crossfade_auto)
                                    } else {
                                        "${crossfadeDuration / 1000}s"
                                    },
                                isEnable = !castState.isRemote,
                                onClick = {
                                    viewModel.setAlertData(
                                        SettingAlertState(
                                            title = runBlocking { getString(Res.string.crossfade_duration) },
                                            selectOne =
                                                SettingAlertState.SelectData(
                                                    listSelect =
                                                        listOf(
                                                            (crossfadeDuration == DataStoreManager.CROSSFADE_DURATION_AUTO) to
                                                                runBlocking { getString(Res.string.crossfade_auto) },
                                                            (crossfadeDuration == 1000) to "1s",
                                                            (crossfadeDuration == 2000) to "2s",
                                                            (crossfadeDuration == 3000) to "3s",
                                                            (crossfadeDuration == 5000) to "5s",
                                                            (crossfadeDuration == 8000) to "8s",
                                                            (crossfadeDuration == 10000) to "10s",
                                                            (crossfadeDuration == 12000) to "12s",
                                                            (crossfadeDuration == 15000) to "15s",
                                                            (crossfadeDuration == 20000) to "20s",
                                                            (crossfadeDuration == 30000) to "30s",
                                                        ),
                                                ),
                                            confirm =
                                                runBlocking { getString(Res.string.change) } to { state ->
                                                    val duration =
                                                        when (state.selectOne?.getSelected()) {
                                                            runBlocking {
                                                                getString(
                                                                    Res.string.crossfade_auto,
                                                                )
                                                            },
                                                            -> DataStoreManager.CROSSFADE_DURATION_AUTO
                                                            "1s" -> 1000
                                                            "2s" -> 2000
                                                            "3s" -> 3000
                                                            "5s" -> 5000
                                                            "8s" -> 8000
                                                            "10s" -> 10000
                                                            "12s" -> 12000
                                                            "15s" -> 15000
                                                            "20s" -> 20000
                                                            "30s" -> 30000
                                                            else -> 5000
                                                        }
                                                    viewModel.setCrossfadeDuration(duration)
                                                },
                                            dismiss = runBlocking { getString(Res.string.cancel) },
                                        ),
                                    )
                                },
                            )
                    //                        if (getPlatform() == Platform.Android) {
                            SettingItem(
                                title = stringResource(Res.string.crossfade_dj_mode),
                                subtitle =
                                    if (castState.isRemote) {
                                        stringResource(Res.string.not_available_while_casting)
                                    } else {
                                        stringResource(Res.string.crossfade_dj_mode_description)
                                    },
                                smallSubtitle = true,
                                switch = ((crossfadeDjMode) to { viewModel.setCrossfadeDjMode(it) }),
                                isEnable = !castState.isRemote,
                            )
                            SettingItem(
                                title = stringResource(Res.string.crossfade_skip_album),
                                subtitle =
                                    if (castState.isRemote) {
                                        stringResource(Res.string.not_available_while_casting)
                                    } else {
                                        stringResource(Res.string.crossfade_skip_album_description)
                                    },
                                smallSubtitle = true,
                                switch = ((crossfadeSkipAlbum) to { viewModel.setCrossfadeSkipAlbum(it) }),
                                isEnable = !castState.isRemote,
                            )
                    //                        }
                        }
                    }
                }
            }
        }
        // Deliberately not part of "storage" further down, which is Android-only: tracking and the
        // rows it leaves behind exist on Desktop just the same. The switch that produces the history
        // and the button that erases it belong together.
        if (displayedSettingsPage == "listening_history") item(key = "listening_history") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_activity),
                    icon = SimpIcons.History,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.local_tracking_title),
                        subtitle = stringResource(Res.string.local_tracking_description),
                        switch = (localTrackingEnabled to { viewModel.setLocalTrackingEnabled(it) }),
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.clear_listening_history),
                        subtitle = stringResource(Res.string.clear_listening_history_description),
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_listening_history) },
                                    message = runBlocking { getString(Res.string.clear_listening_history_confirm) },
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearListeningHistory()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                }
            }
        }
        if (displayedSettingsPage == "lyrics") item(key = "lyrics_display") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                val requiresAndroid12 = " (" + stringResource(Res.string.requires_android_12) + ")"
                SettingsSection(
                    title = stringResource(Res.string.settings_lyrics_display),
                    icon = SimpIcons.Lyrics,
                ) {
                    val lyricsStyleLabels =
                        listOf(
                            DataStoreManager.LYRICS_STYLE_ENHANCED to stringResource(Res.string.lyrics_style_enhanced),
                            DataStoreManager.LYRICS_STYLE_CLASSIC to stringResource(Res.string.lyrics_style_classic),
                        ) +
                            if (isLyricsBlurSupported()) {
                                listOf(
                                    DataStoreManager.LYRICS_STYLE_APPLE_MUSIC to
                                        stringResource(Res.string.lyrics_style_apple_music) + requiresAndroid12,
                                )
                            } else {
                                emptyList()
                            }
                    SettingItem(
                        title = stringResource(Res.string.lyrics_style),
                        subtitle = lyricsStyleLabels.firstOrNull { it.first == lyricsStyle }?.second ?: "",
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.lyrics_style) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = lyricsStyleLabels.map { (it.first == lyricsStyle) to it.second },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selected = state.selectOne?.getSelected()
                                            lyricsStyleLabels.firstOrNull { it.second == selected }?.first?.let {
                                                sharedViewModel.setLyricsStyle(it)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )

                    fun formatPointSize(sizeTenthsPt: Int): String =
                        if (sizeTenthsPt % 10 == 0) {
                            "${sizeTenthsPt / 10} pt"
                        } else {
                            "${sizeTenthsPt / 10}.${sizeTenthsPt % 10} pt"
                        }
                    SettingItem(
                        title = stringResource(Res.string.lyrics_font_size),
                        subtitle = formatPointSize(selectedLyricsFontSize.toInt()),
                        otherView = {
                            Slider(
                                value = selectedLyricsFontSize,
                                onValueChange = { selectedLyricsFontSize = it },
                                onValueChangeFinished = {
                                    sharedViewModel.setLyricsFontSizeTenthsPt(selectedLyricsFontSize.toInt())
                                },
                                valueRange = 100f..360f,
                                steps = 51,
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.translation_lyrics_font_size),
                        subtitle = formatPointSize(selectedTranslatedLyricsFontSize.toInt()),
                        otherView = {
                            Slider(
                                value = selectedTranslatedLyricsFontSize,
                                onValueChange = { selectedTranslatedLyricsFontSize = it },
                                onValueChangeFinished = {
                                    sharedViewModel.setTranslatedLyricsFontSizeTenthsPt(selectedTranslatedLyricsFontSize.toInt())
                                },
                                valueRange = 80f..300f,
                                steps = 43,
                            )
                        },
                    )

                    // Independent of BOTH style settings, and not gated on Android 12: this changes
                    // what the words SAY, not how they are drawn, so it applies to every style on
                    // every version. Sits next to them because a user looking for "something about
                    // lyrics" looks in one place.
                    val romanizationLabels =
                        listOf(
                            RomanizationLanguage.JAPANESE to stringResource(Res.string.romanization_japanese),
                            RomanizationLanguage.KOREAN to stringResource(Res.string.romanization_korean),
                            RomanizationLanguage.CHINESE to stringResource(Res.string.romanization_chinese),
                            RomanizationLanguage.HINDI to stringResource(Res.string.romanization_hindi),
                            RomanizationLanguage.PUNJABI to stringResource(Res.string.romanization_punjabi),
                            RomanizationLanguage.RUSSIAN to stringResource(Res.string.romanization_russian),
                            RomanizationLanguage.UKRAINIAN to stringResource(Res.string.romanization_ukrainian),
                            RomanizationLanguage.SERBIAN to stringResource(Res.string.romanization_serbian),
                            RomanizationLanguage.BULGARIAN to stringResource(Res.string.romanization_bulgarian),
                            RomanizationLanguage.BELARUSIAN to stringResource(Res.string.romanization_belarusian),
                            RomanizationLanguage.KYRGYZ to stringResource(Res.string.romanization_kyrgyz),
                            RomanizationLanguage.MACEDONIAN to stringResource(Res.string.romanization_macedonian),
                        )
                    val romanizationSelected = RomanizationLanguage.parse(romanizationStored)
                    SettingItem(
                        title = stringResource(Res.string.lyrics_romanization),
                        // Two different jobs for one line. Off, the row has to explain what the
                        // feature IS — nobody guesses "romanization" from the title alone. On, the only
                        // question worth answering at a glance is which of the twelve are picked, and
                        // the explanation has served its purpose.
                        subtitle =
                            if (romanizationSelected.isEmpty()) {
                                stringResource(Res.string.lyrics_romanization_description)
                            } else {
                                val selectedNames =
                                    romanizationLabels.filter { it.first in romanizationSelected }.joinToString(", ") { it.second }
                                // Japanese is the one language with a dictionary pack to fetch; while
                                // that is in flight — or has failed — the row says so, instead of
                                // listing Japanese as if it were already live.
                                when {
                                    RomanizationLanguage.JAPANESE !in romanizationSelected -> selectedNames
                                    japaneseDictionaryState == RomanizationDictionaryState.DOWNLOADING ->
                                        "$selectedNames — ${stringResource(Res.string.romanization_japanese_dict_downloading)}"
                                    japaneseDictionaryState == RomanizationDictionaryState.FAILED ->
                                        "$selectedNames — ${stringResource(Res.string.romanization_japanese_dict_failed)}"
                                    else -> selectedNames
                                }
                            },
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.lyrics_romanization) },
                                    // NO `message` here, deliberately. The dialog picks its body with
                                    // an if/else-if chain that tests `message` FIRST, and that branch
                                    // renders only the text and an optional textField — a multipleSelect
                                    // passed alongside it is never reached, so the dialog came up with
                                    // the description and no languages at all.
                                    multipleSelect =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                romanizationLabels.map { (language, label) ->
                                                    (language in romanizationSelected) to label
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.save) } to { state ->
                                            val chosen = state.multipleSelect?.getListSelected().orEmpty()
                                            val languages =
                                                romanizationLabels.filter { it.second in chosen }.map { it.first }.toSet()
                                            sharedViewModel.setRomanizationLanguages(languages)
                                            // Japanese needs its dictionary pack on disk. A no-op when
                                            // it is already there (or bundled, as on Desktop) — and the
                                            // retry after a FAILED attempt is simply confirming again.
                                            if (RomanizationLanguage.JAPANESE in languages) {
                                                viewModel.downloadJapaneseDictionaryIfNeeded()
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                }
            }
        }
        if (displayedSettingsPage == "lyrics") item(key = "lyrics") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_timing),
                    icon = SimpIcons.AccessAlarm,
                ) {
                    LyricsOffsetControl(
                        offsetMs = lyricsOffsetMs,
                        onAdjustOffset = viewModel::adjustLyricsOffsetMs,
                        showHint = true,
                        onDefault = { viewModel.setLyricsOffsetMs(0) },
                        scopeHint = stringResource(Res.string.lyrics_offset_global_scope),
                    )
                }
            }
        }
        if (displayedSettingsPage == "lyrics") item(key = "ai_lyrics") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.ai_lyrics_translation),
                    icon = SimpIcons.Translate,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.use_ai_translation),
                        subtitle = stringResource(Res.string.use_ai_translation_description),
                        switch = (showLyricsTranslationButton to { viewModel.setLyricsTranslationButton(it) }),
                        isEnable = true,
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.translation_language),
                        subtitle = selectedTranslationName,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.translation_language) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect = translationLanguageOptions.map { (_, name) ->
                                                (name == selectedTranslationName) to name
                                            },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            val selectedName = state.selectOne?.getSelected()
                                            translationLanguageOptions.firstOrNull { it.second == selectedName }?.let { (code, _) ->
                                                viewModel.setTranslationLanguage(code)
                                            }
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                        isEnable = true,
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.ai_provider),
                        subtitle =
                            aiProviderName,
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.ai_provider) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                listOf(
                                                    (aiProvider == DataStoreManager.AI_PROVIDER_OPENAI) to
                                                        runBlocking { getString(Res.string.openai) },
                                                    (aiProvider == DataStoreManager.AI_PROVIDER_GEMINI) to
                                                        runBlocking { getString(Res.string.gemini) },
                                                    (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) to "OpenRouter",
                                                    (aiProvider == DataStoreManager.AI_PROVIDER_GROQ) to "Groq",
                                                    (aiProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) to
                                                        runBlocking { getString(Res.string.openai_api_compatible) },
                                                ),
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            viewModel.setAIProvider(
                                                when (state.selectOne?.getSelected()) {
                                                    runBlocking { getString(Res.string.openai) } -> DataStoreManager.AI_PROVIDER_OPENAI
                                                    runBlocking { getString(Res.string.gemini) } -> DataStoreManager.AI_PROVIDER_GEMINI
                                                    "OpenRouter" -> DataStoreManager.AI_PROVIDER_OPENROUTER
                                                    "Groq" -> DataStoreManager.AI_PROVIDER_GROQ
                                                    runBlocking {
                                                        getString(
                                                            Res.string.openai_api_compatible,
                                                        )
                                                    },
                                                    -> DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI

                                                    else -> DataStoreManager.AI_PROVIDER_OPENAI
                                                },
                                            )
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingsNested {
                        SettingItem(
                            title = when (aiProvider) {
                                DataStoreManager.AI_PROVIDER_OPENROUTER -> "OpenRouter API Key"
                                DataStoreManager.AI_PROVIDER_GROQ -> "Groq API Key"
                                else -> stringResource(Res.string.ai_api_key)
                            },
                            subtitle = if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) {
                                if (openRouterApiKey.isNotEmpty()) "XXXXXXXXXX" else "N/A"
                            } else if (isHasApiKey) "XXXXXXXXXX" else "N/A",
                            onClick = {
                                viewModel.setAlertData(
                    SettingAlertState(
                        title = when (aiProvider) {
                            DataStoreManager.AI_PROVIDER_OPENROUTER -> "OpenRouter API Key"
                            DataStoreManager.AI_PROVIDER_GROQ -> "Groq API Key"
                            else -> runBlocking { getString(Res.string.ai_api_key) }
                        },
                        textField =
                            SettingAlertState.TextFieldData(
                                label = when (aiProvider) {
                                    DataStoreManager.AI_PROVIDER_OPENROUTER -> "OpenRouter API Key"
                                    DataStoreManager.AI_PROVIDER_GROQ -> "Groq API Key"
                                    else -> runBlocking { getString(Res.string.ai_api_key) }
                                },
                                value = "",
                                verifyCodeBlock = {
                                    (it.isNotEmpty()) to runBlocking { getString(Res.string.invalid_api_key) }
                                },
                            ),
                        message = "",
                        confirm =
                            runBlocking { getString(Res.string.set) } to { state ->
                                if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) {
                                    viewModel.setOpenRouterApiKey(state.textField?.value ?: "")
                                } else {
                                    viewModel.setAIApiKey(state.textField?.value ?: "")
                                }
                            },
                        dismiss = runBlocking { getString(Res.string.cancel) },
                    ),
                                )
                            },
                        )
                        if (aiProvider != DataStoreManager.AI_PROVIDER_GROQ) SettingItem(
                            title = if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) runBlocking { getString(Res.string.openrouter_model_id) } else stringResource(Res.string.custom_ai_model_id),
                            subtitle = (if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) openRouterModelId else customModelId).ifEmpty {
                                if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) runBlocking { getString(Res.string.required_field) } else stringResource(Res.string.default_models)
                            },
                            onClick = {
                                viewModel.setAlertData(
                    SettingAlertState(
                        title = if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) runBlocking { getString(Res.string.openrouter_model_id) } else runBlocking { getString(Res.string.custom_ai_model_id) },
                        textField =
                            SettingAlertState.TextFieldData(
                                label = if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) runBlocking { getString(Res.string.openrouter_model_id) } else runBlocking { getString(Res.string.custom_ai_model_id) },
                                value = if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) openRouterModelId else customModelId,
                                verifyCodeBlock = {
                                    (it.isNotEmpty() && !it.contains(" ")) to runBlocking { getString(Res.string.invalid) }
                                },
                            ),
                        message = runBlocking { getString(Res.string.custom_model_id_messages) },
                        confirm =
                            runBlocking { getString(Res.string.set) } to { state ->
                                if (aiProvider == DataStoreManager.AI_PROVIDER_OPENROUTER) {
                                    viewModel.setOpenRouterModelId(state.textField?.value ?: "")
                                } else {
                                    viewModel.setCustomModelId(state.textField?.value ?: "")
                                }
                            },
                        dismiss = runBlocking { getString(Res.string.cancel) },
                    ),
                                )
                            },
                        )
                        // Custom OpenAI Base URL - only show when Custom OpenAI is selected
                        if (aiProvider == DataStoreManager.AI_PROVIDER_CUSTOM_OPENAI) {
                            SettingItem(
                                title = runBlocking { getString(Res.string.custom_base_url) },
                                subtitle = customOpenAIBaseUrl.ifEmpty { "https://api.openai.com/v1/" },
                                onClick = {
                    viewModel.setAlertData(
                        SettingAlertState(
                            title = runBlocking { getString(Res.string.custom_base_url) },
                            textField =
                                SettingAlertState.TextFieldData(
                                    label = runBlocking { getString(Res.string.base_url) },
                                    value = customOpenAIBaseUrl,
                                    verifyCodeBlock = {
                                        (it.isEmpty() || it.startsWith("http")) to runBlocking { getString(Res.string.invalid_url_format) }
                                    },
                                ),
                            message = runBlocking { getString(Res.string.custom_base_url_prompt) },
                            confirm =
                                runBlocking { getString(Res.string.set) } to { state ->
                                    viewModel.setCustomOpenAIBaseUrl(state.textField?.value ?: "")
                                },
                            dismiss = runBlocking { getString(Res.string.cancel) },
                        ),
                    )
                                },
                            )
                            SettingItem(
                                title = runBlocking { getString(Res.string.custom_headers) },
                                subtitle = if (customOpenAIHeaders.isNotEmpty()) runBlocking { getString(Res.string.configured) } else runBlocking { getString(Res.string.not_set) },
                                onClick = {
                    viewModel.setAlertData(
                        SettingAlertState(
                            title = runBlocking { getString(Res.string.custom_headers_json) },
                            textField =
                                SettingAlertState.TextFieldData(
                                    label = runBlocking { getString(Res.string.headers_json) },
                                    value = customOpenAIHeaders,
                                    verifyCodeBlock = { input ->
                                        if (input.isEmpty()) {
                                            true to null
                                        } else {
                                            try {
                                                // Simple validation: check if it looks like JSON
                                                val trimmed = input.trim()
                                                (trimmed.startsWith("{") && trimmed.endsWith("}")) to runBlocking { getString(Res.string.invalid_json_format) }
                                            } catch (e: Exception) {
                                                false to runBlocking { getString(Res.string.invalid_json_format) }
                                            }
                                        }
                                    },
                                ),
                            message = runBlocking { getString(Res.string.custom_headers_prompt) },
                            confirm =
                                runBlocking { getString(Res.string.set) } to { state ->
                                    viewModel.setCustomOpenAIHeaders(state.textField?.value ?: "")
                                },
                            dismiss = runBlocking { getString(Res.string.cancel) },
                        ),
                    )
                                },
                            )
                        }
                    }
                }
            }
        }
        // Hidden entirely when the build carries no Last.fm credentials — a FOSS build, or a full
        // build whose local.properties has no key.
        if (viewModel.lastfmAvailable && displayedSettingsPage == "lastfm") {
            item(key = "lastfm") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_lastfm),
                    icon = SimpIcons.Sync,
                ) {
                    SettingItem(
                        title =
                            if (lastfmLoggedIn) {
                                stringResource(Res.string.log_out_from_lastfm)
                            } else {
                                stringResource(Res.string.log_in_to_lastfm)
                            },
                        subtitle =
                            if (lastfmLoggedIn) {
                                stringResource(Res.string.logged_in_as, lastfmUsername)
                            } else {
                                stringResource(Res.string.intro_login_to_lastfm)
                            },
                        onClick = {
                            if (lastfmLoggedIn) {
                                viewModel.confirmLogOut(
                                    confirmLabel = runBlocking { getString(Res.string.log_out_from_lastfm) },
                                ) { viewModel.logOutLastfm() }
                            } else {
                                navController.navigate(LastfmLoginDestination)
                            }
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.enable_scrobbling),
                        subtitle = stringResource(Res.string.scrobbling_info),
                        switch = (lastfmScrobbleEnabled to { viewModel.setLastfmScrobbleEnabled(it) }),
                        isEnable = lastfmLoggedIn,
                    )
                }
            }
            }
        }
        if (getPlatform() == Platform.Android && displayedSettingsPage == "storage") {
            item(key = "storage") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_cache),
                    icon = SimpIcons.Delete,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.player_cache),
                        subtitle = "${playerCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_player_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearPlayerCache()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.downloaded_cache),
                        subtitle = "${downloadedCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_downloaded_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearDownloadedCache()
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.thumbnail_cache),
                        subtitle = "${thumbnailCache.bytesToMB()} MB",
                        onClick = {
                            viewModel.setBasicAlertData(
                                SettingBasicAlertState(
                                    title = runBlocking { getString(Res.string.clear_thumbnail_cache) },
                                    message = null,
                                    confirm =
                                        runBlocking { getString(Res.string.clear) } to {
                                            viewModel.clearThumbnailCache(platformContext)
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.limit_player_cache),
                        subtitle = LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache).toString(),
                        onClick = {
                            viewModel.setAlertData(
                                SettingAlertState(
                                    title = runBlocking { getString(Res.string.limit_player_cache) },
                                    selectOne =
                                        SettingAlertState.SelectData(
                                            listSelect =
                                                LIMIT_CACHE_SIZE.items.map { item ->
                                                    (item == LIMIT_CACHE_SIZE.getItemFromData(limitPlayerCache)) to item.toString()
                                                },
                                        ),
                                    confirm =
                                        runBlocking { getString(Res.string.change) } to { state ->
                                            viewModel.setPlayerCacheLimit(
                                                LIMIT_CACHE_SIZE.getDataFromItem(state.selectOne?.getSelected()),
                                            )
                                        },
                                    dismiss = runBlocking { getString(Res.string.cancel) },
                                ),
                            )
                        },
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_device),
                    icon = SimpIcons.Info,
                ) {
                    Box(
                        Modifier.padding(
                            horizontal = 24.dp,
                            vertical = 16.dp,
                        ),
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(0.dp),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .onGloballyPositioned { layoutCoordinates ->
                                        with(localDensity) {
                                            width =
                                                layoutCoordinates.size.width
                                                    .toDp()
                                                    .value
                                                    .toInt()
                                        }
                                    },
                        ) {
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.otherApp * width).dp,
                                            ).background(
                                                md_theme_dark_primary,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.downloadCache * width).dp,
                                            ).background(
                                                Color(0xD540FF17),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                ((fraction.playerCache + fraction.canvasCache) * width).dp,
                                            ).background(
                                                Color(0xD5FFFF00),
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.thumbCache * width).dp,
                                            ).background(
                                                Color.Magenta,
                                            ).fillMaxHeight(),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.appDatabase * width).dp,
                                            ).background(
                                                Color.White,
                                            ),
                                )
                            }
                            item {
                                Box(
                                    modifier =
                                        Modifier
                                            .width(
                                                (fraction.freeSpace * width).dp,
                                            ).background(
                                                Color.DarkGray,
                                            ).fillMaxHeight(),
                                )
                            }
                        }
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    md_theme_dark_primary,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.other_app), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Green,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.downloaded_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Yellow,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.player_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.Magenta,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.thumbnail_cache), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.White,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.database), style = typo().bodySmall)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 4.dp),
                    ) {
                        Box(
                            Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(
                                    Color.LightGray,
                                ),
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(text = stringResource(Res.string.free_space), style = typo().bodySmall)
                    }
                }
            }
            }
        }
        if (displayedSettingsPage == "backup") item(key = "backup") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_backup_contents),
                    icon = SimpIcons.Download,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.backup_downloaded),
                        subtitle = stringResource(Res.string.backup_downloaded_description),
                        switch = (backupDownloaded to { viewModel.setBackupDownloaded(it) }),
                    )
                }
                if (getPlatform() == Platform.Android) {
                    SettingsSection(
                        title = stringResource(Res.string.settings_automatic_backup),
                        icon = SimpIcons.CalendarToday,
                    ) {
                        SettingItem(
                            title = stringResource(Res.string.auto_backup),
                            subtitle = stringResource(Res.string.auto_backup_description),
                            switch = (autoBackupEnabled to { viewModel.setAutoBackupEnabled(it) }),
                        )
                        AnimatedVisibility(visible = autoBackupEnabled) {
                            SettingsNested {
                                SettingItem(
                                    title = stringResource(Res.string.backup_frequency),
                                    subtitle =
                                        when (autoBackupFrequency) {
                                            DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY -> stringResource(Res.string.daily)
                                            DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY -> stringResource(Res.string.weekly)
                                            DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY -> stringResource(Res.string.monthly)
                                            else -> stringResource(Res.string.daily)
                                        },
                                    onClick = {
                                        viewModel.setAlertData(
                                            SettingAlertState(
                                                title = runBlocking { getString(Res.string.backup_frequency) },
                                                selectOne =
                                                    SettingAlertState.SelectData(
                                                        listSelect =
                                                            listOf(
                                                                (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY) to
                                                                    runBlocking { getString(Res.string.daily) },
                                                                (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY) to
                                                                    runBlocking { getString(Res.string.weekly) },
                                                                (autoBackupFrequency == DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY) to
                                                                    runBlocking { getString(Res.string.monthly) },
                                                            ),
                                                    ),
                                                confirm =
                                                    runBlocking { getString(Res.string.change) } to { state ->
                                                        val frequency =
                                                            when (state.selectOne?.getSelected()) {
                                                                runBlocking {
                                                                    getString(
                                                                        Res.string.daily,
                                                                    )
                                                                },
                                                                -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                                runBlocking {
                                                                    getString(
                                                                        Res.string.weekly,
                                                                    )
                                                                },
                                                                -> DataStoreManager.AUTO_BACKUP_FREQUENCY_WEEKLY
                                                                runBlocking {
                                                                    getString(
                                                                        Res.string.monthly,
                                                                    )
                                                                },
                                                                -> DataStoreManager.AUTO_BACKUP_FREQUENCY_MONTHLY
                                                                else -> DataStoreManager.AUTO_BACKUP_FREQUENCY_DAILY
                                                            }
                                                        viewModel.setAutoBackupFrequency(frequency)
                                                    },
                                                dismiss = runBlocking { getString(Res.string.cancel) },
                                            ),
                                        )
                                    },
                                )
                                SettingItem(
                                    title = stringResource(Res.string.keep_backups),
                                    subtitle = stringResource(Res.string.keep_backups_format, "$autoBackupMaxFiles"),
                                    onClick = {
                                        viewModel.setAlertData(
                                            SettingAlertState(
                                                title = runBlocking { getString(Res.string.keep_backups) },
                                                selectOne =
                                                    SettingAlertState.SelectData(
                                                        listSelect =
                                                            listOf(
                                                                (autoBackupMaxFiles == 3) to "3",
                                                                (autoBackupMaxFiles == 5) to "5",
                                                                (autoBackupMaxFiles == 10) to "10",
                                                                (autoBackupMaxFiles == 15) to "15",
                                                            ),
                                                    ),
                                                confirm =
                                                    runBlocking { getString(Res.string.change) } to { state ->
                                                        val maxFiles = state.selectOne?.getSelected()?.toIntOrNull() ?: 5
                                                        viewModel.setAutoBackupMaxFiles(maxFiles)
                                                    },
                                                dismiss = runBlocking { getString(Res.string.cancel) },
                                            ),
                                        )
                                    },
                                )
                                SettingItem(
                                    title = stringResource(Res.string.last_backup),
                                    subtitle =
                                        if (autoBackupLastTime == 0L) {
                                            stringResource(Res.string.never)
                                        } else {
                                            DateTimeFormatter
                                                .ofPattern("yyyy-MM-dd HH:mm:ss")
                                                .withZone(ZoneId.systemDefault())
                                                .format(Instant.ofEpochMilli(autoBackupLastTime))
                                        },
                                )
                            }
                        }
                    }
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_transfer),
                    icon = SimpIcons.PlaylistAdd,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.backup),
                        subtitle = stringResource(Res.string.save_all_your_playlist_data),
                        onClick = {
                            coroutineScope.launch {
                                backupLauncher.launch()
                            }
                        },
                    )
                    SettingItem(
                        title = stringResource(Res.string.restore_your_data),
                        subtitle = stringResource(Res.string.restore_your_saved_data),
                        onClick = {
                            coroutineScope.launch {
                                restoreLauncher.launch()
                            }
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.import_data),
                        subtitle = stringResource(Res.string.import_playlists_from_other_apps),
                        onClick = {
                            coroutineScope.launch {
                                importLauncher.launch()
                            }
                        },
                    )
                    val converterLabel = stringResource(Res.string.playlist_converter)
                    val beforeUrl = stringResource(Res.string.import_data_intro).substringBefore("https://www.simpmusic.org/tools")
                    val afterUrl = stringResource(Res.string.import_data_intro).substringAfter("https://www.simpmusic.org/tools")
                    Text(
                        buildAnnotatedString {
                            append(beforeUrl)
                            withLink(
                                LinkAnnotation.Url(
                                    "https://www.simpmusic.org/tools",
                                    TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary)),
                                ),
                            ) {
                                append(converterLabel)
                            }
                            append(afterUrl)
                        },
                        style = typo().bodySmall,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp),
                    )
                }
            }
        }
        if (displayedSettingsPage == "about_us") item(key = "about_us") {
            Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
                SettingsSection(
                    title = stringResource(Res.string.settings_updates),
                    icon = SimpIcons.Info,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.version),
                        subtitle = "v${VersionManager.getVersionName().removePrefix("v")}",
                    )
                    SettingItem(
                        title = stringResource(Res.string.auto_check_for_update),
                        subtitle = stringResource(Res.string.auto_check_for_update_description),
                        switch = (autoCheckUpdate to { viewModel.setAutoCheckUpdate(it) }),
                    )
                    SettingItem(
                        title = stringResource(Res.string.update_channel),
                        subtitle = runBlocking { getString(Res.string.github_releases) },
                    )
                    SettingItem(
                        title = stringResource(Res.string.check_for_update),
                        subtitle = checkForUpdateSubtitle,
                        onClick = {
                            sharedViewModel.checkForUpdate()
                        },
                    )
                }
                SettingsSection(
                    title = stringResource(Res.string.settings_credits),
                    icon = SimpIcons.PeopleAlt,
                ) {
                    SettingItem(
                        title = stringResource(Res.string.author),
                        subtitle = "Pete-Dev",
                        onClick = {
                            uriHandler.openUri("https://github.com/petartawfeek-droid")
                        },
                    )
                    SettingsDivider()
                    SettingItem(
                        title = stringResource(Res.string.third_party_libraries),
                        subtitle = stringResource(Res.string.description_and_licenses),
                        onClick = {
                            showThirdPartyLibraries = true
                        },
                    )
                }
            }
        }
        if (displayedSettingsPage != null) item(key = "end") {
            EndOfPage(bottomPaddingAlreadyApplied = innerPadding.calculateBottomPadding())
        }
    }
    }
    importState?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = importViewModel::dismiss,
        )
    }
    val showLoadingDialog by viewModel.showLoadingDialog.collectAsStateWithLifecycle()
    if (showLoadingDialog.first) {
        LoadingDialog(
            true,
            showLoadingDialog.second,
        )
    }
    val basisAlertData by viewModel.basicAlertData.collectAsStateWithLifecycle()
    if (basisAlertData != null) {
        val alertBasicState = basisAlertData ?: return@SettingsTheme
        AmoledAlertDialog(
            onDismissRequest = { viewModel.setBasicAlertData(null) },
            title = {
                Text(
                    text = alertBasicState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertBasicState.message != null) {
                    Text(text = alertBasicState.message)
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertBasicState.confirm.second.invoke()
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setBasicAlertData(null)
                    },
                ) {
                    Text(text = alertBasicState.dismiss)
                }
            },
        )
    }
    val alertData by viewModel.alertData.collectAsStateWithLifecycle()
    if (alertData != null) {
        val alertState = alertData ?: return@SettingsTheme
        // AlertDialog
        AmoledAlertDialog(
            onDismissRequest = { viewModel.setAlertData(null) },
            title = {
                Text(
                    text = alertState.title,
                    style = typo().titleSmall,
                )
            },
            text = {
                if (alertState.message != null) {
                    Column {
                        Text(text = alertState.message)
                        if (alertState.textField != null) {
                            val verify =
                                alertState.textField.verifyCodeBlock?.invoke(
                                    alertState.textField.value,
                                ) ?: (true to null)
                            TextField(
                                value = alertState.textField.value,
                                onValueChange = {
                                    viewModel.setAlertData(
                                        alertState.copy(
                                            textField =
                                                alertState.textField.copy(
                                                    value = it,
                                                ),
                                        ),
                                    )
                                },
                                isError = !verify.first,
                                label = { Text(text = alertState.textField.label) },
                                supportingText = {
                                    if (!verify.first) {
                                        Text(
                                            modifier = Modifier.fillMaxWidth(),
                                            text = verify.second ?: "",
                                            color = MaterialTheme.colorScheme.error,
                                        )
                                    }
                                },
                                trailingIcon = {
                                    if (!verify.first) {
                                        SimpIcons.Error
                                    }
                                },
                                modifier =
                                    Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            vertical = 6.dp,
                                        ),
                            )
                        }
                    }
                } else if (alertState.selectOne != null) {
                    LazyColumn(
                        Modifier
                            .padding(vertical = 6.dp)
                            .heightIn(0.dp, 500.dp),
                    ) {
                        items(alertState.selectOne.listSelect) { item ->
                            val onSelect = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        selectOne =
                                            alertState.selectOne.copy(
                                                listSelect =
                                                    alertState.selectOne.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            true to it.second
                                                        } else {
                                                            false to it.second
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onSelect.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                RadioButton(
                                    selected = item.first,
                                    onClick = {
                                        onSelect.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = item.second,
                                    style = typo().bodyMedium,
                                    maxLines = 1,
                                    modifier =
                                        Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight(align = Alignment.CenterVertically)
                                            .basicMarquee(
                                                iterations = Int.MAX_VALUE,
                                                animationMode = MarqueeAnimationMode.Immediately,
                                            ).focusable(),
                                )
                            }
                        }
                    }
                } else if (alertState.multipleSelect != null) {
                    LazyColumn(
                        Modifier.padding(vertical = 6.dp),
                    ) {
                        items(alertState.multipleSelect.listSelect) { item ->
                            val onCheck = {
                                viewModel.setAlertData(
                                    alertState.copy(
                                        multipleSelect =
                                            alertState.multipleSelect.copy(
                                                listSelect =
                                                    alertState.multipleSelect.listSelect.toMutableList().map {
                                                        if (it == item) {
                                                            !it.first to it.second
                                                        } else {
                                                            it
                                                        }
                                                    },
                                            ),
                                    ),
                                )
                            }
                            Row(
                                Modifier
                                    .padding(vertical = 4.dp)
                                    .clickable {
                                        onCheck.invoke()
                                    }.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Checkbox(
                                    checked = item.first,
                                    onCheckedChange = {
                                        onCheck.invoke()
                                    },
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(text = item.second, style = typo().bodyMedium, maxLines = 1)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        alertState.confirm.second.invoke(alertState)
                        viewModel.setAlertData(null)
                    },
                    enabled =
                        if (alertState.textField?.verifyCodeBlock != null) {
                            alertState.textField.verifyCodeBlock
                                .invoke(
                                    alertState.textField.value,
                                ).first
                        } else {
                            true
                        },
                ) {
                    Text(text = alertState.confirm.first)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        viewModel.setAlertData(null)
                    },
                ) {
                    Text(text = alertState.dismiss)
                }
            },
        )
    }

    if (showThirdPartyLibraries) {
        val libraries by produceLibraries {
            Res.readBytes("files/aboutlibraries.json").decodeToString()
        }
        val lazyListState = rememberLazyListState()
        val canScrollBackward by remember {
            derivedStateOf {
                lazyListState.canScrollBackward
            }
        }
        val sheetState =
            rememberModalBottomSheetState(
                skipPartiallyExpanded = true,
                confirmValueChange = {
                    !canScrollBackward
                },
            )
        val coroutineScope = rememberCoroutineScope()
        ModalBottomSheet(
            modifier =
                Modifier
                    .fillMaxHeight(),
            onDismissRequest = {
                showThirdPartyLibraries = false
            },
            containerColor = MaterialTheme.colorScheme.surface,
            dragHandle = {},
            scrimColor = Color.Black.copy(alpha = .5f),
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0, 0, 0, 0) },
            shape = RectangleShape,
        ) {
            // Capture theme colors here: the ChipColors getters below run outside composition.
            val surfaceContainerHighestColor = MaterialTheme.colorScheme.surfaceContainerHighest
            val onSurfaceColor = MaterialTheme.colorScheme.onSurface
            LibrariesContainer(
                libraries?.copy(
                    libraries =
                        libraries
                            ?.libraries
                            ?.distinctBy {
                                it.name
                            }?.toImmutableList() ?: emptyList<Library>().toImmutableList(),
                ),
                Modifier.dialogMotionSource(clearPreviousOrigin = true).fillMaxSize(),
                lazyListState = lazyListState,
                contentPadding = innerPadding,
                colors =
                    LibraryDefaults.libraryColors(
                        licenseChipColors =
                            object : ChipColors {
                                override val containerColor: Color
                                    get() = surfaceContainerHighestColor
                                override val contentColor: Color
                                    get() = onSurfaceColor
                            },
                    ),
                header = {
                    item {
                        TopAppBar(
                            expandedHeight = CompactHeaderContentHeight,
                            windowInsets = WindowInsets(0, 0, 0, 0),
                            title = {
                                Text(
                                    text =
                                        stringResource(
                                            Res.string.third_party_libraries,
                                        ),
                                    style = typo().titleMedium,
                                )
                            },
                            navigationIcon = {
                                Box(Modifier.padding(horizontal = 5.dp)) {
                                    RippleIconButton(
                                        SimpIcons.ArrowBackIosNew,
                                        Modifier
                                            .size(32.dp),
                                        true,
                                        tint = MaterialTheme.colorScheme.onSurface,
                                    ) {
                                        coroutineScope.launch {
                                            sheetState.hide()
                                            showThirdPartyLibraries = false
                                        }
                                    }
                                }
                            },
                        )
                    }
                },
            )
        }
    }

    }
    FrostedHeader(
        hazeState = settingsHeaderHazeState,
        backdrop = settingsHeaderBackdrop,
        shareHeader = true,
        showSurface = settingsHeaderSurfaceVisible,
        modifier =
            Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .clipToBounds()
                .height(settingsHeaderTopInset + settingsHeaderContentHeight)
                .graphicsLayer {
                    // Shared controls move in their host; standalone settings move this bar.
                    translationY = if (settingsHeaderHost == null) settingsHeaderOffset.value else 0f
                },
        tint = MaterialTheme.colorScheme.background,
    ) {
        PageTopAppBar(
            windowInsets = WindowInsets(0, 0, 0, 0),
            title = {
                // Keep the toolbar titles attached to their pages during predictive back: the
                // detail title moves with the dragged page while Settings is revealed underneath.
                if (predictiveBackActive || predictiveBackCommitted) {
                    val backProgress = predictiveBackProgress
                    // A full-width title is clamped past the back button by the centered toolbar,
                    // shifting its center. Keep the gesture layer bounded by its title text.
                    Box(Modifier.width(IntrinsicSize.Max)) {
                        Text(
                            text = stringResource(Res.string.settings),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationX = settingsBackTranslationX(
                                            effectivePageTransitionStyle, settingsViewportWidthPx, backProgress.floatValue, entering = true,
                                        )
                                        alpha = settingsBackAlpha(effectivePageTransitionStyle, backProgress.floatValue, entering = true)
                                    },
                            textAlign = TextAlign.Center,
                            style = typo().titleMedium.copy(fontSize = 22.sp, lineHeight = 30.sp),
                        )
                        Text(
                            text = settingsPageTitle ?: stringResource(Res.string.settings),
                            modifier =
                                Modifier
                                    .fillMaxWidth()
                                    .graphicsLayer {
                                        translationX = settingsBackTranslationX(
                                            effectivePageTransitionStyle, settingsViewportWidthPx, backProgress.floatValue, entering = false,
                                        )
                                        alpha = settingsBackAlpha(effectivePageTransitionStyle, backProgress.floatValue, entering = false)
                                    },
                            textAlign = TextAlign.Center,
                            style = typo().titleMedium.copy(fontSize = 22.sp, lineHeight = 30.sp),
                        )
                    }
                } else {
                    AnimatedContent(
                        targetState = selectedSettingsPage,
                        transitionSpec = {
                            if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE_V2) {
                                val openingDetailPage = targetState != null
                                slideV2EnterTransition(openingDetailPage, settingsViewportWidthPx)
                                    .togetherWith(slideV2ExitTransition(openingDetailPage, settingsViewportWidthPx))
                                    .using(null)
                            } else if (effectivePageTransitionStyle == DataStoreManager.PAGE_TRANSITION_SLIDE) {
                                val openingDetailPage = targetState != null
                                slideInHorizontally(
                                    tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
                                ) { if (openingDetailPage) settingsViewportWidthPx else -settingsViewportWidthPx }
                                    .togetherWith(
                                        slideOutHorizontally(
                                            tween(BOTTOM_NAV_TRANSITION_DURATION_MILLIS, easing = FastOutSlowInEasing),
                                        ) { if (openingDetailPage) -settingsViewportWidthPx else settingsViewportWidthPx },
                                    )
                                    .using(SizeTransform(clip = false))
                            } else {
                                fadeIn(tween(260)).togetherWith(fadeOut(tween(170)))
                            }
                        },
                        contentAlignment = Alignment.Center,
                        label = "settings_header_title_transition",
                    ) { displayedSettingsPage ->
                        Text(
                            text =
                                settingsPages.firstOrNull { it.first == displayedSettingsPage }?.second
                                    ?: stringResource(Res.string.settings),
                            style = typo().titleMedium.copy(fontSize = 22.sp, lineHeight = 30.sp),
                        )
                    }
                }
            },
            navigationIcon = {
                Box(Modifier.padding(horizontal = 5.dp)) {
                    RippleIconButton(
                        SimpIcons.ArrowBackIosNew,
                        Modifier
                            .size(32.dp),
                        true,
                        tint = MaterialTheme.colorScheme.onSurface,
                    ) {
                        if (selectedSettingsPage != null) {
                            selectedSettingsPage = null
                        } else {
                            navController.navigateUp()
                        }
                    }
                }
            },
            modifier = Modifier.align(Alignment.BottomCenter),
            colors =
                TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                ),
        )
    }
    }
    }
}


@Composable
private fun SettingsOverview(
    settingsPages: List<Pair<String, String>>,
    onOpenPage: (String) -> Unit,
) {
    Column(Modifier.widthIn(max = 720.dp).fillMaxWidth()) {
        SettingsSection(
            title = stringResource(Res.string.settings_personalize),
        ) {
            val pages = settingsPages.filter { it.first in listOf("user_interface", "lyrics") }
            pages.forEachIndexed { index, (page, title) ->
                if (index > 0) SettingsDivider()
                SettingItem(
                    title = title,
                    subtitle = when (page) {
                        "user_interface" -> stringResource(Res.string.settings_page_user_interface_description)
                        "lyrics" -> stringResource(Res.string.settings_page_lyrics_description)
                        else -> ""
                    },
                    leadingIcon = when (page) {
                        "user_interface" -> SimpIcons.Tune
                        "lyrics" -> SimpIcons.Lyrics
                        else -> SimpIcons.Settings
                    },
                    onClick = { onOpenPage(page) },
                )
            }
        }
        SettingsSection(
            title = stringResource(Res.string.settings_sound_playback),
        ) {
            val pages = settingsPages.filter { it.first in listOf("audio", "playback") }
            pages.forEachIndexed { index, (page, title) ->
                if (index > 0) SettingsDivider()
                SettingItem(
                    title = title,
                    subtitle = when (page) {
                        "audio" -> stringResource(Res.string.settings_page_audio_description)
                        "playback" -> stringResource(Res.string.settings_page_playback_description)
                        else -> ""
                    },
                    leadingIcon = when (page) {
                        "audio" -> SimpIcons.GraphicEq
                        "playback" -> SimpIcons.PlayCircle
                        else -> SimpIcons.Settings
                    },
                    onClick = { onOpenPage(page) },
                )
            }
        }
        SettingsSection(
            title = stringResource(Res.string.settings_library_services),
        ) {
            val pages = settingsPages.filter { it.first in listOf("content", "listening_history", "lastfm") }
            pages.forEachIndexed { index, (page, title) ->
                if (index > 0) SettingsDivider()
                SettingItem(
                    title = title,
                    subtitle = when (page) {
                        "content" -> stringResource(Res.string.settings_page_content_description)
                        "listening_history" -> stringResource(Res.string.settings_page_listening_history_description)
                        "lastfm" -> stringResource(Res.string.settings_page_lastfm_description)
                        else -> ""
                    },
                    leadingIcon = when (page) {
                        "content" -> SimpIcons.PeopleAlt
                        "listening_history" -> SimpIcons.History
                        "lastfm" -> SimpIcons.Sync
                        else -> SimpIcons.Settings
                    },
                    onClick = { onOpenPage(page) },
                )
            }
        }
        SettingsSection(
            title = stringResource(Res.string.settings_data_app),
        ) {
            val pages = settingsPages.filter { it.first in listOf("storage", "backup", "about_us") }
            pages.forEachIndexed { index, (page, title) ->
                if (index > 0) SettingsDivider()
                SettingItem(
                    title = title,
                    subtitle = when (page) {
                        "storage" -> stringResource(Res.string.settings_page_storage_description)
                        "backup" -> stringResource(Res.string.settings_page_backup_description)
                        "about_us" -> stringResource(Res.string.settings_page_about_us_description)
                        else -> ""
                    },
                    leadingIcon = when (page) {
                        "storage" -> SimpIcons.Download
                        "backup" -> SimpIcons.CalendarToday
                        "about_us" -> SimpIcons.Info
                        else -> SimpIcons.Settings
                    },
                    onClick = { onOpenPage(page) },
                )
            }
        }
    }
}

/**
 * Progress and outcome of a playlist import.
 *
 * Only dismissible once the import has finished — cancelling mid-write would leave the database
 * half-populated with no way to tell the user which half.
 */
@Composable
private fun ImportProgressDialog(
    progress: ImportProgress,
    onDismiss: () -> Unit,
) {
    val finished = progress is ImportProgress.Success || progress is ImportProgress.Error
    AmoledAlertDialog(
        onDismissRequest = { if (finished) onDismiss() },
        properties =
            DialogProperties(
                dismissOnBackPress = finished,
                dismissOnClickOutside = finished,
            ),
        title = {
            Text(
                text =
                    stringResource(
                        if (progress is ImportProgress.Error) Res.string.import_failed else Res.string.import_data,
                    ),
                style = typo().titleSmall,
            )
        },
        text = {
            Column {
                when (progress) {
                    is ImportProgress.Preparing -> {
                        Text(
                            text = stringResource(Res.string.import_reading_file),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }

                    is ImportProgress.Importing -> {
                        Text(
                            text = stringResource(Res.string.import_progress_songs, progress.processed, progress.total),
                            style = typo().bodyMedium,
                        )
                        Spacer(Modifier.height(12.dp))
                        LinearProgressIndicator(
                            progress = {
                                if (progress.total > 0) progress.processed.toFloat() / progress.total else 0f
                            },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    is ImportProgress.Success -> {
                        Text(
                            text =
                                stringResource(
                                    Res.string.import_result,
                                    progress.result.playlistsCreated,
                                    progress.result.songsImported,
                                ),
                            style = typo().bodyMedium,
                        )
                        if (progress.result.skippedEntries > 0) {
                            Spacer(Modifier.height(8.dp))
                            Text(
                                text = stringResource(Res.string.import_result_skipped, progress.result.skippedEntries),
                                style = typo().bodySmall,
                            )
                        }
                    }

                    is ImportProgress.Error -> {
                        Text(
                            text = progress.message,
                            style = typo().bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (finished) {
                TextButton(onClick = onDismiss) {
                    Text(text = stringResource(Res.string.ok))
                }
            }
        },
    )
}
