package com.rei.app.ui.screens.tracking

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaListStatus
import com.rei.app.ui.components.PremiumEmptyState
import com.rei.app.ui.components.PremiumProgressIndicator
import com.rei.app.ui.components.PremiumShimmer
import com.rei.app.ui.theme.LocalReiConfig
import com.rei.app.ui.theme.SwipeAction

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrackingScreen(onAnimeClick: (Int) -> Unit, vm: TrackingViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val config = LocalReiConfig.current

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = { Text("My List", fontWeight = FontWeight.Bold) },
                actions = {
                    IconButton({ }) { Icon(Icons.Filled.Sort, "Sort", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton({ }) { Icon(Icons.Filled.FilterList, "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                    IconButton({ }) { Icon(Icons.Filled.Search, "Search", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                },
                scrollBehavior = scroll
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Premium status tabs with colored indicators
            ScrollableTabRow(
                selectedTabIndex = state.status.ordinal,
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                divider = {}
            ) {
                MediaListStatus.entries.forEach { s ->
                    val selected = s == state.status
                    val statusColor = statusColor(s)
                    Tab(
                        selected = selected,
                        onClick = { vm.setStatus(s) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(if (selected) statusColor else statusColor.copy(alpha = 0.3f)))
                                Text(
                                    s.displayName,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (selected) statusColor else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        },
                        selectedContentColor = statusColor,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.15f))

            // Stats summary bar — premium
            if (state.anime.isNotEmpty()) {
                Surface(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(14.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ) {
                    Row(
                        Modifier.padding(horizontal = 14.dp, vertical = 10.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalEps = state.anime.sumOf { it.mediaListEntry?.progress ?: 0 }
                        val avgScore = state.anime.mapNotNull { it.meanScore }.average()
                        MiniStat("${state.anime.size}", "anime", MaterialTheme.colorScheme.primary)
                        if (totalEps > 0) MiniStat("$totalEps", "eps", Color(0xFF4CAF50))
                        if (avgScore > 0) MiniStat("${(avgScore / 10).toInt()}", "avg", Color(0xFFFFC107))
                    }
                }
            }

            // Anime list
            if (state.anime.isEmpty()) {
                TrackingEmptyState(onDiscover = {})
            } else {
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(state.anime, key = { it.id }) { anime ->
                        PremiumTrackingItem(
                            anime = anime,
                            onClick = { onAnimeClick(anime.id) },
                            onIncrement = { },
                            swipeLeft = config.swipeLeftAction,
                            swipeRight = config.swipeRightAction
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PremiumTrackingItem(
    anime: Anime,
    onClick: () -> Unit,
    onIncrement: () -> Unit,
    swipeLeft: SwipeAction,
    swipeRight: SwipeAction
) {
    val statusColor = statusColor(try { anime.mediaListEntry?.status ?: MediaListStatus.CURRENT } catch (_: Exception) { MediaListStatus.CURRENT })
    val progress = anime.mediaListEntry?.progress ?: 0
    val total = anime.episodes ?: 0
    val score = anime.mediaListEntry?.score ?: 0f

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            Modifier.padding(10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Cover image with status dot overlay
            Box {
                Card(shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.width(56.dp).height(80.dp)) {
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(anime.coverImage.best).crossfade(true).build(), contentDescription = anime.title.primary, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                }
                Box(Modifier.size(10.dp).clip(CircleShape).background(statusColor).border(1.5.dp, MaterialTheme.colorScheme.surface, CircleShape).align(Alignment.BottomEnd).offset(x = 2.dp, y = 2.dp))
            }

            // Info column
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(anime.title.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(anime.mediaListEntry?.status?.displayName ?: "Watching", style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium)
                    if (score > 0) { Text("\u2022", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)); Text("\u2605 ${(score / 10).toInt()}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
                }
                if (total > 0) {
                    PremiumProgressIndicator(current = progress, total = total, modifier = Modifier.padding(top = 2.dp))
                } else if (progress > 0) {
                    Text("$progress episodes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            // Quick increment button
            if (total == 0 || progress < total) {
                FilledIconButton(
                    onClick = onIncrement,
                    modifier = Modifier.size(36.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = FilledIconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                ) { Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp)) }
            }
        }
    }
}

@Composable
private fun MiniStat(value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 10.sp)
    }
}

private fun statusColor(status: MediaListStatus): Color = when (status) {
    MediaListStatus.CURRENT -> Color(0xFFFF4081)
    MediaListStatus.PLANNING -> Color(0xFFFFC107)
    MediaListStatus.COMPLETED -> Color(0xFF4CAF50)
    MediaListStatus.DROPPED -> Color(0xFFFF5722)
    MediaListStatus.PAUSED -> Color(0xFF2196F3)
    MediaListStatus.REPEATING -> Color(0xFF7C4DFF)
}
