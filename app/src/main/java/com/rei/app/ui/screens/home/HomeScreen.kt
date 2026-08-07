package com.rei.app.ui.screens.home

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.Anime
import com.rei.app.ui.components.*
import com.rei.app.ui.theme.LocalReiConfig
import com.rei.app.navigation.Route
import androidx.navigation.NavHostController
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onAnimeClick: (Int) -> Unit, nav: NavHostController? = null, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val config = LocalReiConfig.current
    var isRefreshing by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val greeting = when { hour < 12 -> "Good Morning"; hour < 17 -> "Good Afternoon"; hour < 21 -> "Good Evening"; else -> "Late Night" }

    Scaffold(modifier = Modifier.nestedScroll(scroll.nestedScrollConnection), topBar = {
        TopAppBar(
            title = {
                Column {
                    Text(greeting, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text("Rei", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Black)
                        Text("\u96F6", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold)
                    }
                }
            },
            actions = {
                IconButton({ nav?.navigate(Route.Calendar.route) }) { Icon(Icons.Outlined.CalendarMonth, "Schedule", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                IconButton({ nav?.navigate(Route.WaifuGallery.route) }) { Icon(Icons.Outlined.Image, "Gallery", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                BadgedBox(badge = { Badge { } }) { IconButton({ }) { Icon(Icons.Outlined.Notifications, "Notifications") } }
            },
            scrollBehavior = scroll,
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
        )
    }) { pv ->
        when (state) {
            is HomeState.Loading -> HomeLoadingContent(pv)
            is HomeState.Success -> {
                val d = state as HomeState.Success
                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = { isRefreshing = true; vm.refresh(); scope.launch { kotlinx.coroutines.delay(1500); isRefreshing = false } },
                    modifier = Modifier.fillMaxSize().padding(pv)
                ) {
                    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(20.dp)) {
                        item { HeroCarousel(d.trending, onAnimeClick, Modifier.fillMaxWidth()) }

                        // Premium Quick Actions — 2 rows of 3
                        item {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PremiumQuickAction("\u25B6", "Schedule", Color(0xFF4CAF50), { nav?.navigate(Route.Calendar.route) }, Modifier.weight(1f))
                                    PremiumQuickAction("\u2726", "Gallery", Color(0xFF7C4DFF), { nav?.navigate(Route.WaifuGallery.route) }, Modifier.weight(1f))
                                    PremiumQuickAction("\u25A0", "News", Color(0xFFFF9800), { nav?.navigate(Route.News.route) }, Modifier.weight(1f))
                                }
                                Row(Modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    PremiumQuickAction("\u2726", "For You", Color(0xFF7C4DFF), { nav?.navigate(Route.Recommendations.route) }, Modifier.weight(1f))
                                    PremiumQuickAction("\u25C6", "Stats", Color(0xFFFF4081), { nav?.navigate(Route.Stats.route) }, Modifier.weight(1f))
                                    PremiumQuickAction("\u2605", "Random", Color(0xFFFFC107), { nav?.navigate(Route.Random.route) }, Modifier.weight(1f))
                                }
                            }
                        }

                        // Continue Watching
                        if (d.continueWatching.isNotEmpty()) item {
                            Column {
                                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.PlayCircleFilled, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                        Text("Continue Watching", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                    }
                                    TextButton({ }) { Text("See All \u25B8", style = MaterialTheme.typography.labelLarge) }
                                }
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 20.dp)) {
                                    items(d.continueWatching) { entry ->
                                        ContinueWatchingCard(
                                            anime = Anime(id = entry.mediaId, title = com.rei.app.domain.model.AnimeTitle(english = entry.title), coverImage = com.rei.app.domain.model.CoverImage(large = entry.coverImage), bannerImage = entry.bannerImage, episodes = entry.totalEpisodes, mediaListEntry = com.rei.app.domain.model.MediaList(id = 0, status = try { com.rei.app.domain.model.MediaListStatus.valueOf(entry.status) } catch (_: Exception) { com.rei.app.domain.model.MediaListStatus.CURRENT }, progress = entry.progress)),
                                            onClick = { onAnimeClick(entry.mediaId) }
                                        )
                                    }
                                }
                            }
                        }

                        if (d.popular.isNotEmpty()) item { AnimeRow("Popular This Season", d.popular, onAnimeClick) }

                        // MAL Top Rated with rank badges
                        item {
                            SectionHeader("\u2605 MAL Top Rated")
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 20.dp)) {
                                items(d.allTime.take(10).withIndex()) { (index, anime) ->
                                    Box {
                                        AnimeCard(anime, { onAnimeClick(anime.id) }, Modifier.width(140.dp))
                                        if (config.showRankBadges) { RankBadge(index + 1, Modifier.padding(4.dp).align(Alignment.TopStart)) }
                                    }
                                }
                            }
                        }

                        if (d.upcoming.isNotEmpty()) item { AnimeRow("Upcoming Next Season", d.upcoming, onAnimeClick) }
                        if (d.allTime.isNotEmpty()) item { AnimeRow("All Time Popular", d.allTime, onAnimeClick) }

                        // Genre Quick Browse — premium with icon circles
                        item {
                            Column {
                                SectionHeader("Browse by Genre")
                                val genres = listOf("Action" to "\u2694" to Color(0xFFFF4444), "Romance" to "\u2665" to Color(0xFFFF69B4), "Comedy" to "\u263A" to Color(0xFFFFD54F), "Fantasy" to "\u2726" to Color(0xFF7C4DFF), "Sci-Fi" to "\u25C6" to Color(0xFF00BCD4), "Horror" to "\u25C7" to Color(0xFF880E4F), "Isekai" to "\u25CE" to Color(0xFF6B4DFF), "Slice of Life" to "\u2615" to Color(0xFF81C784), "Mecha" to "\u25A4" to Color(0xFF546E7A), "Drama" to "\u25B3" to Color(0xFFFF7043), "Sports" to "\u26BD" to Color(0xFF2E7D32), "Music" to "\u266A" to Color(0xFFE91E63))
                                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(horizontal = 20.dp)) {
                                    items(genres) { (pair, color) ->
                                        val (name, symbol) = pair
                                        Surface(onClick = { nav?.navigate(Route.SearchGenre.create(name)) }, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.12f), border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))) {
                                            Row(Modifier.padding(horizontal = 14.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                Box(Modifier.size(28.dp).clip(CircleShape).background(color.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) { Text(symbol, fontSize = 14.sp, color = color) }
                                                Text(name, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Coin tip card
                        item {
                            Card(Modifier.padding(horizontal = 20.dp).fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f)), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))) {
                                Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Box(Modifier.size(44.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape), contentAlignment = Alignment.Center) { Text("\u25C8", fontSize = 20.sp, color = MaterialTheme.colorScheme.primary) }
                                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Earn Rei Coins", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        Text("Track, rate, and complete anime to earn coins. Spend them in the shop!", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
                                    }
                                    FilledTonalButton({ nav?.navigate(Route.Economy.route) }, shape = RoundedCornerShape(12.dp), contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)) { Text("Shop \u25B8", style = MaterialTheme.typography.labelLarge) }
                                }
                            }
                        }
                        item { Spacer(Modifier.height(80.dp)) }
                    }
                }
            }
            is HomeState.Error -> HomeErrorContent((state as HomeState.Error).msg, { vm.refresh() }, pv)
        }
    }
}

@Composable
private fun PremiumQuickAction(symbol: String, label: String, color: Color, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.1f), border = BorderStroke(0.5.dp, color.copy(alpha = 0.25f))) {
        Column(Modifier.padding(vertical = 10.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(symbol, fontSize = 18.sp, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun HomeLoadingContent(pv: PaddingValues) = LazyColumn(Modifier.fillMaxSize().padding(pv)) {
    item { PremiumShimmer(Modifier.fillMaxWidth().height(400.dp), RoundedCornerShape(0.dp)) }
    item { Spacer(Modifier.height(16.dp)) }
    item { CardRowSkeleton(3) }
    item { Spacer(Modifier.height(16.dp)) }
    item { CardRowSkeleton(3) }
}

@Composable
private fun HomeErrorContent(msg: String, retry: () -> Unit, pv: PaddingValues) = Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) { NetworkErrorState(retry) }
