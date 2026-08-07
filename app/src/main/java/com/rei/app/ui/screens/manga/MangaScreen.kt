package com.rei.app.ui.screens.manga

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.rei.app.data.remote.mangadex.MangaDexEntry

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MangaScreen(onBack: () -> Unit, vm: MangaViewModel = hiltViewModel()) {
    val trending by vm.trending.collectAsState()
    val recent by vm.recent.collectAsState()
    val searchResults by vm.searchResults.collectAsState()
    val query by vm.query.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Manga", fontWeight = FontWeight.Bold) },
                subtitle = { Text("via MangaDex", style = MaterialTheme.typography.labelSmall) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    if (isLoading) CircularProgressIndicator(modifier = Modifier.size(20.dp).padding(end = 12.dp), strokeWidth = 2.dp)
                    else IconButton({ vm.refresh() }) { Icon(Icons.Filled.Refresh, "Refresh") }
                }
            )
        }
    ) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Search bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { vm.search(it) },
                    placeholder = { Text("Search manga...") },
                    leadingIcon = { Icon(Icons.Filled.Search, null) },
                    trailingIcon = { if (query.isNotEmpty()) IconButton({ vm.clearSearch() }) { Icon(Icons.Filled.Close, null) } },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    singleLine = true
                )
            }

            // Search results
            if (searchResults.isNotEmpty()) {
                item { SectionLabel("Results (${searchResults.size})") }
                items(searchResults) { manga -> MangaCard(manga) }
            }

            // Trending
            if (trending.isNotEmpty() && searchResults.isEmpty()) {
                item { SectionLabel("\u25B2 Trending Manga") }
                items(trending) { manga -> MangaCard(manga) }
            }

            // Recently Updated
            if (recent.isNotEmpty() && searchResults.isEmpty()) {
                item { SectionLabel("\u25B6 Recently Updated") }
                items(recent) { manga -> MangaCard(manga) }
            }

            // Empty state
            if (trending.isEmpty() && recent.isEmpty() && searchResults.isEmpty() && !isLoading) {
                item {
                    Box(Modifier.fillMaxWidth().padding(vertical = 60.dp), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("\u25CE", fontSize = 40.sp, color = MaterialTheme.colorScheme.primary)
                            Text("No manga loaded", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            FilledTonalButton({ vm.refresh() }, shape = RoundedCornerShape(12.dp)) { Text("Refresh") }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
}

@Composable
private fun MangaCard(manga: MangaDexEntry) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 1.dp) {
        Row(Modifier.padding(10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Cover
            Card(shape = RoundedCornerShape(10.dp), elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), modifier = Modifier.width(60.dp).height(84.dp)) {
                manga.coverUrl?.let { url ->
                    AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(), contentDescription = manga.title, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                } ?: Box(Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceVariant), contentAlignment = Alignment.Center) {
                    Text("\u25CE", fontSize = 20.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f))
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(3.dp)) {
                Text(manga.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    if (manga.status.isNotBlank()) {
                        val statusColor = when (manga.status) {
                            "completed" -> Color(0xFF4CAF50); "ongoing" -> Color(0xFF42A5F5); "hiatus" -> Color(0xFFFF9800); else -> Color.Gray
                        }
                        Surface(color = statusColor.copy(alpha = 0.12f), shape = RoundedCornerShape(4.dp)) {
                            Text(manga.status.replaceFirstChar { it.uppercase() }, style = MaterialTheme.typography.labelSmall, color = statusColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                        }
                    }
                    manga.year?.let { Text("$it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                if (manga.authors.isNotEmpty()) {
                    Text("by ${manga.authors.take(2).joinToString(", ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                if (manga.tags.isNotEmpty()) {
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        manga.tags.take(3).forEach { tag ->
                            Surface(color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f), shape = RoundedCornerShape(4.dp)) {
                                Text(tag, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp), fontSize = 9.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
