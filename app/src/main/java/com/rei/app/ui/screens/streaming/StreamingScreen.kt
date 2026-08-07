package com.rei.app.ui.screens.streaming

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.data.remote.livechart.LiveChartAnime
import com.rei.app.data.remote.livechart.StreamingOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StreamingScreen(onAnimeClick: (Int) -> Unit, vm: StreamingViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var selectedPlatform by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Where to Watch", fontWeight = FontWeight.Bold) },
                subtitle = { Text("Powered by LiveChart.me", style = MaterialTheme.typography.labelSmall) }
            )
        }
    ) { pv ->
        when {
            state.loading -> {
                Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Loading streaming data...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
            state.anime.isNotEmpty() -> {
                LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Filter chips
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(state.filter == null, { vm.setFilter(null) }, label = { Text("All") })
                            FilterChip(state.filter == "streaming", { vm.setFilter("streaming") }, label = { Text("Available Now") })
                            FilterChip(state.filter == "airing", { vm.setFilter("airing") }, label = { Text("Currently Airing") })
                        }
                    }

                    // Platform quick filters
                    item {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Crunchyroll" to Color(0xFFF47521), "Netflix" to Color(0xFFE50914), "Hulu" to Color(0xFF1CE783), "HIDIVE" to Color(0xFF0066FF), "Disney+" to Color(0xFF113CCF), "Amazon" to Color(0xFF00A8E1)).forEach { (name, color) ->
                                Surface(
                                    onClick = { selectedPlatform = if (selectedPlatform == name) null else name },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (selectedPlatform == name) color.copy(alpha = 0.2f) else color.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(0.5.dp, color.copy(alpha = if (selectedPlatform == name) 0.4f else 0.15f))
                                ) {
                                    Text(name, style = MaterialTheme.typography.labelSmall, fontWeight = if (selectedPlatform == name) FontWeight.Bold else FontWeight.Medium, color = color, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp))
                                }
                            }
                        }
                    }

                    val filtered = if (state.filter == "streaming") state.anime.filter { it.hasStreaming } else if (state.filter == "airing") state.anime.filter { it.hasNextEpisode } else state.anime
                    items(filtered, key = { it.id }) { anime ->
                        StreamingAnimeCard(anime, onAnimeClick)
                    }
                }
            }
            state.error != null -> {
                Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, Modifier.size(64.dp), MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                        Text("Failed to load", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.error)
                        Text(state.error!!, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton({ }) { Text("Retry") }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingAnimeCard(anime: LiveChartAnime, onClick: (Int) -> Unit) {
    val context = LocalContext.current
    Surface(
        onClick = { if (anime.anilistId != null) onClick(anime.anilistId) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                anime.imageUrl?.let { url ->
                    Card(shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.width(64.dp).height(90.dp)) {
                        AsyncImage(model = ImageRequest.Builder(context).data(url).crossfade(true).build(), contentDescription = anime.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                }
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(anime.bestTitle, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        anime.format?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        anime.episodes?.let { Text("$it eps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        anime.score?.let { Text("\u2605 ${it / 10.0}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold) }
                    }
                    if (anime.studios.isNotEmpty()) Text(anime.studios.joinToString(", "), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }

            // Streaming badges
            if (anime.hasStreaming) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    anime.streaming.forEach { option -> StreamingBadge(option) }
                }
            } else {
                Text("No streaming available", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
            }

            // Next episode countdown
            anime.nextEpisodeAiringAt?.let { airingAt ->
                val now = System.currentTimeMillis() / 1000
                val diff = airingAt - now
                if (diff > 0) {
                    val days = diff / 86400
                    val hours = (diff % 86400) / 3600
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Box(Modifier.size(6.dp).clip(RoundedCornerShape(3.dp)).background(Color(0xFF4CAF50)))
                        Text("Ep ${anime.nextEpisode} in ${days}d ${hours}h", style = MaterialTheme.typography.labelSmall, color = Color(0xFF4CAF50), fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingBadge(option: StreamingOption) {
    val (bgColor, textColor) = when {
        option.isCrunchyroll -> Color(0xFFF47521).copy(alpha = 0.15f) to Color(0xFFF47521)
        option.isHidive -> Color(0xFF0066FF).copy(alpha = 0.15f) to Color(0xFF0066FF)
        option.isNetflix -> Color(0xFFE50914).copy(alpha = 0.15f) to Color(0xFFE50914)
        option.isAmazon -> Color(0xFF00A8E1).copy(alpha = 0.15f) to Color(0xFF00A8E1)
        option.isDisney -> Color(0xFF113CCF).copy(alpha = 0.15f) to Color(0xFF113CCF)
        else -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(onClick = { }, shape = RoundedCornerShape(8.dp), color = bgColor, border = androidx.compose.foundation.BorderStroke(0.5.dp, textColor.copy(alpha = 0.3f))) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(option.icon, style = MaterialTheme.typography.labelSmall)
            Text(option.service, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = textColor)
        }
    }
}
