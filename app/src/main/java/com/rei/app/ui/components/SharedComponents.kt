package com.rei.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.Anime
import com.rei.app.ui.theme.LocalBannerHeight
import com.rei.app.ui.theme.LocalBorderRadius
import com.rei.app.ui.theme.LocalCardElevation
import com.rei.app.ui.theme.LocalCardStyle
import com.rei.app.ui.theme.LocalReiConfig
import com.rei.app.ui.theme.LocalScoreStyle
import com.rei.app.ui.theme.ReiConfig
import com.rei.app.ui.theme.CardStyle
import com.rei.app.ui.theme.ScoreStyle
import com.rei.app.ui.theme.IndicatorStyle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ═══════════════════════════════════════════
// GENRE COLOR MAP — 30+ genres with unique colors
// ═══════════════════════════════════════════
val GenreColorMap = mapOf(
    "Action" to Color(0xFFFF4444), "Romance" to Color(0xFFFF69B4), "Comedy" to Color(0xFFFFD54F),
    "Fantasy" to Color(0xFF7C4DFF), "Sci-Fi" to Color(0xFF00BCD4), "Horror" to Color(0xFF880E4F),
    "Isekai" to Color(0xFF6B4DFF), "Mecha" to Color(0xFF546E7A), "Drama" to Color(0xFFFF7043),
    "Slice of Life" to Color(0xFF81C784), "Thriller" to Color(0xFFB71C1C), "Mystery" to Color(0xFF4A148C),
    "Psychological" to Color(0xFF1A237E), "Sports" to Color(0xFF2E7D32), "Music" to Color(0xFFE91E63),
    "Supernatural" to Color(0xFF311B92), "Adventure" to Color(0xFFF57C00), "Ecchi" to Color(0xFFF48FB1),
    "Harem" to Color(0xFFCE93D8), "School" to Color(0xFF42A5F5), "Military" to Color(0xFF455A64),
    "Space" to Color(0xFF0D47A1), "Magic" to Color(0xFFAA00FF), "Demons" to Color(0xFFB71C1C),
    "Historical" to Color(0xFF8D6E63), "Martial Arts" to Color(0xFFBF360C), "Vampire" to Color(0xFF4A001F),
    "Super Power" to Color(0xFFFF6D00), "Game" to Color(0xFF00E676), "Parody" to Color(0xFFA7FFEB),
    "Kids" to Color(0xFF64FFDA), "Shoujo" to Color(0xFFF8BBD0), "Shounen" to Color(0xFF2979FF),
    "Seinen" to Color(0xFF37474F), "Josei" to Color(0xFFAD1457)
)

fun genreColor(genre: String): Color = GenreColorMap[genre] ?: Color(0xFF78909C)

// ═══════════════════════════════════════════
// HERO CAROUSEL
// ═══════════════════════════════════════════
@OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)
@Composable
fun HeroCarousel(anime: List<Anime>, onAnimeClick: (Int) -> Unit, modifier: Modifier = Modifier) {
    if (anime.isEmpty()) return
    val bannerH = LocalBannerHeight.current
    val pagerState = rememberPagerState(pageCount = { minOf(anime.size, 5) })
    val config = LocalReiConfig.current
    LaunchedEffect(pagerState) { while (true) { delay(config.carouselAutoScrollMs.toLong()); pagerState.animateScrollToPage((pagerState.currentPage + 1) % pagerState.pageCount) } }
    Box(modifier = modifier.height(bannerH.dp)) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize(), beyondBoundsPageCount = 1) { page ->
            val item = anime[page]
            Box(modifier = Modifier.fillMaxSize().clickable { onAnimeClick(item.id) }) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(item.bannerImage ?: item.coverImage.best).crossfade(true).build(), contentDescription = item.title.primary, modifier = Modifier.fillMaxSize().graphicsLayer {
                    val off = (pagerState.currentPage - page).toFloat()
                    if (config.useParallax) { translationX = size.width * off * 0.25f; scaleX = 1f + kotlin.math.abs(off) * 0.08f; scaleY = scaleX }
                }, contentScale = ContentScale.Crop, alpha = 0.82f)
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color.Transparent, MaterialTheme.colorScheme.background.copy(alpha = 0.3f), MaterialTheme.colorScheme.background.copy(alpha = 0.9f), MaterialTheme.colorScheme.background))))
                Column(modifier = Modifier.align(Alignment.BottomStart).padding(horizontal = 24.dp).padding(bottom = 56.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (config.showFormatOnCards) item.format?.let { Surface(color = MaterialTheme.colorScheme.primary, shape = RoundedCornerShape(4.dp)) { Text(it.name.replace("_"," "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) } }
                    Text(item.title.primary, style = MaterialTheme.typography.headlineMedium.copy(shadow = Shadow(Color.Black.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, 2f), 4f)), fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onBackground)
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (config.showScoreOnCards) item.meanScore?.let { ScoreBadge(it) }
                        item.episodes?.let { Text("$it eps", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        item.seasonYear?.let { Text(it.toString(), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                    if (config.showGenreChips && item.genres.isNotEmpty()) { LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) { items(item.genres.take(4)) { g ->
                        val gc = if (config.useColorfulGenres) genreColor(g) else MaterialTheme.colorScheme.surfaceVariant
                        Surface(color = gc.copy(alpha = 0.25f), shape = RoundedCornerShape(6.dp), border = BorderStroke(0.5.dp, gc.copy(alpha = 0.5f))) { Text(g, style = MaterialTheme.typography.labelSmall, color = gc, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) }
                    } } }
                    if (config.showNextEpisode) item.nextAiringEpisode?.let { Text("Ep ${it.episode} in ${fmtTime(it.timeUntilAiring)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Medium) }
                }
            }
        }
        // Indicator styles
        when (config.indicatorStyle) {
            IndicatorStyle.DOT -> Row(modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 20.dp), horizontalArrangement = Arrangement.Center) { repeat(pagerState.pageCount) { i -> Box(modifier = Modifier.padding(horizontal = 3.dp).height(4.dp).width(if (pagerState.currentPage == i) 24.dp else 8.dp).clip(RoundedCornerShape(2.dp)).background(if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f))) } }
            IndicatorStyle.LINE -> Row(modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(4.dp)) { repeat(pagerState.pageCount) { i -> Box(modifier = Modifier.weight(1f).height(3.dp).clip(RoundedCornerShape(2.dp)).background(if (pagerState.currentPage == i) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))) } }
            IndicatorStyle.NUMBER -> Box(Modifier.align(Alignment.BottomEnd).padding(end = 20.dp, bottom = 20.dp)) { Surface(color = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f), shape = RoundedCornerShape(12.dp)) { Text("${pagerState.currentPage + 1}/${pagerState.pageCount}", style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp), fontWeight = FontWeight.Bold) } }
            IndicatorStyle.NONE -> {}
        }
    }
}

// ═══════════════════════════════════════════
// ANIME CARD with spring press animation
// ═══════════════════════════════════════════
@Composable
fun AnimeCard(anime: Anime, onClick: () -> Unit, modifier: Modifier = Modifier, showProgress: Boolean = false, progress: Int = 0, total: Int? = null, onLongClick: (() -> Unit)? = null) {
    val style = LocalCardStyle.current; val r = LocalBorderRadius.current; val el = LocalCardElevation.current; val config = LocalReiConfig.current; val shape = RoundedCornerShape(r.dp)
    // Spring press animation
    val pressed = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    val interactionModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            val down = awaitFirstDown(requireUnconsumed = false)
            scope.launch { pressed.animateTo(0.95f, spring(stiffness = Spring.StiffnessHigh)) }
            val up = waitForUpOrCancellation()
            scope.launch { pressed.animateTo(1f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)) }
            if (up != null) onClick()
        }
    }.graphicsLayer { scaleX = pressed.value; scaleY = pressed.value }

    when (style) {
        CardStyle.STANDARD -> StdCard(anime, onClick, modifier, shape, el, config, showProgress, progress, total, interactionModifier)
        CardStyle.COMPACT -> CompactCard(anime, onClick, modifier, shape, el, config, interactionModifier)
        CardStyle.MINIMAL -> MinimalCard(anime, onClick, modifier, config, interactionModifier)
        CardStyle.CINEMATIC -> CinematicCard(anime, onClick, modifier, shape, config, interactionModifier)
        CardStyle.GLASS -> GlassCard(anime, onClick, modifier, shape, config, interactionModifier)
        CardStyle.NEON -> NeonCard(anime, onClick, modifier, shape, config, interactionModifier)
    }
}

@Composable private fun StdCard(a: Anime, onClick: () -> Unit, m: Modifier, s: RoundedCornerShape, el: Int, c: ReiConfig, sp: Boolean, pr: Int, tot: Int?, im: Modifier) {
    Column(m.width(140.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.height(200.dp).then(im)) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.fillMaxSize().clip(s).shadow(el.dp, s), contentScale = ContentScale.Crop)
            if (c.showScoreOnCards) a.meanScore?.let { ScoreBadge(it, Modifier.align(Alignment.TopEnd).padding(6.dp)) }
            if (sp && tot != null && tot > 0) LinearProgressIndicator(progress = { pr.toFloat() / tot }, modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary, trackColor = Color.Black.copy(alpha = 0.5f))
        }
        Text(a.title.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
        if (c.showFormatOnCards) a.format?.let { Text(it.name.replace("_"," "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
    }
}

@Composable private fun CompactCard(a: Anime, onClick: () -> Unit, m: Modifier, s: RoundedCornerShape, el: Int, c: ReiConfig, im: Modifier) {
    Row(m.fillMaxWidth().then(im).padding(vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.width(64.dp).height(90.dp).clip(s).shadow(el.dp, s), contentScale = ContentScale.Crop)
        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(a.title.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                a.genres.firstOrNull()?.let { g ->
                    val gc = if (c.useColorfulGenres) genreColor(g) else MaterialTheme.colorScheme.onSurfaceVariant
                    Text(g, style = MaterialTheme.typography.labelSmall, color = gc)
                }
            }
            if (c.showScoreOnCards) a.meanScore?.let { ScoreBadge(it, compact = true) }
        }
    }
}

@Composable private fun MinimalCard(a: Anime, onClick: () -> Unit, m: Modifier, c: ReiConfig, im: Modifier) {
    Column(m.width(100.dp).then(im), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.fillMaxWidth().height(140.dp).clip(RoundedCornerShape(4.dp)), contentScale = ContentScale.Crop)
        Text(a.title.primary, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
    }
}

@Composable private fun CinematicCard(a: Anime, onClick: () -> Unit, m: Modifier, s: RoundedCornerShape, c: ReiConfig, im: Modifier) {
    Box(m.width(260.dp).height(160.dp).clip(s).then(im)) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.bannerImage ?: a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.7f)
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background))))
        Column(Modifier.align(Alignment.BottomStart).padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(a.title.primary, style = MaterialTheme.typography.titleSmall.copy(shadow = Shadow(Color.Black, androidx.compose.ui.geometry.Offset(0f,1f),3f)), fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) { if (c.showScoreOnCards) a.meanScore?.let { Text("★ ${it/10.0}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }; a.genres.firstOrNull()?.let { g -> val gc = if (c.useColorfulGenres) genreColor(g) else Color.White.copy(alpha = 0.7f); Text(g, style = MaterialTheme.typography.labelSmall, color = gc) } }
        }
    }
}

@Composable private fun GlassCard(a: Anime, onClick: () -> Unit, m: Modifier, s: RoundedCornerShape, c: ReiConfig, im: Modifier) {
    Box(m.width(150.dp).height(220.dp).clip(s).then(im)) {
        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.5f)
        Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)))
        Column(Modifier.align(Alignment.BottomStart).padding(10.dp), verticalArrangement = Arrangement.spacedBy(3.dp)) {
            if (c.showScoreOnCards) a.meanScore?.let { ScoreBadge(it, compact = true) }
            Text(a.title.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis, color = Color.White)
        }
    }
}

@Composable private fun NeonCard(a: Anime, onClick: () -> Unit, m: Modifier, s: RoundedCornerShape, c: ReiConfig, im: Modifier) {
    val accent = MaterialTheme.colorScheme.primary
    Column(m.width(140.dp).then(im), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(Modifier.height(195.dp).clip(s).border(1.5.dp, accent, s)) {
            AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(a.coverImage.best).crossfade(true).build(), a.title.primary, modifier = Modifier.fillMaxSize().clip(s), contentScale = ContentScale.Crop, alpha = 0.85f)
            if (c.showScoreOnCards) a.meanScore?.let { ScoreBadge(it, Modifier.align(Alignment.TopEnd).padding(6.dp)) }
        }
        Text(a.title.primary, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = accent)
    }
}

// ═══════════════════════════════════════════
// SCORE BADGE with glow effect
// ═══════════════════════════════════════════
@Composable fun ScoreBadge(score: Int, modifier: Modifier = Modifier, compact: Boolean = false) {
    val sc = when { score >= 80 -> Color(0xFF4CAF50); score >= 60 -> Color(0xFF8BC34A); score >= 40 -> Color(0xFFFFC107); else -> Color(0xFFFF5722) }
    val style = LocalScoreStyle.current
    val config = LocalReiConfig.current
    Surface(color = if (compact) Color.Transparent else sc.copy(alpha = 0.15f), shape = RoundedCornerShape(if (compact) 4.dp else 8.dp), modifier = modifier.let { if (config.useGlowOnScore && !compact) it.shadow(4.dp, RoundedCornerShape(8.dp), ambientColor = sc.copy(alpha = 0.3f), spotColor = sc.copy(alpha = 0.2f)) else it }) {
        Row(modifier = Modifier.padding(horizontal = if (compact) 0.dp else 6.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(2.dp)) {
            val label = when (style) { ScoreStyle.STAR -> "★"; ScoreStyle.SMILEY -> if (score >= 70) ":)" else if (score >= 40) ":|" else ":("; else -> "" }
            val value = when (style) { ScoreStyle.PERCENTAGE -> "$score%"; ScoreStyle.POINTS_10 -> "${score/10}"; ScoreStyle.POINTS_100 -> "$score"; else -> "${score/10.0}" }
            Text(label, color = sc, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
            Text(value, color = sc, style = if (compact) MaterialTheme.typography.labelSmall else MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable fun SectionHeader(title: String, onSeeAll: (() -> Unit)? = null, m: Modifier = Modifier) {
    Row(m.fillMaxWidth().padding(horizontal = 20.dp, vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onBackground)
        onSeeAll?.let { TextButton(onClick = it) { Text("See All", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.primary) } }
    }
}

@Composable fun AnimeRow(title: String, anime: List<Anime>, onClick: (Int) -> Unit, onSeeAll: (() -> Unit)? = null, m: Modifier = Modifier) {
    Column(m) { SectionHeader(title, onSeeAll); LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(horizontal = 20.dp)) { items(anime) { AnimeCard(it, { onClick(it.id) }) } } }
}

// ═══════════════════════════════════════════
// SHIMMER with spring
// ═══════════════════════════════════════════
@Composable fun ShimmerBox(m: Modifier = Modifier) {
    val a by rememberInfiniteTransition(label = "s").animateFloat(0.2f, 0.5f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "s")
    Surface(m, color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = a), shape = RoundedCornerShape(8.dp)) {}
}

@Composable fun GenreChip(genre: String, sel: Boolean, onClick: () -> Unit, m: Modifier = Modifier) {
    val config = LocalReiConfig.current
    val gc = if (config.useColorfulGenres) genreColor(genre) else MaterialTheme.colorScheme.primary
    FilterChip(selected = sel, onClick = onClick, label = { Text(genre, style = MaterialTheme.typography.labelMedium) }, modifier = m, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = gc.copy(alpha = 0.2f), selectedLabelColor = gc))
}

// ═══════════════════════════════════════════
// CONTINUE WATCHING CARD
// ═══════════════════════════════════════════
@Composable
fun ContinueWatchingCard(anime: Anime, onClick: () -> Unit, modifier: Modifier = Modifier) {
    val config = LocalReiConfig.current
    val entry = anime.mediaListEntry
    val progress = entry?.progress ?: 0
    val total = anime.episodes ?: 0
    val progressFraction = if (total > 0) progress.toFloat() / total else 0f
    val r = LocalBorderRadius.current

    Surface(onClick = onClick, modifier = modifier.width(200.dp), shape = RoundedCornerShape(r.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.height(100.dp).fillMaxWidth()) {
                AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(anime.bannerImage ?: anime.coverImage.best).crossfade(true).build(), anime.title.primary, modifier = Modifier.fillMaxSize().clip(RoundedCornerShape(topStart = r.dp, topEnd = r.dp)), contentScale = ContentScale.Crop, alpha = 0.7f)
                Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)))))
                Column(Modifier.align(Alignment.BottomStart).padding(8.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(anime.title.primary, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurface)
                    if (total > 0) Text("Ep $progress of $total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Play icon overlay
                if (total > progress) {
                    Box(Modifier.align(Alignment.Center).size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.8f), RoundedCornerShape(18.dp)), contentAlignment = Alignment.Center) {
                        Text("▶", color = MaterialTheme.colorScheme.onPrimary, fontSize = 14.sp)
                    }
                }
            }
            if (total > 0) {
                LinearProgressIndicator(progress = { progressFraction }, modifier = Modifier.fillMaxWidth().height(3.dp).padding(horizontal = 8.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
            }
            Spacer(Modifier.height(4.dp))
        }
    }
}

private fun fmtTime(s: Int): String { val d = s/86400; val h = (s%86400)/3600; return if (d > 0) "${d}d ${h}h" else "${h}h" }
