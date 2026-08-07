package com.rei.app.ui

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import com.rei.app.navigation.ReiNavHost
import com.rei.app.navigation.Route
import com.rei.app.ui.theme.LocalReiConfig
import com.rei.app.ui.theme.NavStyle
import com.rei.app.ui.theme.ReiTheme
import com.rei.app.ui.theme.ThemeViewModel
import com.rei.app.util.DeepLinkHandler
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject lateinit var themeViewModel: ThemeViewModel

    // Deep link destination to navigate to after compose setup
    private var pendingDeepLinkId by mutableStateOf<Int?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splash = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        splash.setKeepOnScreenCondition { false }

        // Handle initial deep link
        handleIncomingDeepLink(intent)

        setContent {
            val ts by themeViewModel.themeState.collectAsState()
            ReiTheme(ts.themeMode, ts.accentColor, ts.isAmoled, ts.config) {
                MainContent(initialAnimeId = pendingDeepLinkId, onDeepLinkConsumed = { pendingDeepLinkId = null })
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingDeepLink(intent)
    }

    private fun handleIncomingDeepLink(intent: Intent) {
        when (val result = DeepLinkHandler.resolve(intent)) {
            is DeepLinkHandler.DeepLinkResult.AnimeDetail -> {
                pendingDeepLinkId = result.animeId
            }
            is DeepLinkHandler.DeepLinkResult.AniListAuth -> {
                // Handled by AniListAuth callback in DataStore
            }
            is DeepLinkHandler.DeepLinkResult.MalAuth -> {
                // Handled by MALAuth callback in DataStore
            }
            is DeepLinkHandler.DeepLinkResult.Unhandled -> { }
        }
    }
}

data class NavI(val route: String, val label: String, val sel: ImageVector, val unsel: ImageVector)
val mainNav = listOf(
    NavI(Route.Home.route, "Home", Icons.Filled.Home, Icons.Outlined.Home),
    NavI(Route.Search.route, "Search", Icons.Filled.Search, Icons.Outlined.Search),
    NavI(Route.Discover.route, "Discover", Icons.Filled.Explore, Icons.Outlined.Explore),
    NavI(Route.Streaming.route, "Watch", Icons.Filled.LiveTv, Icons.Outlined.LiveTv),
    NavI(Route.Profile.route, "Profile", Icons.Filled.Person, Icons.Outlined.PersonOutline)
)

@Composable
fun MainContent(initialAnimeId: Int? = null, onDeepLinkConsumed: () -> Unit = {}) {
    val nav = androidx.navigation.compose.rememberNavController()
    val entry by nav.currentBackStackEntryAsState()
    val dest = entry?.destination
    val showBar = mainNav.any { dest?.hierarchy?.any { d -> d.route == it.route } == true }
    val showFab = dest?.route in listOf(Route.Home.route, Route.Search.route, Route.Discover.route)
    val config = LocalReiConfig.current

    // Handle deep link navigation
    LaunchedEffect(initialAnimeId) {
        if (initialAnimeId != null) {
            nav.navigate(Route.AnimeDetail.create(initialAnimeId))
            onDeepLinkConsumed()
        }
    }

    when (config.navStyle) {
        NavStyle.RAIL -> RailNavLayout(nav, dest, showFab)
        NavStyle.PILL -> PillNavLayout(nav, dest, showFab)
        else -> BottomNavLayout(nav, dest, showBar, showFab)
    }
}

// ═══════════════════════════════════════════
// BOTTOM NAV (default)
// ═══════════════════════════════════════════
@Composable
private fun BottomNavLayout(nav: androidx.navigation.NavHostController, dest: androidx.navigation.NavDestination?, showBar: Boolean, showFab: Boolean) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            AnimatedVisibility(showBar, enter = slideInVertically { it }, exit = slideOutVertically { it }) {
                NavigationBar(
                    modifier = Modifier.fillMaxWidth().windowInsetsPadding(WindowInsets.systemBars.only(WindowInsetsSides.Bottom)),
                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                    tonalElevation = 3.dp
                ) {
                    mainNav.forEach { item ->
                        val sel = dest?.hierarchy?.any { it.route == item.route } == true
                        NavigationBarItem(
                            icon = { Icon(if (sel) item.sel else item.unsel, item.label, modifier = Modifier.graphicsLayer { val s = if (sel) 1.15f else 1f; scaleX = s; scaleY = s }) },
                            label = { Text(item.label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, style = MaterialTheme.typography.labelSmall) },
                            selected = sel,
                            onClick = { if (dest?.route != item.route) nav.navigate(item.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                            colors = NavigationBarItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
            }
        }
    ) { innerPadding -> NavContent(nav, innerPadding, showFab) }
}

// ═══════════════════════════════════════════
// NAVIGATION RAIL (tablet / landscape)
// ═══════════════════════════════════════════
@Composable
private fun RailNavLayout(nav: androidx.navigation.NavHostController, dest: androidx.navigation.NavDestination?, showFab: Boolean) {
    Row(Modifier.fillMaxSize()) {
        NavigationRail(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f)
        ) {
            mainNav.forEach { item ->
                val sel = dest?.hierarchy?.any { it.route == item.route } == true
                NavigationRailItem(
                    icon = { Icon(if (sel) item.sel else item.unsel, item.label) },
                    label = { Text(item.label, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal) },
                    selected = sel,
                    onClick = { if (dest?.route != item.route) nav.navigate(item.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                    colors = NavigationRailItemDefaults.colors(selectedIconColor = MaterialTheme.colorScheme.primary, selectedTextColor = MaterialTheme.colorScheme.primary, indicatorColor = MaterialTheme.colorScheme.primaryContainer)
                )
            }
        }
        NavContent(nav, PaddingValues(), showFab)
    }
}

// ═══════════════════════════════════════════
// PILL NAV (floating bottom)
// ═══════════════════════════════════════════
@Composable
private fun PillNavLayout(nav: androidx.navigation.NavHostController, dest: androidx.navigation.NavDestination?, showFab: Boolean) {
    Box(Modifier.fillMaxSize()) {
        // Main content
        NavContent(nav, PaddingValues(bottom = 80.dp), showFab)

        // Floating pill
        if (dest != null) {
            Row(
                modifier = Modifier.align(Alignment.BottomCenter)
                    .padding(bottom = 24.dp)
                    .clip(RoundedCornerShape(28.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                mainNav.forEach { item ->
                    val sel = dest.hierarchy.any { it.route == item.route }
                    Surface(
                        onClick = { if (dest.route != item.route) nav.navigate(item.route) { popUpTo(nav.graph.startDestinationId) { saveState = true }; launchSingleTop = true; restoreState = true } },
                        shape = RoundedCornerShape(20.dp),
                        color = if (sel) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
                        modifier = Modifier.size(width = 64.dp, height = 48.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(if (sel) item.sel else item.unsel, item.label, modifier = Modifier.size(20.dp), tint = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(item.label, style = MaterialTheme.typography.labelSmall, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal, color = if (sel) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NavContent(nav: androidx.navigation.NavHostController, innerPadding: PaddingValues, showFab: Boolean) {
    Box(Modifier.fillMaxSize().padding(innerPadding)) {
        ReiNavHost(nav)
        // FABs
        if (showFab) {
            Column(
                modifier = Modifier.align(Alignment.BottomEnd).padding(end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                horizontalAlignment = Alignment.End
            ) {
                SmallFloatingActionButton(
                    onClick = { nav.navigate(Route.ImageSearch.route) },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                    contentColor = MaterialTheme.colorScheme.onTertiaryContainer
                ) { Icon(Icons.Filled.PhotoCamera, "Scene Search") }

                SmallFloatingActionButton(
                    onClick = { nav.navigate(Route.Economy.route) },
                    containerColor = androidx.compose.ui.graphics.Color(0xFF7C4DFF).copy(alpha = 0.15f),
                    contentColor = androidx.compose.ui.graphics.Color(0xFF7C4DFF)
                ) { Icon(Icons.Filled.ShoppingBag, "Shop") }

                FloatingActionButton(
                    onClick = { nav.navigate(Route.Random.route) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(16.dp)
                ) { Icon(Icons.Filled.Casino, "Random") }
            }
        }
    }
}
