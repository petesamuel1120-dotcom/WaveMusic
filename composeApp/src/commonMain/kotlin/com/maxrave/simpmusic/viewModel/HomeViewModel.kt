package com.maxrave.simpmusic.viewModel

import androidx.lifecycle.viewModelScope
import com.maxrave.common.SELECTED_LANGUAGE
import com.maxrave.common.SUPPORTED_LANGUAGE
import com.maxrave.domain.data.entities.SongEntity
import com.maxrave.domain.data.model.home.HomeItem
import com.maxrave.domain.data.model.home.chart.Chart
import com.maxrave.domain.data.model.mood.Mood
import com.maxrave.domain.manager.DataStoreManager
import com.maxrave.domain.manager.DataStoreManager.Values.TRUE
import com.maxrave.domain.repository.HomeRepository
import com.maxrave.domain.utils.Resource
import com.maxrave.logger.Logger
import com.maxrave.simpmusic.viewModel.base.BaseViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.coroutineScope
import org.jetbrains.compose.resources.getString as getComposeString
import simpmusic.composeapp.generated.resources.Res
import simpmusic.composeapp.generated.resources.new_release
import simpmusic.composeapp.generated.resources.song
import simpmusic.composeapp.generated.resources.view_count

class HomeViewModel(
    private val dataStoreManager: DataStoreManager,
    private val homeRepository: HomeRepository,
) : BaseViewModel() {
    private val _homeItemList: MutableStateFlow<List<HomeItem>> =
        MutableStateFlow(arrayListOf())
    val homeItemList: StateFlow<List<HomeItem>> = _homeItemList

    private var _homeListState = MutableStateFlow<ListState>(ListState.IDLE)
    val homeListState: StateFlow<ListState> = _homeListState

    private var _continuation = MutableStateFlow<String?>(null)
    val continuation: StateFlow<String?> = _continuation

    private val _exploreMoodItem: MutableStateFlow<Mood?> = MutableStateFlow(null)
    val exploreMoodItem: StateFlow<Mood?> = _exploreMoodItem
    private val _accountInfo: MutableStateFlow<Pair<String?, String?>?> = MutableStateFlow(null)
    val accountInfo: StateFlow<Pair<String?, String?>?> = _accountInfo

    private var homeJob: Job? = null
    private var continuationJob: Job? = null
    private val homeFeeds = HomeFeedSession<HomeItem>()
    private var loadedFeedRequest: HomeFeedSession.Request? = null
    private var secondaryHomeJob: Job? = null
    private var secondaryHomeDataKey: String? = null

    val showSnackBarErrorState = MutableSharedFlow<String>()

    private val _chart: MutableStateFlow<Chart?> = MutableStateFlow(null)
    val chart: StateFlow<Chart?> = _chart
    private val _newRelease: MutableStateFlow<List<HomeItem>> = MutableStateFlow(arrayListOf())
    val newRelease: StateFlow<List<HomeItem>> = _newRelease
    var regionCodeChart: MutableStateFlow<String?> = MutableStateFlow(null)

    val loading = MutableStateFlow<Boolean>(true)
    val loadingChart = MutableStateFlow<Boolean>(true)
    private var regionCode: String = ""
    private var language: String = ""

    private val _songEntity: MutableStateFlow<SongEntity?> = MutableStateFlow(null)
    val songEntity: StateFlow<SongEntity?> = _songEntity

    private var _params: MutableStateFlow<String?> = MutableStateFlow(null)
    val params: StateFlow<String?> = _params

    // For showing alert that should log in to YouTube
    private val _showLogInAlert: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val showLogInAlert: StateFlow<Boolean> = _showLogInAlert

    val dataSyncId =
        dataStoreManager
            .dataSyncId
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), "")

    private val _mainHomeThumbnail: MutableStateFlow<String?> = MutableStateFlow(null)
    val mainHomeThumbnail: StateFlow<String?> = _mainHomeThumbnail

    init {
        viewModelScope.launch {
            if (dataStoreManager.cookie.first().isEmpty() &&
                dataStoreManager.shouldShowLogInRequiredAlert.first() == TRUE
            ) {
                _showLogInAlert.update { true }
            }
        }
        homeJob = Job()
        viewModelScope.launch {
            regionCodeChart.value = dataStoreManager.chartKey.first()
            language = dataStoreManager.getString(SELECTED_LANGUAGE).first()
                ?: SUPPORTED_LANGUAGE.codes.first()
            //  refresh when region change
            val job1 =
                launch {
                    dataStoreManager.location.distinctUntilChanged().drop(1).collect {
                        regionCode = it
                        getHomeItemList(params.value)
                    }
                }
            //  refresh when language change
            val job2 =
                launch {
                    dataStoreManager.language.distinctUntilChanged().drop(1).collect {
                        language = it
                        getHomeItemList(params.value)
                    }
                }
            val job3 =
                launch {
                    var previousCookie: String? = null
                    dataStoreManager.cookie.distinctUntilChanged().collect {
                        // A non-empty cookie refresh is handled by job5 after the datastore has
                        // finished persisting the account details. Fetch here only on sign-out;
                        // doing both used to cancel and immediately restart the same full feed
                        // request on every sign-in.
                        if (previousCookie != null && it.isEmpty()) getHomeItemList(params.value)
                        previousCookie = it
                    }
                }
            val job4 =
                launch {
                    params.collectLatest {
                        getHomeItemList(it, forceRefresh = false)
                    }
                }
            val job5 =
                launch {
                    dataStoreManager
                        .cookie
                        .distinctUntilChanged()
                        .drop(1)
                        .collectLatest {
                            if (it.isNotEmpty()) {
                                Logger.w(tag, "Cookie changed, refreshing home")
                                loading.value = true
                                delay(1000) // To wait for the cookie to be saved properly
                                getHomeItemList(params.value)
                            }
                        }
                }
            val job6 =
                launch {
                    homeItemList.collectLatest { list ->
                        _mainHomeThumbnail.value =
                            list
                                .firstOrNull()
                                ?.contents
                                ?.firstOrNull()
                                ?.thumbnails
                                ?.lastOrNull()
                                ?.url
                    }
                }
            // Sign-in can persist the cookie before the selected account details. Observe those
            // preferences too so the header replaces its signed-out icon as soon as the avatar URL
            // is written, even when the cookie itself no longer changes.
            val job7 =
                launch {
                    combine(
                        dataStoreManager.cookie,
                        dataStoreManager.getString("AccountName"),
                        dataStoreManager.getString("AccountThumbUrl"),
                    ) { cookie, name, thumbnail ->
                        if (cookie.isNotEmpty()) Pair(name, thumbnail) else null
                    }.distinctUntilChanged().collect { accountInfo ->
                        _accountInfo.emit(accountInfo)
                    }
                }
            job1.join()
            job2.join()
            job3.join()
            job4.join()
            job5.join()
            job6.join()
            job7.join()
        }
    }

    fun doneShowLogInAlert(neverShowAgain: Boolean = false) {
        viewModelScope.launch {
            _showLogInAlert.update { false }
            if (neverShowAgain) {
                dataStoreManager.setShouldShowLogInRequiredAlert(false)
            }
        }
    }

    fun getHomeItemList(params: String? = null, forceRefresh: Boolean = true) {
        homeJob?.cancel()
        continuationJob?.cancel()
        loadedFeedRequest = null
        val request = homeFeeds.beginRequest(params)
        homeJob =
            viewModelScope.launch {
                language =
                    dataStoreManager.getString(SELECTED_LANGUAGE).first()
                        ?: SUPPORTED_LANGUAGE.codes.first()
                regionCode = dataStoreManager.location.first()
                regionCodeChart.value = dataStoreManager.chartKey.first()
                val context = HomeFeedContext(dataStoreManager.cookie.first(), language, regionCode)
                if (!homeFeeds.isCurrent(request) || _params.value != request.params) return@launch
                homeFeeds.read(request, context, forceRefresh)?.let { cached ->
                    loadedFeedRequest = request
                    showHomeFeed(cached)
                    loadSecondaryHomeDataIfNeeded()
                    return@launch
                }
                // Only an uncached category or explicit refresh enters the loading screen.
                loading.value = true
                _homeListState.value = ListState.LOADING
                _continuation.value = null
                // BaseViewModel.getString uses runBlocking for synchronous call sites. Resolve
                // these resources through Compose's suspending API so Home's first request never
                // parks the UI thread on a cold resource lookup.
                val viewCountLabel = getComposeString(Res.string.view_count)
                val songLabel = getComposeString(Res.string.song)
                // Prioritize the feed the user sees first. The moods, charts, and release shelves
                // used to start alongside it on every category change, competing for network,
                // parsing, and CPU during cold start. Load those secondary shelves once the main
                // feed has arrived, and reuse them across Home category switches.
                homeRepository.getHomeData(
                    params,
                    viewCountLabel,
                    songLabel,
                ).collectLatest { home ->
                    if (!homeFeeds.isCurrent(request) || _params.value != request.params) return@collectLatest
                    when (home) {
                        is Resource.Success -> {
                            val feed = HomeFeedSession.Feed(home.data?.second ?: emptyList(), home.data?.first)
                            homeFeeds.store(request, feed)
                            loadedFeedRequest = request
                            showHomeFeed(feed)
                            dataStoreManager.cookie.first().takeIf { it.isNotEmpty() }?.let {
                                _accountInfo.emit(
                                    Pair(
                                        dataStoreManager.getString("AccountName").first(),
                                        dataStoreManager.getString("AccountThumbUrl").first(),
                                    ),
                                )
                            }
                            loadSecondaryHomeDataIfNeeded()
                        }
                        is Resource.Error -> {
                            _continuation.value = null
                            _homeItemList.value = emptyList()
                            _homeListState.value = ListState.PAGINATION_EXHAUST
                            loading.value = false
                            home.message?.let { showSnackBarErrorState.emit(it) }
                        }
                        else -> Unit
                    }
                    Logger.d("HomeViewModel", "Home feed emitted ${home.data?.second?.size ?: 0} rows")
                }
            }
    }

    private fun showHomeFeed(feed: HomeFeedSession.Feed<HomeItem>) {
        _homeItemList.value = feed.items
        _continuation.value = feed.continuation
        _homeListState.value = if (feed.continuation.isNullOrEmpty()) ListState.PAGINATION_EXHAUST else ListState.IDLE
        loading.value = false
    }

    private fun loadSecondaryHomeDataIfNeeded() {
        val chartRegion = regionCodeChart.value.orEmpty()
        val requestKey = "$language|$chartRegion"
        if (secondaryHomeDataKey == requestKey) return

        secondaryHomeJob?.cancel()
        secondaryHomeDataKey = requestKey
        loadingChart.value = true
        secondaryHomeJob = viewModelScope.launch {
            // Let the feed's first composition and visible artwork settle before background
            // shelves begin parsing responses on a cold process.
            delay(750)
            coroutineScope {
                launch {
                    homeRepository.getMoodAndMomentsData().collectLatest { mood ->
                        when (mood) {
                            is Resource.Success -> _exploreMoodItem.value = mood.data
                            is Resource.Error -> {
                                _exploreMoodItem.value = null
                                mood.message?.let { showSnackBarErrorState.emit(it) }
                            }
                            else -> Unit
                        }
                    }
                }
                launch {
                    // Stagger secondary requests so they do not all parse responses while the
                    // first Home feed and its artwork are still settling.
                    delay(450)
                    homeRepository.getChartData(chartRegion).collectLatest { chart ->
                        when (chart) {
                            is Resource.Success -> _chart.value = chart.data
                            is Resource.Error -> {
                                _chart.value = null
                                chart.message?.let { showSnackBarErrorState.emit(it) }
                            }
                            else -> Unit
                        }
                        loadingChart.value = false
                    }
                }
                launch {
                    delay(900)
                    val newReleaseLabel = getComposeString(Res.string.new_release)
                    homeRepository.getNewRelease(
                        newReleaseLabel,
                    ).collectLatest { releases ->
                        when (releases) {
                            is Resource.Success -> _newRelease.value = releases.data ?: emptyList()
                            is Resource.Error -> _newRelease.value = emptyList()
                            else -> Unit
                        }
                    }
                }
            }
        }
    }

    fun getContinueHomeItem(continuation: String?) {
        val request = loadedFeedRequest ?: return
        if (loading.value || !homeFeeds.isCurrent(request) || request.params != _params.value ||
            continuation != _continuation.value || continuationJob?.isActive == true ||
            _homeListState.value == ListState.PAGINATING
        ) return
        continuationJob = viewModelScope.launch {
            if (continuation.isNullOrEmpty()) {
                _homeListState.value = ListState.PAGINATION_EXHAUST
                return@launch
            } else {
                log("Get more home item with continuation: $continuation")
                _homeListState.value = ListState.PAGINATING
                val viewCountLabel = getComposeString(Res.string.view_count)
                val songLabel = getComposeString(Res.string.song)
                homeRepository
                    .getHomeDataContinue(
                        continuation,
                        viewCountLabel,
                        songLabel,
                    ).collect { home ->
                        // A cancelled page response must never append to a newly selected category.
                        if (!homeFeeds.isCurrent(request) || request.params != _params.value) return@collect
                        when (home) {
                            is Resource.Success -> {
                                val feed = HomeFeedSession.Feed(
                                    _homeItemList.value + (home.data?.second ?: emptyList()),
                                    home.data?.first,
                                )
                                if (homeFeeds.updatePagination(request, feed)) showHomeFeed(feed)
                            }

                            is Resource.Error -> {
                                val feed = HomeFeedSession.Feed(_homeItemList.value, null)
                                if (homeFeeds.updatePagination(request, feed)) showHomeFeed(feed)
                                Logger.w(tag, "getContinueHomeItem: ${home.message}")
                                showSnackBarErrorState.emit(home.message ?: "Unknown error")
                            }
                        }
                    }
            }
        }
    }

    fun exploreChart(region: String) {
        viewModelScope.launch {
            loadingChart.value = true
            homeRepository
                .getChartData(
                    region,
                ).collect { values ->
                    regionCodeChart.value = region
                    dataStoreManager.setChartKey(region)
                    when (values) {
                        is Resource.Success -> {
                            _chart.value = values.data
                        }

                        else -> {
                            _chart.value = null
                        }
                    }
                    loadingChart.value = false
                }
        }
    }

    fun setParams(params: String?) {
        _params.value = params
    }

    override fun onCleared() {
        super.onCleared()
        homeJob?.cancel()
        continuationJob?.cancel()
        secondaryHomeJob?.cancel()
    }

    companion object {
        // Home params
        const val HOME_PARAMS_RELAX = "ggM8SgQIBxADSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_SLEEP = "ggM8SgQIBxABSgQIBRADSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_ENERGIZE = "ggM8SgQIBxABSgQIBRABSgQICRADSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_SAD = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChADSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_ROMANCE = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRADSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_FEEL_GOOD = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBADSgQIBBABSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_WORKOUT = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBADSgQIDhABSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_PARTY = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhADSgQIAxABSgQIBhAB"
        const val HOME_PARAMS_COMMUTE = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxADSgQIBhAB"
        const val HOME_PARAMS_FOCUS = "ggM8SgQIBxABSgQIBRABSgQICRABSgQIChABSgQIDRABSgQICBABSgQIBBABSgQIDhABSgQIAxABSgQIBhAD"
    }
}
