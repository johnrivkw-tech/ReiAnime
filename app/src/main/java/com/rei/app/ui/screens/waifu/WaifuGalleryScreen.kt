package com.rei.app.ui.screens.waifu

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import com.rei.app.data.remote.waifu.WaifuImage
import com.rei.app.ui.components.PremiumShimmer
import com.rei.app.ui.theme.LocalReiConfig
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WaifuGalleryScreen(
    onBack: () -> Unit,
    vm: WaifuGalleryViewModel = hiltViewModel()
) {
    val images by vm.images.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    val config = LocalReiConfig.current
    val scope = rememberCoroutineScope()
    var selectedCategory by remember { mutableStateOf("waifu") }
    val categories = listOf("waifu", "neko", "shinobu", "megumin", "awoo", "glomp", "handhold", "happy", "dance", "cook", "bite", "slap", "bonk", "kick", "smile", "wave", "poke", "wink", "blush", "yeet", "smug", "highfive", "cringe")

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Gallery", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("\u2726", fontSize = 18.sp, color = MaterialTheme.colorScheme.primary)
                    }
                },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
                actions = {
                    IconButton({ vm.loadMore(selectedCategory) }) {
                        Icon(Icons.Filled.Refresh, "Refresh", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Category filter chips
            ScrollableTabRow(
                selectedTabIndex = categories.indexOf(selectedCategory).coerceAtLeast(0),
                edgePadding = 16.dp,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                divider = {}
            ) {
                categories.forEachIndexed { index, cat ->
                    val selected = cat == selectedCategory
                    Tab(
                        selected = selected,
                        onClick = { selectedCategory = cat; vm.loadMore(cat) },
                        text = {
                            Text(
                                cat.replaceFirstChar { it.uppercase() },
                                fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                                color = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.primary,
                        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

            // Image grid
            if (isLoading && images.isEmpty()) {
                // Shimmer loading
                LazyVerticalGrid(
                    GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(6) {
                        PremiumShimmer(
                            Modifier.fillMaxWidth().aspectRatio(0.7f),
                            RoundedCornerShape(config.borderRadius.dp)
                        )
                    }
                }
            } else if (images.isEmpty()) {
                // Empty state
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text("\u2726", fontSize = 36.sp, color = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                        Text("No images loaded", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        FilledTonalButton({ vm.loadMore(selectedCategory) }, shape = RoundedCornerShape(12.dp)) {
                            Icon(Icons.Filled.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("Load Gallery")
                        }
                    }
                }
            } else {
                LazyVerticalGrid(
                    GridCells.Fixed(2),
                    contentPadding = PaddingValues(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(images) { img ->
                        WaifuGridItem(img, config.borderRadius)
                    }
                    // Load more button
                    item {
                        LaunchedEffect(Unit) { vm.loadMore(selectedCategory) }
                        Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) {
                            if (isLoading) CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WaifuGridItem(img: WaifuImage, borderRadius: Int) {
    Card(
        shape = RoundedCornerShape(borderRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(img.url).crossfade(true).build(),
                contentDescription = "Anime art",
                modifier = Modifier.fillMaxWidth().aspectRatio(0.75f),
                contentScale = ContentScale.Crop
            )
            // Source overlay
            if (img.source.isNotEmpty()) {
                Box(
                    Modifier.fillMaxWidth().align(Alignment.BottomStart)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.65f))))
                        .padding(horizontal = 8.dp, vertical = 5.dp)
                ) {
                    Text(
                        img.source,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            // Tags overlay
            if (img.tags.isNotEmpty()) {
                Row(
                    Modifier.align(Alignment.TopEnd).padding(6.dp),
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    img.tags.take(2).forEach { tag ->
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color.Black.copy(alpha = 0.5f)
                        ) {
                            Text(
                                tag.name,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 9.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
