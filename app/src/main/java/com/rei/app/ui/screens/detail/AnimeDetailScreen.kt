package com.rei.app.ui.screens.detail

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.*
import com.rei.app.ui.components.ScoreBadge
import com.rei.app.ui.components.ScoreRing
import com.rei.app.ui.components.ShimmerBox
import com.rei.app.ui.components.genreColor
import com.rei.app.ui.theme.LocalReiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnimeDetailScreen(
    animeId: Int,
    onBack: () -> Unit,
    onEpisodeGrid: (String, Int) -> Unit = { _, _ -> },
    onFranchise: (String) -> Unit = {},
    vm: AnimeDetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsState()
    var showTrack by remember { mutableStateOf(false) }
    var synopsisExpanded by remember { mutableStateOf(false) }
    val config = LocalReiConfig.current
    LaunchedEffect(animeId) { vm.load(animeId) }

    Scaffold(
        bottomBar = {
            (state as? DetailState.Success)?.anime?.let { anime ->
                DetailBottomBar(anime, onTrack = { showTrack = true }, onFavorite = { vm.toggleFavorite() }, onShare = { vm.shareAnime(LocalContext.current) })
            }
        }
    ) { pv ->
        when (state) {
            is DetailState.Loading -> DetailLoadingContent(pv)
            is DetailState.Success -> {
                val anime = (state as DetailState.Success).anime
                LazyColumn(
                    Modifier.fillMaxSize().padding(pv),
                    verticalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    // ── Hero Banner (full-bleed) ──
                    item {
                        Box(Modifier.fillMaxWidth().height(340.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(anime.bannerImage ?: anime.coverImage.best)
                                    .crossfade(true).build(),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                alpha = 0.65f
                            )
                            // 5-stop gradient
                            Box(Modifier.fillMaxSize().background(
                                Brush.verticalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.Transparent,
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.4f),
                                        MaterialTheme.colorScheme.background.copy(alpha = 0.85f),
                                        MaterialTheme.colorScheme.background
                                    )
                                )
                            ))
                            // Back button
                            IconButton(
                                onBack,
                                Modifier.align(Alignment.TopStart).padding(12.dp)
                                    .background(Color.Black.copy(alpha = 0.3f), CircleShape)
                            ) { Icon(Icons.Filled.ArrowBack, null, tint = Color.White) }
                            // Favorite + Share
                            Row(Modifier.align(Alignment.TopEnd).padding(12.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val isFav = (state as? DetailState.Success)?.anime?.isFavourite == true
                                IconButton(
                                    { vm.toggleFavorite() },
                                    Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                ) { Icon(if (isFav) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder, null, tint = if (isFav) Color(0xFFFF4081) else Color.White) }
                                IconButton(
                                    { vm.shareAnime(LocalContext.current) },
                                    Modifier.background(Color.Black.copy(alpha = 0.3f), CircleShape)
                                ) { Icon(Icons.Filled.Share, null, tint = Color.White) }
                            }
                        }
                    }

                    // ── Cover + Title Section ──
                    item {
                        Row(
                            Modifier.padding(horizontal = 24.dp).offset(y = (-70).dp),
                            horizontalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Card(
                                modifier = Modifier.width(110.dp).height(155.dp),
                                shape = RoundedCornerShape(14.dp),
                                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(anime.coverImage.best).crossfade(true).build(),
                                    anime.title.primary,
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop
                                )
                            }
                            Column(
                                Modifier.weight(1f).padding(top = 70.dp),
                                verticalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Text(
                                    anime.title.primary,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                                anime.title.english?.let { eng ->
                                    if (eng != anime.title.primary) {
                                        Text(
                                            eng,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                anime.meanScore?.let { ScoreBadge(it) }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    anime.format?.let {
                                        InfoChip(it.name.replace("_", " "), MaterialTheme.colorScheme.primary)
                                    }
                                    anime.episodes?.let {
                                        Text("\u2022 $it eps", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    anime.seasonYear?.let {
                                        Text("\u2022 $it", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                    anime.duration?.let {
                                        Text("\u2022 ${it}m", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }

                    // ── Status + Next Episode ──
                    item {
                        Column(Modifier.padding(horizontal = 24.dp, vertical = 10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            anime.status?.let { status ->
                                val (dot, label, color) = when (status) {
                                    MediaStatus.RELEASING -> Triple("\u25A2", "Airing", Color(0xFF4CAF50))
                                    MediaStatus.FINISHED -> Triple("\u2713", "Finished", Color(0xFF4CAF50))
                                    MediaStatus.NOT_YET_RELEASED -> Triple("\u25D0", "Not Yet Aired", Color(0xFFFFC107))
                                    MediaStatus.CANCELLED -> Triple("\u2715", "Cancelled", Color(0xFFFF5722))
                                    MediaStatus.HIATUS -> Triple("\u25B8", "Hiatus", Color(0xFFFF9800))
                                }
                                Surface(
                                    color = color.copy(alpha = 0.1f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(0.5.dp, color.copy(alpha = 0.3f))
                                ) {
                                    Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(dot, fontSize = 12.sp)
                                        Text(label, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
                                    }
                                }
                            }
                            if (config.showNextEpisode) anime.nextAiringEpisode?.let { next ->
                                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Icon(Icons.Filled.Schedule, null, Modifier.size(14.dp), MaterialTheme.colorScheme.primary)
                                    Text("Ep ${next.episode} in ${fmtAir(next.timeUntilAiring)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }

                    // ── Genre Chips ──
                    if (anime.genres.isNotEmpty()) item {
                        LazyRow(
                            Modifier.padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(7.dp),
                            contentPadding = PaddingValues(horizontal = 24.dp)
                        ) {
                            items(anime.genres) { genre ->
                                val gc = if (config.useColorfulGenres) genreColor(genre) else MaterialTheme.colorScheme.secondaryContainer
                                Surface(
                                    color = gc.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(20.dp),
                                    border = BorderStroke(0.5.dp, gc.copy(alpha = 0.35f))
                                ) {
                                    Text(
                                        genre,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = gc,
                                        fontWeight = FontWeight.Medium,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }
                    }

                    // ── Studios ──
                    if (anime.studios.any { it.isAnimationStudio }) item {
                        Row(
                            Modifier.padding(horizontal = 24.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(Icons.Outlined.MovieCreation, null, Modifier.size(15.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f))
                            Text(
                                anime.studios.filter { it.isAnimationStudio }.joinToString(", ") { it.name },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // ── Separator ──
                    item { HorizontalDivider(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)) }

                    // ── Detail Tabs (Overview / Episodes / Related) ──
                    item {
                        val tabs = listOf("Overview", "Episodes", "Related")
                        val pagerState = rememberPagerState(pageCount = { tabs.size })
                        val scope = rememberCoroutineScope()

                        Column(Modifier.padding(horizontal = 24.dp)) {
                            // Tab row
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                tabs.forEachIndexed { index, tab ->
                                    val selected = pagerState.currentPage == index
                                    Surface(
                                        onClick = { scope.launch { pagerState.animateScrollToPage(index) } },
                                        shape = RoundedCornerShape(12.dp),
                                        color = if (selected) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f) else Color.Transparent,
                                        border = if (selected) BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)) else null,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            tab,
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.padding(vertical = 10.dp).align(Alignment.CenterHorizontally),
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }

                        HorizontalPager(
                            state = pagerState,
                            modifier = Modifier.fillMaxWidth().height(600.dp),
                            beyondViewportPageCount = 0
                        ) { page ->
                            when (page) {
                                0 -> OverviewTab(anime, config, synopsisExpanded, { synopsisExpanded = it }, onEpisodeGrid, onFranchise)
                                1 -> EpisodesTab(anime, onEpisodeGrid, onFranchise)
                                2 -> RelatedTab(anime, config)
                            }
                        }
                    }

                    item { Spacer(Modifier.height(90.dp)) }
                }
            }
            is DetailState.Error -> {
                Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, Modifier.size(56.dp), MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        Text("Something went wrong", style = MaterialTheme.typography.titleMedium)
                        FilledTonalButton({ vm.load(animeId) }) { Text("Try Again") }
                    }
                }
            }
        }
    }

    // Track Sheet
    if (showTrack) {
        (state as? DetailState.Success)?.anime?.let { anime ->
            TrackBottomSheet(anime, vm, onDismiss = { showTrack = false })
        }
    }
}

// ═══════════════════════════════════════════
// OVERVIEW TAB
// ═══════════════════════════════════════════
@Composable
private fun OverviewTab(
    anime: Anime,
    config: com.rei.app.ui.theme.ReiConfig,
    synopsisExpanded: Boolean,
    onSynopsisToggle: (Boolean) -> Unit,
    onEpisodeGrid: (String, Int) -> Unit,
    onFranchise: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Synopsis
        anime.description?.let { desc -> item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Synopsis", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                val clean = desc.replace("<br>", "\n").replace("<i>", "").replace("</i>", "").replace("<b>", "").replace("</b>", "").replace("~!", "[SPOILER]").replace("!~", "[/SPOILER]")
                Text(
                    clean,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = if (synopsisExpanded) Int.MAX_VALUE else 4,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 20.sp
                )
                TextButton({ onSynopsisToggle(!synopsisExpanded) }) {
                    Text(
                        if (synopsisExpanded) "Show Less" else "Read More",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }}

        // Stats Card
        if (config.showStatsOnDetail) item {
            Card(
                Modifier.padding(horizontal = 24.dp, vertical = 6.dp).fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f))
            ) {
                Row(Modifier.fillMaxWidth().padding(18.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                    StatCell("Score", anime.meanScore?.let { "${it / 10.0}" } ?: "\u2014")
                    StatCell("Rank", anime.popularity?.let { "#$it" } ?: "\u2014")
                    StatCell("Favs", anime.favourites?.let { if (it >= 1000) "${it / 1000}K" else "$it" } ?: "\u2014")
                    StatCell("Trend", anime.trending?.let { "\u2191$it" } ?: "\u2014")
                }
            }
        }

        // Score Ring
        item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                anime.meanScore?.let { score ->
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        ScoreRing(score, size = 52, strokeWidth = 5f)
                        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text("Mean Score", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("${score / 10.0}/10", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Trailer
        if (config.showTrailerButton && anime.trailer?.thumbnail != null) item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Trailer", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    onClick = { /* Open YouTube */ },
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFFFF0000).copy(alpha = 0.08f),
                    border = BorderStroke(1.dp, Color(0xFFFF0000).copy(alpha = 0.2f))
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.PlayCircleFilled, null, tint = Color(0xFFFF0000), modifier = Modifier.size(28.dp))
                        anime.trailer?.site?.let {
                            Text("Watch on $it", style = MaterialTheme.typography.bodyMedium, color = Color(0xFFFF0000), fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// EPISODES TAB
// ═══════════════════════════════════════════
@Composable
private fun EpisodesTab(
    anime: Anime,
    onEpisodeGrid: (String, Int) -> Unit,
    onFranchise: (String) -> Unit
) {
    LazyColumn(Modifier.fillMaxSize().padding(horizontal = 24.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        item {
            Text("Episode Tracking", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        // Quick action buttons
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (anime.episodes != null && anime.episodes!! > 0) {
                    FilledTonalButton(
                        onClick = { onEpisodeGrid(anime.title.primary, anime.episodes!!) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Filled.GridView, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Grid View", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                    }
                }
                OutlinedButton(
                    onClick = { onFranchise(anime.title.primary) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.AccountTree, null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Watch Order", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
        // Episode preview list (first 8 episodes)
        if (anime.episodes != null && anime.episodes!! > 0) {
            val total = minOf(anime.episodes!!, 8)
            items(total) { epNum ->
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Episode number circle
                        Box(
                            Modifier.size(36.dp).background(
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                                CircleShape
                            ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "$epNum",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(Modifier.weight(1f)) {
                            Text("Episode $epNum", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                            Text("24 min", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Icon(
                            Icons.Outlined.PlayCircleOutline,
                            null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            if (anime.episodes!! > 8) item {
                TextButton({ onEpisodeGrid(anime.title.primary, anime.episodes!!) }) {
                    Text("View all ${anime.episodes} episodes \u25B8", color = MaterialTheme.colorScheme.primary)
                }
            }
        } else {
            item {
                Column(Modifier.padding(vertical = 32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Outlined.Movie, null, Modifier.size(48.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                    Spacer(Modifier.height(12.dp))
                    Text("Episode info not available", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// RELATED TAB
// ═══════════════════════════════════════════
@Composable
private fun RelatedTab(
    anime: Anime,
    config: com.rei.app.ui.theme.ReiConfig
) {
    LazyColumn(Modifier.fillMaxSize(), verticalArrangement = Arrangement.spacedBy(0.dp)) {
        // Recommendations
        if (config.showRecommendations && anime.recommendations.isNotEmpty()) {
            item {
                Row(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Text("Recommendations", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    TextButton({}) { Text("See All", style = MaterialTheme.typography.labelLarge) }
                }
            }
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp), contentPadding = PaddingValues(horizontal = 24.dp)) {
                    items(anime.recommendations) { rec ->
                        Column(Modifier.width(96.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current).data(rec.coverImage.best).crossfade(true).build(),
                                rec.title.primary,
                                modifier = Modifier.fillMaxWidth().height(134.dp).clip(RoundedCornerShape(10.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Text(rec.title.primary, style = MaterialTheme.typography.labelSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        }
                    }
                }
            }
        }

        // Franchise
        if (config.showFranchise) item {
            Column(Modifier.padding(horizontal = 24.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Franchise", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(Icons.Filled.AccountTree, null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                        Text("View Watch Order", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }

        // Related empty state
        if (anime.recommendations.isEmpty()) item {
            Column(Modifier.fillMaxWidth().padding(vertical = 40.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Outlined.Link, null, Modifier.size(48.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                Spacer(Modifier.height(12.dp))
                Text("No related anime found", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// ═══════════════════════════════════════════
// SHARED COMPONENTS
// ═══════════════════════════════════════════
@Composable
private fun DetailLoadingContent(pv: PaddingValues) = LazyColumn(Modifier.fillMaxSize().padding(pv)) {
    item { ShimmerBox(Modifier.fillMaxWidth().height(340.dp)) }
    item { ShimmerBox(Modifier.fillMaxWidth().height(120.dp).padding(horizontal = 24.dp, vertical = 8.dp)) }
    items(4) { ShimmerBox(Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 24.dp, vertical = 6.dp)) }
}

@Composable
private fun InfoChip(label: String, color: Color) {
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, color.copy(alpha = 0.25f))) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
    }
}

@Composable
private fun StatCell(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
    }
}

@Composable
private fun TrackBottomSheet(anime: Anime, vm: AnimeDetailViewModel, onDismiss: () -> Unit) {
    var selectedStatus by remember { mutableStateOf(MediaListStatus.CURRENT) }
    var score by remember { mutableFloatStateOf(0f) }
    var progress by remember { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add to List", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Status", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                MediaListStatus.entries.forEach { s ->
                    val sel = s == selectedStatus
                    Surface(
                        onClick = { selectedStatus = s },
                        color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Row(Modifier.fillMaxWidth().padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(s.icon, fontSize = 15.sp)
                            Text(s.displayName, fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal)
                        }
                    }
                }
                HorizontalDivider()
                Text("Score", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Slider(value = score, onValueChange = { score = it }, valueRange = 0f..100f, steps = 19, modifier = Modifier.weight(1f))
                    Text("${(score / 10).toInt()}", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
                anime.episodes?.let { total ->
                    Text("Progress", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Slider(value = progress.toFloat(), onValueChange = { progress = it.toInt() }, valueRange = 0f..total.toFloat(), steps = total - 1, modifier = Modifier.weight(1f))
                        Text("$progress/$total", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                    }
                }
            }
        },
        confirmButton = { FilledTonalButton(onClick = { vm.updateStatus(anime.id, selectedStatus); onDismiss() }) { Text("Save") } },
        dismissButton = { TextButton(onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun DetailBottomBar(anime: Anime, onTrack: () -> Unit, onFavorite: () -> Unit, onShare: () -> Unit) = Surface(
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 3.dp,
    shadowElevation = 12.dp
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 10.dp), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
        FilledIconButton(
            onTrack,
            Modifier.weight(1f).height(48.dp),
            shape = RoundedCornerShape(14.dp),
            colors = FilledIconButtonDefaults.filledIconButtonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Icon(Icons.Filled.Add, null)
            Spacer(Modifier.width(4.dp))
            Text("Add to List", fontWeight = FontWeight.Bold)
        }
        IconButton(onFavorite) {
            Icon(
                if (anime.isFavourite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                "Favorite",
                tint = if (anime.isFavourite) Color(0xFFFF4081) else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onShare) { Icon(Icons.Outlined.Share, "Share") }
    }
}

private fun fmtAir(s: Int) = "${s / 86400}d ${(s % 86400) / 3600}h"
