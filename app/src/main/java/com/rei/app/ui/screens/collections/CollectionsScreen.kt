package com.rei.app.ui.screens.collections

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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class AnimeCollection(
    val id: String,
    val name: String,
    val symbol: String,
    val color: Color,
    val animeIds: List<Int> = emptyList(),
    val covers: List<String> = emptyList(),
    val updatedAt: Long = System.currentTimeMillis()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collections: List<AnimeCollection>,
    onCreateCollection: () -> Unit = {},
    onCollectionClick: (String) -> Unit = {},
    onBack: () -> Unit
) {
    var showCreateDialog by remember { mutableStateOf(false) }
    var newCollectionName by remember { mutableStateOf("") }
    var newCollectionSymbol by remember { mutableStateOf("\u2605") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Collections", fontWeight = FontWeight.Bold) },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton({ showCreateDialog = true }) { Icon(Icons.Filled.Add, "New Collection") }
                }
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showCreateDialog = true },
                shape = RoundedCornerShape(16.dp),
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(Icons.Filled.Add, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("New Collection", fontWeight = FontWeight.Bold)
            }
        }
    ) { pv ->
        if (collections.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                        Text("\u2605", fontSize = 32.sp, color = MaterialTheme.colorScheme.primary)
                    }
                    Text("No Collections Yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Create custom anime playlists to\norganize your watchlist your way.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                    FilledTonalButton({ showCreateDialog = true }, shape = RoundedCornerShape(12.dp)) {
                        Icon(Icons.Filled.Add, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create Collection", fontWeight = FontWeight.Bold)
                    }
                }
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pv),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Summary
                item {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.12f),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                    ) {
                        Row(Modifier.padding(16.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Box(Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                                Text("\u25C8", fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
                            }
                            Column(Modifier.weight(1f)) {
                                Text("${collections.size} Collection${if (collections.size != 1) "s" else ""}", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("${collections.sumOf { it.animeIds.size }} anime across all collections", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                // Default collections
                item {
                    Text("Quick Collections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 4.dp))
                }

                item {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        QuickCollectionChip("\u25B6", "Watch Next", Color(0xFF4CAF50), Modifier.weight(1f))
                        QuickCollectionChip("\u2605", "Favorites", Color(0xFFFFC107), Modifier.weight(1f))
                        QuickCollectionChip("\u2726", "Hidden Gems", Color(0xFF7C4DFF), Modifier.weight(1f))
                    }
                }

                // User collections
                item {
                    Text("My Collections", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp, bottom = 4.dp))
                }

                items(collections, key = { it.id }) { collection ->
                    CollectionCard(collection, onCollectionClick)
                }

                item { Spacer(Modifier.height(80.dp)) }
            }
        }
    }

    // Create Collection Dialog
    if (showCreateDialog) {
        AlertDialog(
            onDismissRequest = { showCreateDialog = false },
            title = { Text("New Collection", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newCollectionName,
                        onValueChange = { newCollectionName = it },
                        label = { Text("Collection Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )
                    Text("Symbol", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("\u2605", "\u2726", "\u25C6", "\u25C8", "\u25A0", "\u25B6", "\u25CE", "\u25B3", "\u2665", "\u2694", "\u266A", "\u2615")) { sym ->
                            val sel = sym == newCollectionSymbol
                            Surface(
                                onClick = { newCollectionSymbol = sym },
                                shape = CircleShape,
                                color = if (sel) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = if (sel) BorderStroke(1.dp, MaterialTheme.colorScheme.primary) else null,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) { Text(sym, fontSize = 18.sp) }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                FilledTonalButton(
                    onClick = {
                        if (newCollectionName.isNotBlank()) {
                            onCreateCollection()
                            newCollectionName = ""
                            newCollectionSymbol = "\u2605"
                        }
                        showCreateDialog = false
                    },
                    enabled = newCollectionName.isNotBlank()
                ) { Text("Create") }
            },
            dismissButton = { TextButton({ showCreateDialog = false }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun QuickCollectionChip(symbol: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Surface(
        onClick = { },
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.25f))
    ) {
        Column(Modifier.padding(vertical = 12.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(symbol, fontSize = 18.sp, color = color)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun CollectionCard(collection: AnimeCollection, onClick: (String) -> Unit) {
    Surface(
        onClick = { onClick(collection.id) },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp
    ) {
        Row(Modifier.padding(12.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            // Stacked covers or symbol
            Box(Modifier.size(56.dp)) {
                if (collection.covers.isNotEmpty()) {
                    collection.covers.take(3).forEachIndexed { index, url ->
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(56.dp - (index * 8).dp)
                                .offset(x = (index * 4).dp, y = (index * 2).dp)
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop,
                            alpha = 1f - (index * 0.15f)
                        )
                    }
                } else {
                    Box(Modifier.fillMaxSize().clip(RoundedCornerShape(12.dp)).background(collection.color.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
                        Text(collection.symbol, fontSize = 22.sp, color = collection.color)
                    }
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(collection.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("${collection.animeIds.size} anime", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }

            Icon(Icons.Filled.ChevronRight, null, tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f), modifier = Modifier.size(20.dp))
        }
    }
}
