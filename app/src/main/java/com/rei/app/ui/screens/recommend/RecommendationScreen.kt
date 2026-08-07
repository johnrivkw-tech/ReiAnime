package com.rei.app.ui.screens.recommend

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
import com.rei.app.domain.model.Anime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecommendationScreen(onAnimeClick: (Int) -> Unit, onBack: () -> Unit, vm: RecommendationViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("For You", fontWeight = FontWeight.Bold) },
                subtitle = { Text("AI Recommendations", style = MaterialTheme.typography.labelSmall) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    if (state.isLoading) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 12.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.primary)
                    } else {
                        IconButton({ vm.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
                    }
                }
            )
        }
    ) { pv ->
        if (state.isLoading && state.recs.isEmpty()) {
            // Full-screen shimmer loading
            LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(5) {
                    Row(Modifier.padding(vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(80.dp).clip(RoundedCornerShape(12.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(Modifier.fillMaxWidth().height(18.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)))
                            Box(Modifier.width(180.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)))
                        }
                    }
                }
            }
        } else if (state.recs.isEmpty()) {
            // Empty state
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Text("\u2726", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("Track More Anime", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Rate at least 2 anime to unlock\npersonalized AI recommendations.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    FilledTonalButton({ vm.refresh() }, shape = RoundedCornerShape(12.dp)) {
                        Text("Try Again", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // AI banner
                item {
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = Color(0xFF1A0A2E).copy(alpha = 0.8f),
                        border = BorderStroke(0.5.dp, Color(0xFF7C4DFF).copy(alpha = 0.3f))
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(Color(0xFF7C4DFF).copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                                Text("\u2726", fontSize = 20.sp, color = Color(0xFF7C4DFF))
                            }
                            Column(Modifier.weight(1f)) {
                                Text("Powered by Rei AI", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Content-based similarity scoring\nNo API key required \u2022 100% on-device", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }

                // Stats
                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        StatChip("\u2605", "${state.recs.size} picks", Color(0xFF7C4DFF), Modifier.weight(1f))
                        StatChip("\u25B2", "${state.avgMatch}% avg match", Color(0xFF4CAF50), Modifier.weight(1f))
                        StatChip("\u25C8", "${state.genreCount} genres", Color(0xFFFFC107), Modifier.weight(1f))
                    }
                }

                // Recommendations
                items(state.recs, key = { it.anime.id }) { rec ->
                    RecCard(rec, onAnimeClick)
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }
}

@Composable
private fun StatChip(symbol: String, text: String, color: Color, modifier: Modifier = Modifier) {
    Surface(modifier = modifier, shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.08f), border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(symbol, fontSize = 14.sp, color = color)
            Text(text, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun RecCard(rec: RecUiItem, onClick: (Int) -> Unit) {
    val matchColor = when {
        rec.matchPercent >= 75 -> Color(0xFF4CAF50)
        rec.matchPercent >= 50 -> Color(0xFF8BC34A)
        rec.matchPercent >= 30 -> Color(0xFFFFC107)
        else -> Color(0xFFFF9800)
    }

    Surface(
        onClick = { onClick(rec.anime.id) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(Modifier.padding(10.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.Top) {
            // Cover
            Card(shape = RoundedCornerShape(12.dp), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp), modifier = Modifier.width(76.dp).height(106.dp)) {
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(rec.anime.coverImage.best).crossfade(true).build(),
                    contentDescription = rec.anime.title.primary,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Title + match badge
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(rec.anime.title.primary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                    // Match percent badge
                    Surface(shape = RoundedCornerShape(8.dp), color = matchColor.copy(alpha = 0.15f), border = BorderStroke(0.5.dp, matchColor.copy(alpha = 0.3f))) {
                        Text("${rec.matchPercent}%", style = MaterialTheme.typography.labelSmall, color = matchColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }
                }

                // Reasons
                if (rec.reasons.isNotEmpty()) {
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        items(rec.reasons) { reason ->
                            Surface(shape = RoundedCornerShape(6.dp), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)) {
                                Text(reason, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = MaterialTheme.colorScheme.onSecondaryContainer, fontSize = 9.sp)
                            }
                        }
                    }
                }

                // Score + eps
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    rec.anime.meanScore?.let { score ->
                        Surface(color = matchColor.copy(alpha = 0.12f), shape = RoundedCornerShape(6.dp)) {
                            Text("\u2605 ${score / 10.0}", style = MaterialTheme.typography.labelSmall, color = matchColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }
                    rec.anime.episodes?.let { Text("$it eps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }

                // Genre chips
                if (rec.anime.genres.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        rec.anime.genres.take(3).forEach { genre ->
                            Surface(color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp)) {
                                Text(genre, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ═══ UI Models ═══

data class RecUiItem(
    val anime: Anime,
    val matchPercent: Int,
    val reasons: List<String>
)

data class RecState(
    val isLoading: Boolean = false,
    val recs: List<RecUiItem> = emptyList(),
    val avgMatch: Int = 0,
    val genreCount: Int = 0
)
