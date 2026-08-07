package com.rei.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.google.accompanist.systemuicontroller.rememberSystemUiController

// ═══════════════════════════════════════════
// THEME MODES (12)
// ═══════════════════════════════════════════
enum class ThemeMode(val label: String, val icon: String) {
    SYSTEM("System", "□"), LIGHT("Light", "○"), DARK("Dark", "◐"),
    AMOLED("AMOLED Black", "●"), MIDNIGHT_DUSK("Midnight Dusk", "◐"),
    STRAWBERRY("Strawberry", "◆"), OCEAN("Ocean Wave", "◇"),
    SAKURA("Sakura", "✦"), LAVENDER_NIGHT("Lavender Night", "◈"),
    MATRIX("Matrix", "▣"), NUXT("Nuxt Dark", "▢"), ROSE_GOLD("Rose Gold", "♦"),
    CYBERPUNK("Cyberpunk", "◈"), VAPORWAVE("Vaporwave", "✧"), DEEP_PURPLE("Deep Purple", "◆"),
    ARCTIC("Arctic", "○"), TERRACOTTA("Terracotta", "◐")
}

// ═══════════════════════════════════════════
// ACCENT COLORS (16)
// ═══════════════════════════════════════════
enum class AccentColor(val label: String, val emoji: String) {
    REI_BLUE("Rei Blue", "◈"), CRUNCHYROLL_ORANGE("Crunchyroll", "▸"),
    SAKURA_PINK("Sakura Pink", "✦"), OCEAN_TEAL("Ocean Teal", "◇"),
    LAVENDER("Lavender", "◈"), MINT_GREEN("Mint", "⚘"),
    SUNSET("Sunset", "◒"), VIOLET("Violet", "◈"),
    CYAN("Cyan", "◆"), ROSE("Rose", "✿"),
    HOT_PINK("Hot Pink", "♥"), ELECTRIC_BLUE("Electric", "✶"),
    AURORA("Aurora", "✧"), CORAL("Coral", "◠"),
    GOLD("Gold", "★"), NEON_GREEN("Neon Green", "▣")
}

fun AccentColor.toColor(): Color = when (this) {
    AccentColor.REI_BLUE -> Color(0xFF3DB4F2); AccentColor.CRUNCHYROLL_ORANGE -> Color(0xFFF47521)
    AccentColor.SAKURA_PINK -> Color(0xFFFF8BA7); AccentColor.OCEAN_TEAL -> Color(0xFF00BFA5)
    AccentColor.LAVENDER -> Color(0xFF7C4DFF); AccentColor.MINT_GREEN -> Color(0xFF69F0AE)
    AccentColor.SUNSET -> Color(0xFFFF6D00); AccentColor.VIOLET -> Color(0xFFAA00FF)
    AccentColor.CYAN -> Color(0xFF00E5FF); AccentColor.ROSE -> Color(0xFFFF4081)
    AccentColor.HOT_PINK -> Color(0xFFFF1493); AccentColor.ELECTRIC_BLUE -> Color(0xFF2979FF)
    AccentColor.AURORA -> Color(0xFF6B4DFF); AccentColor.CORAL -> Color(0xFFFF6F61)
    AccentColor.GOLD -> Color(0xFFFFD700); AccentColor.NEON_GREEN -> Color(0xFF39FF14)
}

// ═══════════════════════════════════════════
// ALL ENUM OPTIONS
// ═══════════════════════════════════════════
enum class CardStyle(val label: String) { STANDARD("Standard"), COMPACT("Compact"), MINIMAL("Minimal"), CINEMATIC("Cinematic"), GLASS("Glassmorphism"), NEON("Neon Outline") }
enum class HomeLayout(val label: String) { CAROUSEL("Hero Carousel"), GRID("Grid Overview"), COMPACT_LIST("Compact List"), MAGAZINE("Magazine") }
enum class ListDisplayMode(val label: String) { LIST("List"), GRID("Grid"), COMPACT("Compact") }
enum class ScoreStyle(val label: String) { STAR("Stars"), POINTS_10("10 Point"), POINTS_100("100 Point"), PERCENTAGE("Percentage"), SMILEY("Smiley") }
enum class BlurIntensity(val label: String, val radius: Int) { NONE("None", 0), LIGHT("Light", 5), MEDIUM("Medium", 15), HEAVY("Heavy", 25) }
enum class AnimSpeed(val label: String, val mult: Float) { SLOW("Slow", 1.5f), NORMAL("Normal", 1f), FAST("Fast", 0.6f), INSTANT("Instant", 0.1f) }
enum class NotifyStyle(val label: String) { MINIMAL("Minimal"), DETAILED("Detailed"), RICH("Rich Preview") }
enum class FontSize(val label: String, val scale: Float) { TINY("Tiny", 0.85f), SMALL("Small", 0.92f), MEDIUM("Medium", 1f), LARGE("Large", 1.08f), HUGE("Huge", 1.16f) }
enum class ContentScale(val label: String) { CROP("Crop"), FIT("Fit"), FILL_WIDTH("Fill Width") }
enum class SwipeAction(val label: String) { NONE("None"), TRACK("Track"), FAVORITE("Favorite"), SHARE("Share") }
enum class SearchEngine(val label: String) { ANILIST("AniList"), JIKAN("MyAnimeList"), KITSU("Kitsu"), ALL("All (Aggregated)") }
enum class SortDefault(val label: String) { POPULARITY("Popularity"), SCORE("Score"), TRENDING("Trending"), TITLE("Title A-Z"), NEWEST("Newest") }
enum class HapticIntensity(val label: String, val strength: Int) { OFF("Off", 0), LIGHT("Light", 1), MEDIUM("Medium", 2), STRONG("Strong", 3) }
enum class TransitionStyle(val label: String) { SLIDE("Slide"), FADE("Fade"), SCALE("Scale"), SHARED("Shared Element") }
enum class IndicatorStyle(val label: String) { DOT("Dot"), LINE("Line"), NUMBER("Number"), NONE("None") }
enum class SplashAfter(val label: String) { NEVER("Never"), WEEK("After 1 Week"), MONTH("After 1 Month"), ALWAYS("Always") }
enum class NavStyle(val label: String) { BOTTOM("Bottom Bar"), RAIL("Navigation Rail"), PILL("Pill Nav") }
enum class ImageQuality(val label: String) { LOW("Low (Save Data)"), MEDIUM("Medium"), HIGH("High"), ORIGINAL("Original") }
enum class FontFamily(val label: String) { SYSTEM("System Default"), INTER("Inter"), Poppins("Poppins"), NUNITO("Nunito"), ROBOTO_MONO("Roboto Mono") }
enum class CardLayout(val label: String) { VERTICAL("Vertical"), HORIZONTAL("Horizontal"), GRID("Grid") }
enum class ScrollBehavior(val label: String) { PAGED("Paged"), CONTINUOUS("Continuous"), SNAP("Snap") }
enum class DetailTabOrder(val label: String) { OVERVIEW_FIRST("Overview First"), EPISODES_FIRST("Episodes First"), RELATED_FIRST("Related First") }
enum class BannerStyle(val label: String) { FULL_BLEED("Full Bleed"), COMPACT("Compact"), PARALLAX("Parallax"), BLUR("Blurred") }
enum class StartupScreen(val label: String) { HOME("Home"), DISCOVER("Discover"), TRACKING("Tracking"), SEASONAL("Seasonal") }

// ═══════════════════════════════════════════
// MASTER CONFIG — 50+ options
// ═══════════════════════════════════════════
data class ReiConfig(
    // Theme
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.REI_BLUE,
    val secondaryAccent: AccentColor = AccentColor.ROSE,
    val isAmoled: Boolean = false,
    val useDynamicColors: Boolean = false,
    // Display
    val cardStyle: CardStyle = CardStyle.STANDARD,
    val homeLayout: HomeLayout = HomeLayout.CAROUSEL,
    val listDisplay: ListDisplayMode = ListDisplayMode.LIST,
    val scoreStyle: ScoreStyle = ScoreStyle.STAR,
    val fontSize: FontSize = FontSize.MEDIUM,
    val contentScale: ContentScale = ContentScale.CROP,
    val imageQuality: ImageQuality = ImageQuality.HIGH,
    // Show/hide toggles
    val showScoreOnCards: Boolean = true,
    val showFormatOnCards: Boolean = true,
    val showGenreChips: Boolean = true,
    val showStudioOnCard: Boolean = false,
    val showProgressOnCards: Boolean = true,
    val collapseSynopsis: Boolean = true,
    val useCoverAsBanner: Boolean = true,
    val showNextEpisode: Boolean = true,
    val showTrailerButton: Boolean = true,
    val showRecommendations: Boolean = true,
    val showReviews: Boolean = true,
    val showFranchise: Boolean = true,
    val showRelated: Boolean = true,
    val showCharacters: Boolean = true,
    val showStaff: Boolean = true,
    val showStatsOnDetail: Boolean = true,
    val showSourceInfo: Boolean = true,
    val showRankings: Boolean = true,
    val showAiringSchedule: Boolean = true,
    // Effects
    val blurIntensity: BlurIntensity = BlurIntensity.MEDIUM,
    val animSpeed: AnimSpeed = AnimSpeed.NORMAL,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    val transitionStyle: TransitionStyle = TransitionStyle.SLIDE,
    val indicatorStyle: IndicatorStyle = IndicatorStyle.DOT,
    val useParallax: Boolean = true,
    val useBlurBanner: Boolean = true,
    val useHaptic: Boolean = true,
    val useStaggeredAnimations: Boolean = true,
    val useParticleBackground: Boolean = false,
    val useGlowOnScore: Boolean = true,
    val useColorfulGenres: Boolean = true,
    // Layout tuning
    val borderRadius: Int = 14,
    val cardElevation: Int = 2,
    val bannerHeight: Int = 420,
    val gridColumns: Int = 3,
    val itemsPerRow: Int = 3,
    val customSurfaceAlpha: Float = 0.95f,
    val topBarAlpha: Float = 0.95f,
    val bottomBarAlpha: Float = 0.95f,
    val cardWidth: Int = 140,
    val cardHeight: Int = 200,
    val listItemSpacing: Int = 12,
    val sectionSpacing: Int = 16,
    val carouselAutoScrollMs: Int = 5000,
    // Behavior
    val useTranslucentBars: Boolean = true,
    val startScreen: String = "home",
    val defaultSort: SortDefault = SortDefault.POPULARITY,
    val searchEngine: SearchEngine = SearchEngine.ALL,
    val swipeLeftAction: SwipeAction = SwipeAction.TRACK,
    val swipeRightAction: SwipeAction = SwipeAction.FAVORITE,
    val navStyle: NavStyle = NavStyle.BOTTOM,
    val splashAfter: SplashAfter = SplashAfter.WEEK,
    val notifyStyle: NotifyStyle = NotifyStyle.DETAILED,
    val openDetailInOverlay: Boolean = false,
    val confirmBeforeTrack: Boolean = true,
    val showSpoilerTags: Boolean = false,
    val nsfwFilter: Boolean = true,
    val autoProgress: Boolean = true,
    val markCompleteAt: Int = 100,
    val longPressPreview: Boolean = true,
    val doubleTapFavorite: Boolean = true,
    // New premium options
    val fontFamily: FontFamily = FontFamily.SYSTEM,
    val cardLayout: CardLayout = CardLayout.VERTICAL,
    val scrollBehavior: ScrollBehavior = ScrollBehavior.CONTINUOUS,
    val detailTabOrder: DetailTabOrder = DetailTabOrder.OVERVIEW_FIRST,
    val bannerStyle: BannerStyle = BannerStyle.FULL_BLEED,
    val startupScreen: StartupScreen = StartupScreen.HOME,
    val useBlurNavBar: Boolean = false,
    val useBlurTopBar: Boolean = false,
    val showRankBadges: Boolean = true,
    val showAiringBadge: Boolean = true,
    val useCompactCards: Boolean = false,
    val showSourceOnDetail: Boolean = true,
    val showDurationOnDetail: Boolean = true,
    val showEpisodePreview: Boolean = true,
    val cardAspectRatio: Float = 0.7f,
    val carouselScale: Float = 0.95f,
    val maxLinesTitle: Int = 2,
    val showWaifuOfTheDay: Boolean = false,
    val usePremiumShimmer: Boolean = true,
    val showBreadcrumb: Boolean = true,
    val hapticOnScroll: Boolean = false,
    val swipeSensitivity: Int = 50,
    val maxCacheMb: Int = 50,
    val useWebSocket: Boolean = false,
    val notifyAiring: Boolean = true,
    val notifyFollows: Boolean = false,
    val notifyAnnouncements: Boolean = false
)

data class ThemeState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val accentColor: AccentColor = AccentColor.REI_BLUE,
    val isAmoled: Boolean = false,
    val config: ReiConfig = ReiConfig()
)

// ═══════════════════════════════════════════
// COMPOSITION LOCALS
// ═══════════════════════════════════════════
val LocalCardStyle = compositionLocalOf { CardStyle.STANDARD }
val LocalBorderRadius = compositionLocalOf { 14 }
val LocalCardElevation = compositionLocalOf { 2 }
val LocalUseCoverAsBanner = compositionLocalOf { true }
val LocalBlurIntensity = compositionLocalOf { BlurIntensity.MEDIUM }
val LocalAnimSpeed = compositionLocalOf { AnimSpeed.NORMAL }
val LocalScoreStyle = compositionLocalOf { ScoreStyle.STAR }
val LocalShowScoreOnCards = compositionLocalOf { true }
val LocalShowFormatOnCards = compositionLocalOf { true }
val LocalShowNextEpisode = compositionLocalOf { true }
val LocalBannerHeight = compositionLocalOf { 420 }
val LocalGridColumns = compositionLocalOf { 3 }
val LocalReiConfig = compositionLocalOf { ReiConfig() }
val LocalFontSize = compositionLocalOf { FontSize.MEDIUM }

// ═══════════════════════════════════════════
// MAIN THEME
// ═══════════════════════════════════════════
@Composable
fun ReiTheme(
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    accentColor: AccentColor = AccentColor.REI_BLUE,
    isAmoled: Boolean = false,
    config: ReiConfig = ReiConfig(),
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val isDark = when (themeMode) { ThemeMode.SYSTEM -> systemDark; ThemeMode.LIGHT -> false; else -> true }
    val colorScheme = when (themeMode) {
        ThemeMode.AMOLED -> amoledScheme(accentColor)
        ThemeMode.MIDNIGHT_DUSK -> midnightDuskScheme(accentColor)
        ThemeMode.STRAWBERRY -> strawberryScheme(accentColor)
        ThemeMode.OCEAN -> oceanScheme(accentColor)
        ThemeMode.SAKURA -> sakuraScheme(accentColor)
        ThemeMode.LAVENDER_NIGHT -> lavenderNightScheme(accentColor)
        ThemeMode.MATRIX -> matrixScheme(accentColor)
        ThemeMode.NUXT -> nuxtScheme(accentColor)
        ThemeMode.ROSE_GOLD -> roseGoldScheme(accentColor)
        ThemeMode.CYBERPUNK -> cyberpunkScheme(accentColor)
        ThemeMode.VAPORWAVE -> vaporwaveScheme(accentColor)
        ThemeMode.DEEP_PURPLE -> deepPurpleScheme(accentColor)
        ThemeMode.ARCTIC -> arcticScheme(accentColor)
        ThemeMode.TERRACOTTA -> terracottaScheme(accentColor)
        ThemeMode.LIGHT -> lightScheme(accentColor)
        else -> if (isDark) darkScheme(accentColor) else lightScheme(accentColor)
    }
    val sysUI = rememberSystemUiController()
    SideEffect {
        val barCol = if (isAmoled && isDark) Color.Black else colorScheme.surface
        sysUI.setSystemBarsColor(colorScheme.surface.copy(alpha = 0.4f), !isDark)
        sysUI.setNavigationBarColor(barCol, !isDark)
    }
    val base = ReiTypography
    val s = config.fontSize.scale
    val scaledTypo = Typography(
        displayLarge = base.displayLarge.copy(fontSize = base.displayLarge.fontSize * s),
        displayMedium = base.displayMedium.copy(fontSize = base.displayMedium.fontSize * s),
        displaySmall = base.displaySmall.copy(fontSize = base.displaySmall.fontSize * s),
        headlineLarge = base.headlineLarge.copy(fontSize = base.headlineLarge.fontSize * s),
        headlineMedium = base.headlineMedium.copy(fontSize = base.headlineMedium.fontSize * s),
        headlineSmall = base.headlineSmall.copy(fontSize = base.headlineSmall.fontSize * s),
        titleLarge = base.titleLarge.copy(fontSize = base.titleLarge.fontSize * s),
        titleMedium = base.titleMedium.copy(fontSize = base.titleMedium.fontSize * s),
        titleSmall = base.titleSmall.copy(fontSize = base.titleSmall.fontSize * s),
        bodyLarge = base.bodyLarge.copy(fontSize = base.bodyLarge.fontSize * s),
        bodyMedium = base.bodyMedium.copy(fontSize = base.bodyMedium.fontSize * s),
        bodySmall = base.bodySmall.copy(fontSize = base.bodySmall.fontSize * s),
        labelLarge = base.labelLarge.copy(fontSize = base.labelLarge.fontSize * s),
        labelMedium = base.labelMedium.copy(fontSize = base.labelMedium.fontSize * s),
        labelSmall = base.labelSmall.copy(fontSize = base.labelSmall.fontSize * s)
    )
    CompositionLocalProvider(
        LocalCardStyle provides config.cardStyle,
        LocalBorderRadius provides config.borderRadius,
        LocalCardElevation provides config.cardElevation,
        LocalUseCoverAsBanner provides config.useCoverAsBanner,
        LocalBlurIntensity provides config.blurIntensity,
        LocalAnimSpeed provides config.animSpeed,
        LocalScoreStyle provides config.scoreStyle,
        LocalShowScoreOnCards provides config.showScoreOnCards,
        LocalShowFormatOnCards provides config.showFormatOnCards,
        LocalShowNextEpisode provides config.showNextEpisode,
        LocalBannerHeight provides config.bannerHeight,
        LocalGridColumns provides config.gridColumns,
        LocalReiConfig provides config,
        LocalFontSize provides config.fontSize
    ) {
        MaterialTheme(colorScheme = colorScheme, typography = scaledTypo, content = content)
    }
}

// ═══════════════════════════════════════════
// COLOR SCHEMES
// ═══════════════════════════════════════════
@Composable
private fun darkScheme(a: AccentColor) = MaterialTheme.colorScheme.copy(primary = a.toColor(), onPrimary = Color.White, primaryContainer = a.toColor().copy(alpha = .15f), onPrimaryContainer = a.toColor(), secondary = a.toColor().copy(alpha = .8f), onSecondary = Color.White, tertiary = a.toColor().copy(alpha = .6f), background = Color(0xFF0F0F1A), onBackground = Color(0xFFE6E1E5), surface = Color(0xFF1A1A2E), onSurface = Color(0xFFE6E1E5), surfaceVariant = Color(0xFF252540), onSurfaceVariant = Color(0xFFCAC4D0), outline = Color(0xFF49454F), outlineVariant = Color(0xFF333347))
@Composable
private fun lightScheme(a: AccentColor) = MaterialTheme.colorScheme.copy(primary = a.toColor(), onPrimary = Color.White, primaryContainer = a.toColor().copy(alpha = .12f), onPrimaryContainer = a.toColor(), secondary = a.toColor().copy(alpha = .8f), onSecondary = Color.White, background = Color(0xFFFFFBFE), onBackground = Color(0xFF1C1B1F), surface = Color(0xFFFFFBFE), onSurface = Color(0xFF1C1B1F), surfaceVariant = Color(0xFFE7E0EC), onSurfaceVariant = Color(0xFF49454F), outline = Color(0xFF79747E))
@Composable
private fun amoledScheme(a: AccentColor) = darkScheme(a).copy(background = Color.Black, surface = Color.Black, surfaceVariant = Color(0xFF0D0D1A), outline = Color(0xFF1A1A2E))
@Composable
private fun midnightDuskScheme(a: AccentColor) = darkScheme(a).copy(secondary = Color(0xFFE85D75), tertiary = Color(0xFF6B4DFF), background = Color(0xFF0D0D1A), surface = Color(0xFF151528), surfaceVariant = Color(0xFF1E1E3A), outline = Color(0xFF3A3A5C))
@Composable
private fun strawberryScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFFF4081) else a.toColor(), secondary = Color(0xFFFF80AB), tertiary = Color(0xFFFF1744), background = Color(0xFF1A0A14), surface = Color(0xFF221018), surfaceVariant = Color(0xFF321A24), outline = Color(0xFF4A2838))
@Composable
private fun oceanScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF00BFA5) else a.toColor(), secondary = Color(0xFF0097A7), tertiary = Color(0xFF0091EA), background = Color(0xFF0A141A), surface = Color(0xFF0F1E28), surfaceVariant = Color(0xFF152A38), outline = Color(0xFF28404E))
@Composable
private fun sakuraScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFFF8BA7) else a.toColor(), secondary = Color(0xFFFFC1D4), tertiary = Color(0xFFF48FB1), background = Color(0xFF12081A), surface = Color(0xFF1A0E28), surfaceVariant = Color(0xFF241438), outline = Color(0xFF3E2854))
@Composable
private fun lavenderNightScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF7C4DFF) else a.toColor(), secondary = Color(0xFFB388FF), tertiary = Color(0xFF9C7CFF), background = Color(0xFF0D081A), surface = Color(0xFF161028), surfaceVariant = Color(0xFF201840), outline = Color(0xFF3A2860))
@Composable
private fun matrixScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF39FF14) else a.toColor(), secondary = Color(0xFF00E676), tertiary = Color(0xFF76FF03), background = Color(0xFF0A0A0A), surface = Color(0xFF0F150F), surfaceVariant = Color(0xFF152015), outline = Color(0xFF1A3A1A))
@Composable
private fun nuxtScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF00DC82) else a.toColor(), secondary = Color(0xFF00C972), tertiary = Color(0xFF10B981), background = Color(0xFF0E0E10), surface = Color(0xFF18181B), surfaceVariant = Color(0xFF27272A), outline = Color(0xFF3F3F46))
@Composable
private fun roseGoldScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFB76E79) else a.toColor(), secondary = Color(0xFFE8A0A0), tertiary = Color(0xFFF0C0C0), background = Color(0xFF1A1014), surface = Color(0xFF221520), surfaceVariant = Color(0xFF332028), outline = Color(0xFF442838))

@Composable
private fun cyberpunkScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFFF00FF) else a.toColor(), secondary = Color(0xFF00FFFF), tertiary = Color(0xFFFFFF00), background = Color(0xFF0A0012), surface = Color(0xFF12001E), surfaceVariant = Color(0xFF1A0030), outline = Color(0xFF2E0050))

@Composable
private fun vaporwaveScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFFF71CE) else a.toColor(), secondary = Color(0xFF01CDFE), tertiary = Color(0xFF05FFA1), background = Color(0xFF1A0A2E), surface = Color(0xFF221240), surfaceVariant = Color(0xFF2D1A55), outline = Color(0xFF3F2670))

@Composable
private fun deepPurpleScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF7B1FA2) else a.toColor(), secondary = Color(0xFFCE93D8), tertiary = Color(0xFFAB47BC), background = Color(0xFF0E0A1A), surface = Color(0xFF15102A), surfaceVariant = Color(0xFF1E1640), outline = Color(0xFF2A2058))

@Composable
private fun arcticScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFF80DEEA) else a.toColor(), secondary = Color(0xFFB2EBF2), tertiary = Color(0xFF4DD0E1), background = Color(0xFF0A1520), surface = Color(0xFF0F1E30), surfaceVariant = Color(0xFF152840), outline = Color(0xFF1E3A58))

@Composable
private fun terracottaScheme(a: AccentColor) = darkScheme(a).copy(primary = if (a == AccentColor.REI_BLUE) Color(0xFFE07A5F) else a.toColor(), secondary = Color(0xFFF2CC8F), tertiary = Color(0xFF81B29A), background = Color(0xFF1A1210), surface = Color(0xFF251A16), surfaceVariant = Color(0xFF33241E), outline = Color(0xFF443028))
