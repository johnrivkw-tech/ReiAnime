package com.rei.app.ui.screens.settings

import android.content.*
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, androidx.compose.foundation.layout.ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, vm: SettingsViewModel = hiltViewModel()) {
    val config by vm.config.collectAsState()
    val anilistConnected by vm.anilistLoggedIn.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var pick by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var showResetDialog by remember { mutableStateOf(false) }
    var showBackupExport by remember { mutableStateOf(false) }
    var showBackupImport by remember { mutableStateOf(false) }
    var backupJson by remember { mutableStateOf("") }
    var syncMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton({ showResetDialog = true }) { Icon(Icons.Outlined.RestartAlt, "Reset") }
                }
            )
        }
    ) { pv ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pv),
            verticalArrangement = Arrangement.spacedBy(0.dp),
            contentPadding = PaddingValues(vertical = 4.dp)
        ) {
            // CONNECTIONS
            item { SectionHeader(Icons.Outlined.Link, "Connections", Color(0xFF3DB4F2)) }
            item { ConnectionRow("\u25C8", "AniList", if (anilistConnected) "Connected \u2014 tap to reconnect" else "Not configured (set CLIENT_ID)", anilistConnected, Color(0xFF3DB4F2)) { vm.launchAnilistAuth(context) } }
            item { ConnectionRow("\u25C6", "MyAnimeList", if (vm.isMalConfigured()) "Tap to login via MAL" else "Not configured (set CLIENT_ID)", vm.isMalConfigured(), Color(0xFF2E51BE)) { vm.launchMalAuth(context) } }
            item { SettingsAction(Icons.Outlined.CloudDownload, "Import from MAL", "Pull your entire MAL list") { vm.importFromMal { syncMessage = it } } }
            item { SettingsAction(Icons.Outlined.Upload, "Export Backup", "Export all tracking data as JSON") { showBackupExport = true; vm.exportBackup { backupJson = it } } }
            item { SettingsAction(Icons.Outlined.CloudDownload, "Import Backup", "Restore data from a backup JSON") { showBackupImport = true } }
            item { SettingsAction(Icons.Outlined.SyncAlt, "Full Sync Now", "Bidirectional sync with AniList + MAL") { vm.fullSync { syncMessage = it } } }
            item { SettingsSwitch(Icons.Outlined.CloudSync, "Auto-Sync on Track", "Push changes when you track/update", true) { _ -> } }
            item { SettingsSwitch(Icons.Outlined.Sync, "Pull on Startup", "Fetch remote lists on app open", false) { _ -> } }
            if (syncMessage != null) item {
                Card(Modifier.padding(horizontal = 16.dp, vertical = 4.dp).fillMaxWidth(), shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.CheckCircle, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Text(syncMessage!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            }
            item { SectionDivider() }

            // APPEARANCE
            item { SectionHeader(Icons.Outlined.Palette, "Appearance", config.accentColor.toColor()) }
            item { CompactSelector("Theme Mode", ThemeMode.entries, config.themeMode, { vm.update { c -> c.copy(themeMode = it) } }, { it.label }, { it.icon }) }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Primary Accent", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Box(Modifier.size(20.dp).clip(CircleShape).background(config.accentColor.toColor()).border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape))
                    }
                    Spacer(Modifier.height(6.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        AccentColor.entries.forEach { accent ->
                            val sel = accent == config.accentColor
                            Surface(onClick = { vm.update { c -> c.copy(accentColor = accent) } }, shape = CircleShape, color = accent.toColor().copy(alpha = if (sel) 1f else 0.4f), border = if (sel) BorderStroke(2.dp, Color.White) else null, modifier = Modifier.size(32.dp)) {
                                if (sel) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(14.dp)) }
                            }
                        }
                    }
                }
            }
            item {
                Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Secondary Accent", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
                        Box(Modifier.size(20.dp).clip(CircleShape).background(config.secondaryAccent.toColor()).border(1.5.dp, MaterialTheme.colorScheme.secondary, CircleShape))
                    }
                    Spacer(Modifier.height(6.dp))
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(AccentColor.entries) { accent ->
                            val sel = accent == config.secondaryAccent
                            Surface(onClick = { vm.update { c -> c.copy(secondaryAccent = accent) } }, shape = CircleShape, color = accent.toColor().copy(alpha = if (sel) 1f else 0.3f), modifier = Modifier.size(28.dp)) {
                                if (sel) Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Icon(Icons.Filled.Check, null, tint = Color.White, modifier = Modifier.size(12.dp)) }
                            }
                        }
                    }
                }
            }
            item { SettingsSwitch(Icons.Outlined.Contrast, "AMOLED Black", "Pure black for OLED screens", config.isAmoled) { vm.update { c -> c.copy(isAmoled = it) } } }
            item { SettingsSwitch(Icons.Outlined.AutoAwesome, "Material You", "Dynamic colors from wallpaper", config.useDynamicColors) { vm.update { c -> c.copy(useDynamicColors = it) } } }
            item { SectionDivider() }

            // DISPLAY
            item { SectionHeader(Icons.Outlined.ViewAgenda, "Display", Color(0xFFFF4081)) }
            item { EnumRow(Icons.Outlined.ViewAgenda, "Card Style", config.cardStyle.label, { pick = "card" }) }
            item { EnumRow(Icons.Outlined.Home, "Home Layout", config.homeLayout.label, { pick = "home" }) }
            item { EnumRow(Icons.Outlined.List, "List Mode", config.listDisplay.label, { pick = "list" }) }
            item { EnumRow(Icons.Outlined.Star, "Score Style", config.scoreStyle.label, { pick = "score" }) }
            item { EnumRow(Icons.Outlined.TextFields, "Font Size", config.fontSize.label, { pick = "font" }) }
            item { EnumRow(Icons.Outlined.TextFields, "Font Family", config.fontFamily.label, { pick = "fontFamily" }) }
            item { EnumRow(Icons.Outlined.Image, "Content Scale", config.contentScale.label, { pick = "content" }) }
            item { EnumRow(Icons.Outlined.HighQuality, "Image Quality", config.imageQuality.label, { pick = "imgq" }) }
            item { EnumRow(Icons.Outlined.ViewAgenda, "Card Layout", config.cardLayout.label, { pick = "cardLayout" }) }
            item { EnumRow(Icons.Outlined.ViewCarousel, "Banner Style", config.bannerStyle.label, { pick = "bannerStyle" }) }
            item { SectionDivider() }

            // SHOW / HIDE
            item { SectionHeader(Icons.Outlined.Visibility, "Show / Hide", Color(0xFF4CAF50)) }
            item { ToggleGrid(items = listOf(
                ToggleItem("Score on Cards", config.showScoreOnCards) { vm.update { c -> c.copy(showScoreOnCards = it) } },
                ToggleItem("Format on Cards", config.showFormatOnCards) { vm.update { c -> c.copy(showFormatOnCards = it) } },
                ToggleItem("Genre Chips", config.showGenreChips) { vm.update { c -> c.copy(showGenreChips = it) } },
                ToggleItem("Studio on Cards", config.showStudioOnCard) { vm.update { c -> c.copy(showStudioOnCard = it) } },
                ToggleItem("Progress Bars", config.showProgressOnCards) { vm.update { c -> c.copy(showProgressOnCards = it) } },
                ToggleItem("Collapse Synopsis", config.collapseSynopsis) { vm.update { c -> c.copy(collapseSynopsis = it) } },
                ToggleItem("Cover as Banner", config.useCoverAsBanner) { vm.update { c -> c.copy(useCoverAsBanner = it) } },
                ToggleItem("Next Episode", config.showNextEpisode) { vm.update { c -> c.copy(showNextEpisode = it) } },
                ToggleItem("Trailer Button", config.showTrailerButton) { vm.update { c -> c.copy(showTrailerButton = it) } },
                ToggleItem("Recommendations", config.showRecommendations) { vm.update { c -> c.copy(showRecommendations = it) } },
                ToggleItem("Reviews", config.showReviews) { vm.update { c -> c.copy(showReviews = it) } },
                ToggleItem("Franchise", config.showFranchise) { vm.update { c -> c.copy(showFranchise = it) } },
                ToggleItem("Related Anime", config.showRelated) { vm.update { c -> c.copy(showRelated = it) } },
                ToggleItem("Characters", config.showCharacters) { vm.update { c -> c.copy(showCharacters = it) } },
                ToggleItem("Staff", config.showStaff) { vm.update { c -> c.copy(showStaff = it) } },
                ToggleItem("Stats on Detail", config.showStatsOnDetail) { vm.update { c -> c.copy(showStatsOnDetail = it) } },
                ToggleItem("Source Info", config.showSourceInfo) { vm.update { c -> c.copy(showSourceInfo = it) } },
                ToggleItem("Rankings", config.showRankings) { vm.update { c -> c.copy(showRankings = it) } },
                ToggleItem("Airing Schedule", config.showAiringSchedule) { vm.update { c -> c.copy(showAiringSchedule = it) } },
                ToggleItem("Rank Badges", config.showRankBadges) { vm.update { c -> c.copy(showRankBadges = it) } },
                ToggleItem("Airing Badge", config.showAiringBadge) { vm.update { c -> c.copy(showAiringBadge = it) } },
                ToggleItem("Duration on Detail", config.showDurationOnDetail) { vm.update { c -> c.copy(showDurationOnDetail = it) } },
                ToggleItem("Episode Preview", config.showEpisodePreview) { vm.update { c -> c.copy(showEpisodePreview = it) } },
                ToggleItem("Source on Detail", config.showSourceOnDetail) { vm.update { c -> c.copy(showSourceOnDetail = it) } },
                ToggleItem("Compact Cards", config.useCompactCards) { vm.update { c -> c.copy(useCompactCards = it) } },
                ToggleItem("Waifu of the Day", config.showWaifuOfTheDay) { vm.update { c -> c.copy(showWaifuOfTheDay = it) } },
                ToggleItem("Breadcrumbs", config.showBreadcrumb) { vm.update { c -> c.copy(showBreadcrumb = it) } }
            ))}
            item { SectionDivider() }

            // EFFECTS & ANIMATION
            item { SectionHeader(Icons.Outlined.AutoFixHigh, "Effects & Animation", Color(0xFF7C4DFF)) }
            item { EnumRow(Icons.Outlined.BlurOn, "Blur Intensity", config.blurIntensity.label, { pick = "blur" }) }
            item { EnumRow(Icons.Outlined.Speed, "Animation Speed", config.animSpeed.label, { pick = "anim" }) }
            item { EnumRow(Icons.Outlined.Vibration, "Haptic Intensity", config.hapticIntensity.label, { pick = "haptic" }) }
            item { EnumRow(Icons.Outlined.SwapHoriz, "Transitions", config.transitionStyle.label, { pick = "transition" }) }
            item { EnumRow(Icons.Outlined.ViewCarousel, "Indicators", config.indicatorStyle.label, { pick = "indicator" }) }
            item { SettingsSwitch(Icons.Outlined.ViewInAr, "Parallax Effect", "Banner parallax scroll", config.useParallax) { vm.update { c -> c.copy(useParallax = it) } } }
            item { SettingsSwitch(Icons.Outlined.BlurCircular, "Blur Banner", "Blur behind banner images", config.useBlurBanner) { vm.update { c -> c.copy(useBlurBanner = it) } } }
            item { SettingsSwitch(Icons.Outlined.TouchApp, "Haptic Feedback", "Vibration on interactions", config.useHaptic) { vm.update { c -> c.copy(useHaptic = it) } } }
            item { SettingsSwitch(Icons.Outlined.Animation, "Staggered Animations", "Items fade in sequence", config.useStaggeredAnimations) { vm.update { c -> c.copy(useStaggeredAnimations = it) } } }
            item { SettingsSwitch(Icons.Outlined.AutoFixHigh, "Particle Background", "Floating particles (beta)", config.useParticleBackground) { vm.update { c -> c.copy(useParticleBackground = it) } } }
            item { SettingsSwitch(Icons.Outlined.Grain, "Glow on Score", "Neon glow on score badges", config.useGlowOnScore) { vm.update { c -> c.copy(useGlowOnScore = it) } } }
            item { SettingsSwitch(Icons.Outlined.Palette, "Colorful Genres", "Unique colors per genre", config.useColorfulGenres) { vm.update { c -> c.copy(useColorfulGenres = it) } } }
            item { SettingsSwitch(Icons.Outlined.BlurOn, "Blur Nav Bar", "Glassmorphic bottom bar", config.useBlurNavBar) { vm.update { c -> c.copy(useBlurNavBar = it) } } }
            item { SettingsSwitch(Icons.Outlined.BlurOn, "Blur Top Bar", "Glassmorphic top bar", config.useBlurTopBar) { vm.update { c -> c.copy(useBlurTopBar = it) } } }
            item { SettingsSwitch(Icons.Outlined.AutoFixHigh, "Premium Shimmer", "Gradient sweep loading", config.usePremiumShimmer) { vm.update { c -> c.copy(usePremiumShimmer = it) } } }
            item { SettingsSwitch(Icons.Outlined.Vibration, "Haptic on Scroll", "Subtle haptic while scrolling", config.hapticOnScroll) { vm.update { c -> c.copy(hapticOnScroll = it) } } }
            item { SectionDivider() }

            // LAYOUT TUNING
            item { SectionHeader(Icons.Outlined.Tune, "Layout Tuning", Color(0xFFFFC107)) }
            item { PremiumSlider("Border Radius", config.borderRadius, 0..28, "dp") { vm.update { c -> c.copy(borderRadius = it) } } }
            item { PremiumSlider("Card Elevation", config.cardElevation, 0..12, "dp") { vm.update { c -> c.copy(cardElevation = it) } } }
            item { PremiumSlider("Banner Height", config.bannerHeight, 200..500, "dp") { vm.update { c -> c.copy(bannerHeight = it) } } }
            item { PremiumSlider("Grid Columns", config.gridColumns, 2..5, "") { vm.update { c -> c.copy(gridColumns = it) } } }
            item { PremiumSlider("Items Per Row", config.itemsPerRow, 2..6, "") { vm.update { c -> c.copy(itemsPerRow = it) } } }
            item { PremiumSlider("Surface Opacity", (config.customSurfaceAlpha * 100).toInt(), 50..100, "%") { vm.update { c -> c.copy(customSurfaceAlpha = it / 100f) } } }
            item { PremiumSlider("Top Bar Opacity", (config.topBarAlpha * 100).toInt(), 50..100, "%") { vm.update { c -> c.copy(topBarAlpha = it / 100f) } } }
            item { PremiumSlider("Bottom Bar Opacity", (config.bottomBarAlpha * 100).toInt(), 50..100, "%") { vm.update { c -> c.copy(bottomBarAlpha = it / 100f) } } }
            item { PremiumSlider("Card Width", config.cardWidth, 80..200, "dp") { vm.update { c -> c.copy(cardWidth = it) } } }
            item { PremiumSlider("Card Height", config.cardHeight, 100..300, "dp") { vm.update { c -> c.copy(cardHeight = it) } } }
            item { PremiumSlider("Card Aspect Ratio", (config.cardAspectRatio * 100).toInt(), 50..90, "%") { vm.update { c -> c.copy(cardAspectRatio = it / 100f) } } }
            item { PremiumSlider("List Spacing", config.listItemSpacing, 4..24, "dp") { vm.update { c -> c.copy(listItemSpacing = it) } } }
            item { PremiumSlider("Section Spacing", config.sectionSpacing, 8..32, "dp") { vm.update { c -> c.copy(sectionSpacing = it) } } }
            item { PremiumSlider("Carousel Speed", config.carouselAutoScrollMs, 2000..10000, "ms") { vm.update { c -> c.copy(carouselAutoScrollMs = it) } } }
            item { PremiumSlider("Carousel Scale", (config.carouselScale * 100).toInt(), 80..100, "%") { vm.update { c -> c.copy(carouselScale = it / 100f) } } }
            item { PremiumSlider("Title Max Lines", config.maxLinesTitle, 1..4, "") { vm.update { c -> c.copy(maxLinesTitle = it) } } }
            item { SectionDivider() }

            // BEHAVIOR
            item { SectionHeader(Icons.Outlined.SettingsSuggest, "Behavior", Color(0xFFFF6D00)) }
            item { EnumRow(Icons.Outlined.Search, "Search Engine", config.searchEngine.label, { pick = "search" }) }
            item { EnumRow(Icons.Outlined.Sort, "Default Sort", config.defaultSort.label, { pick = "sort" }) }
            item { EnumRow(Icons.Outlined.Swipe, "Swipe Left", config.swipeLeftAction.label, { pick = "swipeL" }) }
            item { EnumRow(Icons.Outlined.SwipeRight, "Swipe Right", config.swipeRightAction.label, { pick = "swipeR" }) }
            item { EnumRow(Icons.Outlined.Navigation, "Nav Style", config.navStyle.label, { pick = "nav" }) }
            item { EnumRow(Icons.Outlined.SmartDisplay, "Splash Screen", config.splashAfter.label, { pick = "splash" }) }
            item { EnumRow(Icons.Outlined.Notifications, "Notification Style", config.notifyStyle.label, { pick = "notify" }) }
            item { EnumRow(Icons.Outlined.Tab, "Detail Tab Order", config.detailTabOrder.label, { pick = "detailTabOrder" }) }
            item { EnumRow(Icons.Outlined.Navigation, "Startup Screen", config.startupScreen.label, { pick = "startupScreen" }) }
            item { EnumRow(Icons.Outlined.ViewCarousel, "Scroll Behavior", config.scrollBehavior.label, { pick = "scrollBehavior" }) }
            item { SettingsSwitch(Icons.Outlined.Visibility, "Translucent Bars", "System bar transparency", config.useTranslucentBars) { vm.update { c -> c.copy(useTranslucentBars = it) } } }
            item { SettingsSwitch(Icons.Outlined.PictureInPicture, "Overlay Detail", "Open detail in overlay", config.openDetailInOverlay) { vm.update { c -> c.copy(openDetailInOverlay = it) } } }
            item { SettingsSwitch(Icons.Outlined.HelpOutline, "Confirm Before Track", "Show confirmation dialog", config.confirmBeforeTrack) { vm.update { c -> c.copy(confirmBeforeTrack = it) } } }
            item { SettingsSwitch(Icons.Outlined.VisibilityOff, "Show Spoiler Tags", null, config.showSpoilerTags) { vm.update { c -> c.copy(showSpoilerTags = it) } } }
            item { SettingsSwitch(Icons.Outlined.Block, "NSFW Filter", "Hide adult content", config.nsfwFilter) { vm.update { c -> c.copy(nsfwFilter = it) } } }
            item { SettingsSwitch(Icons.Outlined.AutoMode, "Auto-Progress", "Auto increment on watch", config.autoProgress) { vm.update { c -> c.copy(autoProgress = it) } } }
            item { SettingsSwitch(Icons.Outlined.TouchApp, "Long Press Preview", "Preview on long press", config.longPressPreview) { vm.update { c -> c.copy(longPressPreview = it) } } }
            item { SettingsSwitch(Icons.Outlined.Favorite, "Double-Tap Favorite", "Double tap to favorite", config.doubleTapFavorite) { vm.update { c -> c.copy(doubleTapFavorite = it) } } }
            item { PremiumSlider("Mark Complete At %", config.markCompleteAt, 80..100, "%") { vm.update { c -> c.copy(markCompleteAt = it) } } }
            item { PremiumSlider("Swipe Sensitivity", config.swipeSensitivity, 10..100, "") { vm.update { c -> c.copy(swipeSensitivity = it) } } }
            item { SectionDivider() }

            // NOTIFICATIONS
            item { SectionHeader(Icons.Outlined.Notifications, "Notifications", Color(0xFFE91E63)) }
            item { SettingsSwitch(Icons.Outlined.Notifications, "Airing Reminders", "Notify before episodes air", config.notifyAiring) { vm.update { c -> c.copy(notifyAiring = it) } } }
            item { SettingsSwitch(Icons.Outlined.PersonOutline, "Follow Activity", "Notify on follows' activity", config.notifyFollows) { vm.update { c -> c.copy(notifyFollows = it) } } }
            item { SettingsSwitch(Icons.Outlined.Campaign, "Announcements", "App news and updates", config.notifyAnnouncements) { vm.update { c -> c.copy(notifyAnnouncements = it) } } }
            item { EnumRow(Icons.Outlined.Notifications, "Notification Style", config.notifyStyle.label, { pick = "notify" }) }
            item { SectionDivider() }

            // DATA & STORAGE
            item { SectionHeader(Icons.Outlined.Storage, "Data & Storage", Color(0xFF009688)) }
            item { PremiumSlider("HTTP Cache", config.maxCacheMb, 10..100, "MB") { vm.update { c -> c.copy(maxCacheMb = it) } } }
            item { SettingsAction(Icons.Outlined.DeleteSweep, "Clear Image Cache", "Free up disk space") { Toast.makeText(context, "Cache cleared", Toast.LENGTH_SHORT).show() } }
            item { SettingsAction(Icons.Outlined.DeleteForever, "Clear Search History", "Remove all recent searches") { Toast.makeText(context, "Search history cleared", Toast.LENGTH_SHORT).show() } }
            item { SettingsAction(Icons.Outlined.RestartAlt, "Reset All Settings", "Restore all defaults") { showResetDialog = true } }
            item { SettingsSwitch(Icons.Outlined.CloudSync, "WebSocket (Beta)", "Real-time via Notify.moe", config.useWebSocket) { vm.update { c -> c.copy(useWebSocket = it) } } }
            item { SectionDivider() }

            // ABOUT
            item { SectionHeader(Icons.Outlined.Info, "About", MaterialTheme.colorScheme.onSurfaceVariant) }
            item { AboutRow("Rei \u96F6", "v2.2.0 \u2022 Made with \u2665") }
            item { AboutRow("Tech Stack", "Kotlin \u2022 Compose \u2022 KSP \u2022 Room \u2022 Hilt \u2022 DataStore") }
            item { AboutRow("API Sources", "AniList \u2022 MAL \u2022 Kitsu \u2022 Shikimori \u2022 Trace.moe \u2022 LiveChart \u2022 AnimeSchedule \u2022 Waifu.im") }
            item { AboutRow("Features", "18 themes \u2022 80+ options \u2022 Bidirectional sync \u2022 Coin economy") }
            item { Spacer(Modifier.height(32.dp)) }
        }
    }

    // ═══ ALL PICKER DIALOGS ═══
    if (pick == "theme") EnumPicker("Theme", ThemeMode.entries, config.themeMode, { vm.update { c -> c.copy(themeMode = it) } }, { it.label + " " + it.icon }) { pick = null }
    if (pick == "accent") EnumPicker("Accent", AccentColor.entries, config.accentColor, { vm.update { c -> c.copy(accentColor = it) } }, { it.label + " " + it.emoji }) { pick = null }
    if (pick == "accent2") EnumPicker("2nd Accent", AccentColor.entries, config.secondaryAccent, { vm.update { c -> c.copy(secondaryAccent = it) } }, { it.label + " " + it.emoji }) { pick = null }
    if (pick == "card") EnumPicker("Card Style", CardStyle.entries, config.cardStyle, { vm.update { c -> c.copy(cardStyle = it) } }, { it.label }) { pick = null }
    if (pick == "home") EnumPicker("Home Layout", HomeLayout.entries, config.homeLayout, { vm.update { c -> c.copy(homeLayout = it) } }, { it.label }) { pick = null }
    if (pick == "list") EnumPicker("List Display", ListDisplayMode.entries, config.listDisplay, { vm.update { c -> c.copy(listDisplay = it) } }, { it.label }) { pick = null }
    if (pick == "score") EnumPicker("Score Style", ScoreStyle.entries, config.scoreStyle, { vm.update { c -> c.copy(scoreStyle = it) } }, { it.label }) { pick = null }
    if (pick == "font") EnumPicker("Font Size", FontSize.entries, config.fontSize, { vm.update { c -> c.copy(fontSize = it) } }, { it.label }) { pick = null }
    if (pick == "content") EnumPicker("Content Scale", ContentScale.entries, config.contentScale, { vm.update { c -> c.copy(contentScale = it) } }, { it.label }) { pick = null }
    if (pick == "imgq") EnumPicker("Image Quality", ImageQuality.entries, config.imageQuality, { vm.update { c -> c.copy(imageQuality = it) } }, { it.label }) { pick = null }
    if (pick == "blur") EnumPicker("Blur", BlurIntensity.entries, config.blurIntensity, { vm.update { c -> c.copy(blurIntensity = it) } }, { it.label }) { pick = null }
    if (pick == "anim") EnumPicker("Animation", AnimSpeed.entries, config.animSpeed, { vm.update { c -> c.copy(animSpeed = it) } }, { it.label }) { pick = null }
    if (pick == "haptic") EnumPicker("Haptic", HapticIntensity.entries, config.hapticIntensity, { vm.update { c -> c.copy(hapticIntensity = it) } }, { it.label }) { pick = null }
    if (pick == "transition") EnumPicker("Transition", TransitionStyle.entries, config.transitionStyle, { vm.update { c -> c.copy(transitionStyle = it) } }, { it.label }) { pick = null }
    if (pick == "indicator") EnumPicker("Indicator", IndicatorStyle.entries, config.indicatorStyle, { vm.update { c -> c.copy(indicatorStyle = it) } }, { it.label }) { pick = null }
    if (pick == "search") EnumPicker("Search Engine", SearchEngine.entries, config.searchEngine, { vm.update { c -> c.copy(searchEngine = it) } }, { it.label }) { pick = null }
    if (pick == "sort") EnumPicker("Default Sort", SortDefault.entries, config.defaultSort, { vm.update { c -> c.copy(defaultSort = it) } }, { it.label }) { pick = null }
    if (pick == "swipeL") EnumPicker("Swipe Left", SwipeAction.entries, config.swipeLeftAction, { vm.update { c -> c.copy(swipeLeftAction = it) } }, { it.label }) { pick = null }
    if (pick == "swipeR") EnumPicker("Swipe Right", SwipeAction.entries, config.swipeRightAction, { vm.update { c -> c.copy(swipeRightAction = it) } }, { it.label }) { pick = null }
    if (pick == "nav") EnumPicker("Nav Style", NavStyle.entries, config.navStyle, { vm.update { c -> c.copy(navStyle = it) } }, { it.label }) { pick = null }
    if (pick == "splash") EnumPicker("Splash", SplashAfter.entries, config.splashAfter, { vm.update { c -> c.copy(splashAfter = it) } }, { it.label }) { pick = null }
    if (pick == "notify") EnumPicker("Notifications", NotifyStyle.entries, config.notifyStyle, { vm.update { c -> c.copy(notifyStyle = it) } }, { it.label }) { pick = null }
    if (pick == "fontFamily") EnumPicker("Font Family", FontFamily.entries, config.fontFamily, { vm.update { c -> c.copy(fontFamily = it) } }, { it.label }) { pick = null }
    if (pick == "cardLayout") EnumPicker("Card Layout", CardLayout.entries, config.cardLayout, { vm.update { c -> c.copy(cardLayout = it) } }, { it.label }) { pick = null }
    if (pick == "bannerStyle") EnumPicker("Banner Style", BannerStyle.entries, config.bannerStyle, { vm.update { c -> c.copy(bannerStyle = it) } }, { it.label }) { pick = null }
    if (pick == "detailTabOrder") EnumPicker("Detail Tab Order", DetailTabOrder.entries, config.detailTabOrder, { vm.update { c -> c.copy(detailTabOrder = it) } }, { it.label }) { pick = null }
    if (pick == "startupScreen") EnumPicker("Startup Screen", StartupScreen.entries, config.startupScreen, { vm.update { c -> c.copy(startupScreen = it) } }, { it.label }) { pick = null }
    if (pick == "scrollBehavior") EnumPicker("Scroll", ScrollBehavior.entries, config.scrollBehavior, { vm.update { c -> c.copy(scrollBehavior = it) } }, { it.label }) { pick = null }

    // ═══ Reset Confirmation Dialog ═══
    if (showResetDialog) {
        AlertDialog(
            onDismissRequest = { showResetDialog = false },
            icon = { Icon(Icons.Outlined.Warning, null, tint = Color(0xFFFF5722)) },
            title = { Text("Reset All Settings?", fontWeight = FontWeight.Bold) },
            text = { Text("This will restore all settings to their defaults. Your tracking data and coins will be preserved. This cannot be undone.") },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        vm.resetAllSettings()
                        showResetDialog = false
                        Toast.makeText(context, "Settings reset to defaults", Toast.LENGTH_SHORT).show()
                    },
                    colors = ButtonDefaults.filledTonalButtonColors(containerColor = Color(0xFFFF5722).copy(alpha = 0.15f), contentColor = Color(0xFFFF5722))
                ) { Text("Reset") }
            },
            dismissButton = { TextButton({ showResetDialog = false }) { Text("Cancel") } }
        )
    }

    // ═══ Backup Export Dialog ═══
    if (showBackupExport && backupJson.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showBackupExport = false; backupJson = "" },
            title = { Text("Export Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Your backup data is ready. Copy it or share it to save.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Card(shape = RoundedCornerShape(8.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))) {
                        Text(backupJson.take(500) + if (backupJson.length > 500) "..." else "", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(8.dp), maxLines = 8, overflow = TextOverflow.Ellipsis)
                    }
                    Text("${backupJson.length} chars", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                FilledTonalButton(onClick = {
                    // Copy to clipboard
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Rei Backup", backupJson))
                    Toast.makeText(context, "Backup copied to clipboard", Toast.LENGTH_SHORT).show()
                    showBackupExport = false; backupJson = ""
                }) { Text("Copy to Clipboard") }
            },
            dismissButton = { TextButton({ showBackupExport = false; backupJson = "" }) { Text("Cancel") } }
        )
    }

    // ═══ Backup Import Dialog ═══
    if (showBackupImport) {
        var importText by remember { mutableStateOf("") }
        var importPreview by remember { mutableStateOf<String?>(null) }
        AlertDialog(
            onDismissRequest = { showBackupImport = false },
            title = { Text("Import Backup", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Paste your backup JSON below:", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    OutlinedTextField(
                        value = importText,
                        onValueChange = { importText = it; importPreview = null },
                        modifier = Modifier.fillMaxWidth().height(120.dp),
                        placeholder = { Text("Paste JSON here...") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    if (importPreview != null) {
                        Text(importPreview!!, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                    }
                }
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = {
                        val preview = vm.previewBackup(importText)
                        importPreview = if (preview != null) "v${preview.version} \u2022 ${preview.tracking.size} tracking \u2022 ${preview.episodes.size} episodes \u2022 ${preview.economy.size} economy" else "Invalid backup format"
                    }) { Text("Preview") }
                    FilledTonalButton(onClick = {
                        vm.importBackup(importText) { result ->
                            Toast.makeText(context, result, Toast.LENGTH_SHORT).show()
                        }
                        showBackupImport = false
                    }, enabled = importText.isNotBlank()) { Text("Import") }
                }
            },
            dismissButton = { TextButton({ showBackupImport = false }) { Text("Cancel") } }
        )
    }
}

// ═══════════════════════════════════════════
// PREMIUM SETTINGS COMPONENTS
// ═══════════════════════════════════════════

@Composable
private fun SectionHeader(icon: ImageVector, title: String, tint: Color) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Surface(shape = RoundedCornerShape(8.dp), color = tint.copy(alpha = 0.12f), modifier = Modifier.size(32.dp)) {
            Box(contentAlignment = Alignment.Center) { Icon(icon, null, tint = tint, modifier = Modifier.size(18.dp)) }
        }
        Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = tint)
    }
}

@Composable
private fun SectionDivider() = HorizontalDivider(Modifier.padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

@Composable
private fun SettingsAction(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun EnumRow(icon: ImageVector, title: String, value: String, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 11.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, modifier = Modifier.weight(1f))
            Text(value, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun SettingsSwitch(icon: ImageVector, title: String, subtitle: String?, checked: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp).clickable { onChange(!checked) }, verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
        Icon(icon, null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(22.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            subtitle?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
        }
        Switch(checked, onChange, modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PremiumSlider(label: String, value: Int, range: IntRange, unit: String, onChange: (Int) -> Unit) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 2.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
            Text("$value$unit", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
        }
        Slider(value.toFloat(), { onChange(it.toInt()) }, valueRange = range.first.toFloat()..range.last.toFloat(), steps = maxOf(1, (range.last - range.first) / 4), modifier = Modifier.fillMaxWidth())
    }
}

@Composable
private fun <T> CompactSelector(label: String, itemList: List<T>, current: T, onSelect: (T) -> Unit, textLabel: (T) -> String, icon: (T) -> String) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            items(itemList) { item ->
                val sel = item == current
                Surface(onClick = { onSelect(item) }, shape = RoundedCornerShape(10.dp), color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)) else null) {
                    Row(Modifier.padding(horizontal = 10.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(icon(item), fontSize = 12.sp)
                        Text(textLabel(item), style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectionRow(icon: String, service: String, status: String, connected: Boolean, color: Color, onClick: () -> Unit) {
    Surface(onClick = onClick, color = Color.Transparent) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(icon, fontSize = 18.sp, color = color)
            Column(Modifier.weight(1f)) {
                Text(service, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                Text(status, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Surface(shape = RoundedCornerShape(12.dp), color = if (connected) Color(0xFF4CAF50).copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                Text(if (connected) "Connected" else "Connect", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (connected) Color(0xFF4CAF50) else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
            }
        }
    }
}

@Composable
private fun AboutRow(title: String, value: String) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 4.dp)) {
        Text(title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun ToggleGrid(items: List<ToggleItem>) {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        items.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(0.dp)) {
                row.forEach { item ->
                    Row(Modifier.weight(1f).clickable { item.onChange(!item.checked) }.padding(vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Switch(item.checked, item.onChange, modifier = Modifier.height(20.dp).width(36.dp))
                        Text(item.label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

private data class ToggleItem(val label: String, val checked: Boolean, val onChange: (Boolean) -> Unit)

@Composable
private fun <T> EnumPicker(title: String, items: List<T>, current: T, onSelect: (T) -> Unit, label: (T) -> String, onDismiss: () -> Unit) = AlertDialog(
    onDismissRequest = onDismiss,
    title = { Text(title, fontWeight = FontWeight.Bold) },
    text = {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            items.forEach { item ->
                val sel = item == current
                Surface(onClick = { onSelect(item); onDismiss() }, color = if (sel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent, shape = RoundedCornerShape(8.dp)) {
                    Row(Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        RadioButton(sel, onClick = { onSelect(item); onDismiss() })
                        Text(label(item), fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    },
    confirmButton = { TextButton(onDismiss) { Text("Cancel") } }
)
