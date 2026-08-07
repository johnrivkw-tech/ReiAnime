package com.rei.app.ui.screens.franchise

import androidx.compose.foundation.*
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
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest

data class FranchiseNode(
    val id: Int,
    val title: String,
    val coverImage: String? = null,
    val relationType: String? = null,
    val format: String? = null,
    val year: Int? = null,
    val depth: Int = 0,
    val isMainLine: Boolean = false
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FranchiseScreen(
    animeId: Int,
    animeTitle: String,
    onBack: () -> Unit,
    onAnimeClick: (Int) -> Unit,
    vm: FranchiseViewModel = hiltViewModel()
) {
    val nodes by vm.nodes.collectAsState()
    val loading by vm.loading.collectAsState()

    LaunchedEffect(animeId) { vm.load(animeId) }

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Column {
                    Text("Watch Order", fontWeight = FontWeight.Bold)
                    Text(animeTitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
        )
    }) { pv ->
        if (loading) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        } else if (nodes.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.AccountTree, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("No franchise data", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("This anime has no related entries", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            }
        } else {
            LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                // Legend
                item {
                    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)) {
                        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.primary))
                                Text("Main series", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.tertiary))
                                Text("Side story", style = MaterialTheme.typography.labelSmall)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Box(Modifier.size(12.dp).clip(RoundedCornerShape(3.dp)).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)))
                                Text("Other", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }

                items(nodes) { node ->
                    FranchiseNodeCard(
                        node = node,
                        onClick = { onAnimeClick(node.id) }
                    )
                }
            }
        }
    }
}

@Composable
private fun FranchiseNodeCard(node: FranchiseNode, onClick: () -> Unit) {
    val accentColor = when {
        node.isMainLine -> MaterialTheme.colorScheme.primary
        node.relationType?.contains("SIDE", ignoreCase = true) == true ||
        node.relationType?.contains("SPIN", ignoreCase = true) == true -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
    }

    val indent = 24.dp * node.depth

    Row(
        modifier = Modifier.fillMaxWidth().padding(start = indent),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Connection line
        if (node.depth > 0) {
            Box(
                Modifier.width(20.dp).height(2.dp).background(accentColor.copy(alpha = 0.3f))
            )
        }

        Surface(
            onClick = onClick,
            modifier = Modifier.weight(1f).padding(vertical = 2.dp),
            shape = RoundedCornerShape(12.dp),
            color = if (node.isMainLine) accentColor.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            border = BorderStroke(if (node.isMainLine) 1.dp else 0.5.dp, accentColor.copy(alpha = if (node.isMainLine) 0.3f else 0.15f))
        ) {
            Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Cover thumbnail
                node.coverImage?.let { url ->
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                        contentDescription = node.title,
                        modifier = Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)),
                        contentScale = ContentScale.Crop
                    )
                } ?: Box(Modifier.size(44.dp).clip(RoundedCornerShape(8.dp)).background(accentColor.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) {
                    Icon(Icons.Filled.Movie, null, tint = accentColor, modifier = Modifier.size(20.dp))
                }

                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(node.title, style = MaterialTheme.typography.bodyMedium, fontWeight = if (node.isMainLine) FontWeight.Bold else FontWeight.Medium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        node.relationType?.let { type ->
                            Surface(color = accentColor.copy(alpha = 0.15f), shape = RoundedCornerShape(4.dp)) {
                                Text(type.replace("_", " "), style = MaterialTheme.typography.labelSmall, color = accentColor, fontWeight = FontWeight.Medium, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                            }
                        }
                        node.format?.let { Text(it, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        node.year?.let { Text("• $it", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
        }
    }
}
