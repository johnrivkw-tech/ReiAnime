package com.rei.app.ui.theme

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ThemeViewModel @Inject constructor(private val store: ReiDataStore) : ViewModel() {
    private val _state = MutableStateFlow(ThemeState())
    val themeState: StateFlow<ThemeState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            store.prefs.collect { p ->
                val config = ReiConfig(
                    themeMode = ThemeMode.entries.getOrElse(p[K.THEME] ?: 0) { ThemeMode.SYSTEM },
                    accentColor = AccentColor.entries.getOrElse(p[K.ACCENT] ?: 0) { AccentColor.REI_BLUE },
                    secondaryAccent = AccentColor.entries.getOrElse(p[K.ACCENT2] ?: 9) { AccentColor.ROSE },
                    isAmoled = p[K.AMOLED] ?: false,
                    useDynamicColors = p[K.DYNAMIC] ?: false,
                    cardStyle = CardStyle.entries.getOrElse(p[K.CARD] ?: 0) { CardStyle.STANDARD },
                    homeLayout = HomeLayout.entries.getOrElse(p[K.HOME_LAYOUT] ?: 0) { HomeLayout.CAROUSEL },
                    listDisplay = ListDisplayMode.entries.getOrElse(p[K.LIST_MODE] ?: 0) { ListDisplayMode.LIST },
                    scoreStyle = ScoreStyle.entries.getOrElse(p[K.SCORE_STYLE] ?: 0) { ScoreStyle.STAR },
                    fontSize = FontSize.entries.getOrElse(p[K.FONT_SIZE] ?: 2) { FontSize.MEDIUM },
                    contentScale = ContentScale.entries.getOrElse(p[K.CONTENT_SCALE] ?: 0) { ContentScale.CROP },
                    imageQuality = ImageQuality.entries.getOrElse(p[K.IMG_QUALITY] ?: 2) { ImageQuality.HIGH },
                    showScoreOnCards = p[K.SCORE_CARDS] ?: true,
                    showFormatOnCards = p[K.FORMAT_CARDS] ?: true,
                    showGenreChips = p[K.GENRE_CHIPS] ?: true,
                    showStudioOnCard = p[K.STUDIO_CARD] ?: false,
                    showProgressOnCards = p[K.PROGRESS_CARDS] ?: true,
                    collapseSynopsis = p[K.COLLAPSE_SYN] ?: true,
                    useCoverAsBanner = p[K.COVER_BANNER] ?: true,
                    showNextEpisode = p[K.NEXT_EP] ?: true,
                    showTrailerButton = p[K.TRAILER] ?: true,
                    showRecommendations = p[K.RECS] ?: true,
                    showReviews = p[K.REVIEWS] ?: true,
                    showFranchise = p[K.FRANCHISE] ?: true,
                    showRelated = p[K.RELATED] ?: true,
                    showCharacters = p[K.CHARS] ?: true,
                    showStaff = p[K.STAFF] ?: true,
                    showStatsOnDetail = p[K.STATS_DETAIL] ?: true,
                    showSourceInfo = p[K.SOURCE] ?: true,
                    showRankings = p[K.RANKINGS] ?: true,
                    showAiringSchedule = p[K.AIRING_SCHED] ?: true,
                    blurIntensity = BlurIntensity.entries.getOrElse(p[K.BLUR] ?: 2) { BlurIntensity.MEDIUM },
                    animSpeed = AnimSpeed.entries.getOrElse(p[K.ANIM] ?: 1) { AnimSpeed.NORMAL },
                    hapticIntensity = HapticIntensity.entries.getOrElse(p[K.HAPTIC_INT] ?: 2) { HapticIntensity.MEDIUM },
                    transitionStyle = TransitionStyle.entries.getOrElse(p[K.TRANSITION] ?: 0) { TransitionStyle.SLIDE },
                    indicatorStyle = IndicatorStyle.entries.getOrElse(p[K.INDICATOR] ?: 0) { IndicatorStyle.DOT },
                    useParallax = p[K.PARALLAX] ?: true,
                    useBlurBanner = p[K.BLUR_BANNER] ?: true,
                    useHaptic = p[K.HAPTIC] ?: true,
                    useStaggeredAnimations = p[K.STAGGER] ?: true,
                    useParticleBackground = p[K.PARTICLES] ?: false,
                    useGlowOnScore = p[K.GLOW_SCORE] ?: true,
                    useColorfulGenres = p[K.COLOR_GENRES] ?: true,
                    borderRadius = p[K.RADIUS] ?: 14,
                    cardElevation = p[K.ELEVATION] ?: 2,
                    bannerHeight = p[K.BANNER_H] ?: 420,
                    gridColumns = p[K.GRID_COLS] ?: 3,
                    itemsPerRow = p[K.ITEMS_ROW] ?: 3,
                    customSurfaceAlpha = (p[K.SURFACE_ALPHA] ?: 95) / 100f,
                    topBarAlpha = (p[K.TOP_BAR_ALPHA] ?: 95) / 100f,
                    bottomBarAlpha = (p[K.BOT_BAR_ALPHA] ?: 95) / 100f,
                    cardWidth = p[K.CARD_W] ?: 140,
                    cardHeight = p[K.CARD_H] ?: 200,
                    listItemSpacing = p[K.LIST_SPACING] ?: 12,
                    sectionSpacing = p[K.SECTION_SPACING] ?: 16,
                    carouselAutoScrollMs = p[K.CAROUSEL_MS] ?: 5000,
                    useTranslucentBars = p[K.TRANSLUCENT] ?: true,
                    defaultSort = SortDefault.entries.getOrElse(p[K.DEFAULT_SORT] ?: 0) { SortDefault.POPULARITY },
                    searchEngine = SearchEngine.entries.getOrElse(p[K.SEARCH_ENGINE] ?: 3) { SearchEngine.ALL },
                    swipeLeftAction = SwipeAction.entries.getOrElse(p[K.SWIPE_L] ?: 1) { SwipeAction.TRACK },
                    swipeRightAction = SwipeAction.entries.getOrElse(p[K.SWIPE_R] ?: 2) { SwipeAction.FAVORITE },
                    navStyle = NavStyle.entries.getOrElse(p[K.NAV_STYLE] ?: 0) { NavStyle.BOTTOM },
                    splashAfter = SplashAfter.entries.getOrElse(p[K.SPLASH] ?: 1) { SplashAfter.WEEK },
                    notifyStyle = NotifyStyle.entries.getOrElse(p[K.NOTIFY] ?: 1) { NotifyStyle.DETAILED },
                    openDetailInOverlay = p[K.OVERLAY_DETAIL] ?: false,
                    confirmBeforeTrack = p[K.CONFIRM_TRACK] ?: true,
                    showSpoilerTags = p[K.SPOILER] ?: false,
                    nsfwFilter = p[K.NSFW] ?: true,
                    autoProgress = p[K.AUTO_PROGRESS] ?: true,
                    markCompleteAt = p[K.MARK_COMPLETE] ?: 100,
                    longPressPreview = p[K.LONG_PRESS] ?: true,
                    doubleTapFavorite = p[K.DOUBLE_TAP] ?: true,
                    // New premium options
                    fontFamily = FontFamily.entries.getOrElse(p[K.FONT_FAMILY] ?: 0) { FontFamily.SYSTEM },
                    cardLayout = CardLayout.entries.getOrElse(p[K.CARD_LAYOUT] ?: 0) { CardLayout.VERTICAL },
                    scrollBehavior = ScrollBehavior.entries.getOrElse(p[K.SCROLL_BEHAV] ?: 1) { ScrollBehavior.CONTINUOUS },
                    detailTabOrder = DetailTabOrder.entries.getOrElse(p[K.DETAIL_TAB] ?: 0) { DetailTabOrder.OVERVIEW_FIRST },
                    bannerStyle = BannerStyle.entries.getOrElse(p[K.BANNER_STYLE] ?: 0) { BannerStyle.FULL_BLEED },
                    startupScreen = StartupScreen.entries.getOrElse(p[K.STARTUP] ?: 0) { StartupScreen.HOME },
                    useBlurNavBar = p[K.BLUR_NAV] ?: false,
                    useBlurTopBar = p[K.BLUR_TOP] ?: false,
                    showRankBadges = p[K.RANK_BADGES] ?: true,
                    showAiringBadge = p[K.AIRING_BADGE] ?: true,
                    useCompactCards = p[K.COMPACT_CARDS] ?: false,
                    showSourceOnDetail = p[K.SOURCE_DETAIL] ?: true,
                    showDurationOnDetail = p[K.DURATION_DETAIL] ?: true,
                    showEpisodePreview = p[K.EP_PREVIEW] ?: true,
                    cardAspectRatio = (p[K.CARD_ASPECT] ?: 70) / 100f,
                    carouselScale = (p[K.CAROUSEL_SCALE] ?: 95) / 100f,
                    maxLinesTitle = p[K.TITLE_LINES] ?: 2,
                    showWaifuOfTheDay = p[K.WAIFU_DAY] ?: false,
                    usePremiumShimmer = p[K.PREMIUM_SHIMMER] ?: true,
                    showBreadcrumb = p[K.BREADCRUMB] ?: true,
                    hapticOnScroll = p[K.HAPTIC_SCROLL] ?: false,
                    swipeSensitivity = p[K.SWIPE_SENS] ?: 50,
                    maxCacheMb = p[K.CACHE_MB] ?: 50,
                    useWebSocket = p[K.WEBSOCKET] ?: false,
                    notifyAiring = p[K.NOTIFY_AIRING] ?: true,
                    notifyFollows = p[K.NOTIFY_FOLLOWS] ?: false,
                    notifyAnnouncements = p[K.NOTIFY_ANNOUNCE] ?: false
                )
                _state.value = ThemeState(config.themeMode, config.accentColor, config.isAmoled, config)
            }
        }
    }

    fun update(transform: (ReiConfig) -> ReiConfig) = viewModelScope.launch {
        val c = transform(_state.value.config)
        // Persist all fields
        store.putInt(K.THEME, c.themeMode.ordinal); store.putInt(K.ACCENT, c.accentColor.ordinal)
        store.putInt(K.ACCENT2, c.secondaryAccent.ordinal); store.putBoolean(K.AMOLED, c.isAmoled)
        store.putBoolean(K.DYNAMIC, c.useDynamicColors); store.putInt(K.CARD, c.cardStyle.ordinal)
        store.putInt(K.HOME_LAYOUT, c.homeLayout.ordinal); store.putInt(K.LIST_MODE, c.listDisplay.ordinal)
        store.putInt(K.SCORE_STYLE, c.scoreStyle.ordinal); store.putInt(K.FONT_SIZE, c.fontSize.ordinal)
        store.putInt(K.CONTENT_SCALE, c.contentScale.ordinal); store.putInt(K.IMG_QUALITY, c.imageQuality.ordinal)
        store.putBoolean(K.SCORE_CARDS, c.showScoreOnCards); store.putBoolean(K.FORMAT_CARDS, c.showFormatOnCards)
        store.putBoolean(K.GENRE_CHIPS, c.showGenreChips); store.putBoolean(K.STUDIO_CARD, c.showStudioOnCard)
        store.putBoolean(K.PROGRESS_CARDS, c.showProgressOnCards); store.putBoolean(K.COLLAPSE_SYN, c.collapseSynopsis)
        store.putBoolean(K.COVER_BANNER, c.useCoverAsBanner); store.putBoolean(K.NEXT_EP, c.showNextEpisode)
        store.putBoolean(K.TRAILER, c.showTrailerButton); store.putBoolean(K.RECS, c.showRecommendations)
        store.putBoolean(K.REVIEWS, c.showReviews); store.putBoolean(K.FRANCHISE, c.showFranchise)
        store.putBoolean(K.RELATED, c.showRelated); store.putBoolean(K.CHARS, c.showCharacters)
        store.putBoolean(K.STAFF, c.showStaff); store.putBoolean(K.STATS_DETAIL, c.showStatsOnDetail)
        store.putBoolean(K.SOURCE, c.showSourceInfo); store.putBoolean(K.RANKINGS, c.showRankings)
        store.putBoolean(K.AIRING_SCHED, c.showAiringSchedule)
        store.putInt(K.BLUR, c.blurIntensity.ordinal); store.putInt(K.ANIM, c.animSpeed.ordinal)
        store.putInt(K.HAPTIC_INT, c.hapticIntensity.ordinal); store.putInt(K.TRANSITION, c.transitionStyle.ordinal)
        store.putInt(K.INDICATOR, c.indicatorStyle.ordinal)
        store.putBoolean(K.PARALLAX, c.useParallax); store.putBoolean(K.BLUR_BANNER, c.useBlurBanner)
        store.putBoolean(K.HAPTIC, c.useHaptic); store.putBoolean(K.STAGGER, c.useStaggeredAnimations)
        store.putBoolean(K.PARTICLES, c.useParticleBackground); store.putBoolean(K.GLOW_SCORE, c.useGlowOnScore)
        store.putBoolean(K.COLOR_GENRES, c.useColorfulGenres)
        store.putInt(K.RADIUS, c.borderRadius); store.putInt(K.ELEVATION, c.cardElevation)
        store.putInt(K.BANNER_H, c.bannerHeight); store.putInt(K.GRID_COLS, c.gridColumns)
        store.putInt(K.ITEMS_ROW, c.itemsPerRow); store.putInt(K.SURFACE_ALPHA, (c.customSurfaceAlpha * 100).toInt())
        store.putInt(K.TOP_BAR_ALPHA, (c.topBarAlpha * 100).toInt()); store.putInt(K.BOT_BAR_ALPHA, (c.bottomBarAlpha * 100).toInt())
        store.putInt(K.CARD_W, c.cardWidth); store.putInt(K.CARD_H, c.cardHeight)
        store.putInt(K.LIST_SPACING, c.listItemSpacing); store.putInt(K.SECTION_SPACING, c.sectionSpacing)
        store.putInt(K.CAROUSEL_MS, c.carouselAutoScrollMs)
        store.putBoolean(K.TRANSLUCENT, c.useTranslucentBars); store.putInt(K.DEFAULT_SORT, c.defaultSort.ordinal)
        store.putInt(K.SEARCH_ENGINE, c.searchEngine.ordinal); store.putInt(K.SWIPE_L, c.swipeLeftAction.ordinal)
        store.putInt(K.SWIPE_R, c.swipeRightAction.ordinal); store.putInt(K.NAV_STYLE, c.navStyle.ordinal)
        store.putInt(K.SPLASH, c.splashAfter.ordinal); store.putInt(K.NOTIFY, c.notifyStyle.ordinal)
        store.putBoolean(K.OVERLAY_DETAIL, c.openDetailInOverlay); store.putBoolean(K.CONFIRM_TRACK, c.confirmBeforeTrack)
        store.putBoolean(K.SPOILER, c.showSpoilerTags); store.putBoolean(K.NSFW, c.nsfwFilter)
        store.putBoolean(K.AUTO_PROGRESS, c.autoProgress); store.putInt(K.MARK_COMPLETE, c.markCompleteAt)
        store.putBoolean(K.LONG_PRESS, c.longPressPreview); store.putBoolean(K.DOUBLE_TAP, c.doubleTapFavorite)
        // New premium writes
        store.putInt(K.FONT_FAMILY, c.fontFamily.ordinal); store.putInt(K.CARD_LAYOUT, c.cardLayout.ordinal)
        store.putInt(K.SCROLL_BEHAV, c.scrollBehavior.ordinal); store.putInt(K.DETAIL_TAB, c.detailTabOrder.ordinal)
        store.putInt(K.BANNER_STYLE, c.bannerStyle.ordinal); store.putInt(K.STARTUP, c.startupScreen.ordinal)
        store.putBoolean(K.BLUR_NAV, c.useBlurNavBar); store.putBoolean(K.BLUR_TOP, c.useBlurTopBar)
        store.putBoolean(K.RANK_BADGES, c.showRankBadges); store.putBoolean(K.AIRING_BADGE, c.showAiringBadge)
        store.putBoolean(K.COMPACT_CARDS, c.useCompactCards); store.putBoolean(K.SOURCE_DETAIL, c.showSourceOnDetail)
        store.putBoolean(K.DURATION_DETAIL, c.showDurationOnDetail); store.putBoolean(K.EP_PREVIEW, c.showEpisodePreview)
        store.putInt(K.CARD_ASPECT, (c.cardAspectRatio * 100).toInt()); store.putInt(K.CAROUSEL_SCALE, (c.carouselScale * 100).toInt())
        store.putInt(K.TITLE_LINES, c.maxLinesTitle); store.putBoolean(K.WAIFU_DAY, c.showWaifuOfTheDay)
        store.putBoolean(K.PREMIUM_SHIMMER, c.usePremiumShimmer); store.putBoolean(K.BREADCRUMB, c.showBreadcrumb)
        store.putBoolean(K.HAPTIC_SCROLL, c.hapticOnScroll); store.putInt(K.SWIPE_SENS, c.swipeSensitivity)
        store.putInt(K.CACHE_MB, c.maxCacheMb); store.putBoolean(K.WEBSOCKET, c.useWebSocket)
        store.putBoolean(K.NOTIFY_AIRING, c.notifyAiring); store.putBoolean(K.NOTIFY_FOLLOWS, c.notifyFollows)
        store.putBoolean(K.NOTIFY_ANNOUNCE, c.notifyAnnouncements)
    }

    private object K {
        val THEME = intPreferencesKey("tm"); val ACCENT = intPreferencesKey("ac"); val ACCENT2 = intPreferencesKey("a2")
        val AMOLED = booleanPreferencesKey("am"); val DYNAMIC = booleanPreferencesKey("dy"); val CARD = intPreferencesKey("cd")
        val HOME_LAYOUT = intPreferencesKey("hl"); val LIST_MODE = intPreferencesKey("lm"); val SCORE_STYLE = intPreferencesKey("ss")
        val FONT_SIZE = intPreferencesKey("fs"); val CONTENT_SCALE = intPreferencesKey("cs"); val IMG_QUALITY = intPreferencesKey("iq")
        val SCORE_CARDS = booleanPreferencesKey("sc"); val FORMAT_CARDS = booleanPreferencesKey("fc"); val GENRE_CHIPS = booleanPreferencesKey("gc")
        val STUDIO_CARD = booleanPreferencesKey("st"); val PROGRESS_CARDS = booleanPreferencesKey("pc"); val COLLAPSE_SYN = booleanPreferencesKey("cs2")
        val COVER_BANNER = booleanPreferencesKey("cb"); val NEXT_EP = booleanPreferencesKey("ne"); val TRAILER = booleanPreferencesKey("tr")
        val RECS = booleanPreferencesKey("rc"); val REVIEWS = booleanPreferencesKey("rv"); val FRANCHISE = booleanPreferencesKey("fr")
        val RELATED = booleanPreferencesKey("rl"); val CHARS = booleanPreferencesKey("ch"); val STAFF = booleanPreferencesKey("sf")
        val STATS_DETAIL = booleanPreferencesKey("sd"); val SOURCE = booleanPreferencesKey("sr"); val RANKINGS = booleanPreferencesKey("rk")
        val AIRING_SCHED = booleanPreferencesKey("as2"); val BLUR = intPreferencesKey("bl"); val ANIM = intPreferencesKey("an")
        val HAPTIC_INT = intPreferencesKey("hi"); val TRANSITION = intPreferencesKey("ts"); val INDICATOR = intPreferencesKey("id2")
        val PARALLAX = booleanPreferencesKey("px"); val BLUR_BANNER = booleanPreferencesKey("bb"); val HAPTIC = booleanPreferencesKey("ht")
        val STAGGER = booleanPreferencesKey("sg"); val PARTICLES = booleanPreferencesKey("pt"); val GLOW_SCORE = booleanPreferencesKey("gs")
        val COLOR_GENRES = booleanPreferencesKey("cg"); val RADIUS = intPreferencesKey("rd"); val ELEVATION = intPreferencesKey("el")
        val BANNER_H = intPreferencesKey("bh"); val GRID_COLS = intPreferencesKey("gc2"); val ITEMS_ROW = intPreferencesKey("ir")
        val SURFACE_ALPHA = intPreferencesKey("sa"); val TOP_BAR_ALPHA = intPreferencesKey("ta"); val BOT_BAR_ALPHA = intPreferencesKey("ba")
        val CARD_W = intPreferencesKey("cw"); val CARD_H = intPreferencesKey("ch2"); val LIST_SPACING = intPreferencesKey("ls")
        val SECTION_SPACING = intPreferencesKey("ss3"); val CAROUSEL_MS = intPreferencesKey("cm"); val TRANSLUCENT = booleanPreferencesKey("tl")
        val DEFAULT_SORT = intPreferencesKey("ds"); val SEARCH_ENGINE = intPreferencesKey("se"); val SWIPE_L = intPreferencesKey("sl")
        val SWIPE_R = intPreferencesKey("srl"); val NAV_STYLE = intPreferencesKey("ns"); val SPLASH = intPreferencesKey("sp")
        val NOTIFY = intPreferencesKey("nf"); val OVERLAY_DETAIL = booleanPreferencesKey("od"); val CONFIRM_TRACK = booleanPreferencesKey("ct")
        val SPOILER = booleanPreferencesKey("sp2"); val NSFW = booleanPreferencesKey("nw"); val AUTO_PROGRESS = booleanPreferencesKey("ap")
        val MARK_COMPLETE = intPreferencesKey("mc"); val LONG_PRESS = booleanPreferencesKey("lp"); val DOUBLE_TAP = booleanPreferencesKey("dt")
        // New premium keys
        val FONT_FAMILY = intPreferencesKey("ff"); val CARD_LAYOUT = intPreferencesKey("cl"); val SCROLL_BEHAV = intPreferencesKey("sb")
        val DETAIL_TAB = intPreferencesKey("dt2"); val BANNER_STYLE = intPreferencesKey("bs"); val STARTUP = intPreferencesKey("su")
        val BLUR_NAV = booleanPreferencesKey("bn"); val BLUR_TOP = booleanPreferencesKey("bt"); val RANK_BADGES = booleanPreferencesKey("rb")
        val AIRING_BADGE = booleanPreferencesKey("ab"); val COMPACT_CARDS = booleanPreferencesKey("cc"); val SOURCE_DETAIL = booleanPreferencesKey("sd2")
        val DURATION_DETAIL = booleanPreferencesKey("dd"); val EP_PREVIEW = booleanPreferencesKey("ep"); val CARD_ASPECT = intPreferencesKey("ca")
        val CAROUSEL_SCALE = intPreferencesKey("cs4"); val TITLE_LINES = intPreferencesKey("tl"); val WAIFU_DAY = booleanPreferencesKey("wd")
        val PREMIUM_SHIMMER = booleanPreferencesKey("ps"); val BREADCRUMB = booleanPreferencesKey("bc"); val HAPTIC_SCROLL = booleanPreferencesKey("hs")
        val SWIPE_SENS = intPreferencesKey("ss4"); val CACHE_MB = intPreferencesKey("cm2"); val WEBSOCKET = booleanPreferencesKey("ws")
        val NOTIFY_AIRING = booleanPreferencesKey("na"); val NOTIFY_FOLLOWS = booleanPreferencesKey("nf2"); val NOTIFY_ANNOUNCE = booleanPreferencesKey("nna")
    }
}

class ReiDataStore @Inject constructor(private val ds: DataStore<Preferences>) {
    val prefs: Flow<Preferences> = ds.data
    suspend fun putInt(k: Preferences.Key<Int>, v: Int) = ds.edit { it[k] = v }
    suspend fun putBoolean(k: Preferences.Key<Boolean>, v: Boolean) = ds.edit { it[k] = v }
    suspend fun putString(k: Preferences.Key<String>, v: String) = ds.edit { it[k] = v }
}
