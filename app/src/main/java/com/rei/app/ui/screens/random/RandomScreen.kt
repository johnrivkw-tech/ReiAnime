package com.rei.app.ui.screens.random

import androidx.compose.animation.*
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
import com.rei.app.ui.components.ScoreBadge
import com.rei.app.ui.components.genreColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RandomAnimeScreen(onAnimeClick: (Int) -> Unit, vm: RandomViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Random Anime", fontWeight = FontWeight.Bold) },
                actions = {
                    FilledTonalButton(
                        onClick = { vm.roll() },
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Filled.Casino, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("Roll", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
                    }
                    Spacer(Modifier.width(8.dp))
                }
            )
        }
    ) { pv ->
        Box(Modifier.fillMaxSize().padding(pv)) {
            when {
                state.loading -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                        Text("Finding something great...", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                state.anime != null -> RandomAnimeCard(state.anime!!, onAnimeClick, Modifier.align(Alignment.Center).padding(16.dp))
                else -> {
                    Column(Modifier.align(Alignment.Center), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Box(Modifier.size(100.dp).clip(RoundedCornerShape(50.dp)).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.Casino, null, Modifier.size(48.dp), MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        }
                        Text("Roll the Dice!", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Discover a random anime from MAL", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton({ vm.roll() }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Casino, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Discover")
                        }
                    }
                }
            }
        }
    }
    LaunchedEffect(Unit) { if (state.anime == null && !state.loading) vm.roll() }
}

@Composable
private fun RandomAnimeCard(anime: Anime, onClick: (Int) -> Unit, m: Modifier = Modifier) {
    val config = com.rei.app.ui.theme.LocalReiConfig.current
    Surface(m.fillMaxWidth().clickable { onClick(anime.id) }, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), tonalElevation = 4.dp) {
        Box {
            anime.bannerImage?.let { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(), null, modifier = Modifier.fillMaxWidth().height(320.dp).clip(RoundedCornerShape(20.dp)), contentScale = ContentScale.Crop, alpha = 0.25f) }
            Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)), MaterialTheme.colorScheme.surfaceVariant)))
            Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Card(shape = RoundedCornerShape(14.dp), elevation = CardDefaults.cardElevation(defaultElevation = 4.dp), modifier = Modifier.width(100.dp).height(140.dp)) {
                        AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(anime.coverImage.best).crossfade(true).build(), anime.title.primary, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(anime.title.primary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                        anime.meanScore?.let { ScoreBadge(it) }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            anime.format?.let { Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(8.dp)) { Text(it.name.replace("_", " "), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)) } }
                            anime.episodes?.let { Text("$it eps", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                            anime.seasonYear?.let { Text("$it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
                if (anime.genres.isNotEmpty()) Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    anime.genres.take(4).forEach { genre ->
                        val gc = if (config.useColorfulGenres) genreColor(genre) else MaterialTheme.colorScheme.primary
                        Surface(color = gc.copy(alpha = 0.12f), shape = RoundedCornerShape(16.dp), border = androidx.compose.foundation.BorderStroke(0.5.dp, gc.copy(alpha = 0.3f))) {
                            Text(genre, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), color = gc, fontWeight = FontWeight.Medium)
                        }
                    }
                }
                anime.description?.let { Text(it.take(200) + "\u2026", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 3, overflow = TextOverflow.Ellipsis, lineHeight = 18.sp) }
                // Action row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton({ onClick(anime.id) }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Info, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Details") }
                    OutlinedButton({ /* track */ }, shape = RoundedCornerShape(12.dp), modifier = Modifier.weight(1f)) { Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp)); Spacer(Modifier.width(4.dp)); Text("Track") }
                }
            }
        }
    }
}

data class RandomState(val anime: Anime? = null, val loading: Boolean = false)
