package com.rei.app.ui.screens.imagesearch

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.data.remote.tracemoe.TraceMatch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageSearchScreen(onAnimeClick: (Int) -> Unit, vm: ImageSearchViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    var urlInput by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Gallery picker (Photo Picker API)
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let { vm.searchByUri(context, it) }
    }

    // Full file picker fallback
    val fileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { vm.searchByUri(context, it) }
    }

    Scaffold(topBar = { TopAppBar(title = { Text("Scene Search", fontWeight = FontWeight.Bold) }, subtitle = { Text("Powered by trace.moe") }) }) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // Upload Area
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Drop zone visual
                    Surface(
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                    ) {
                        Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                            Icon(Icons.Outlined.ImageSearch, null, Modifier.size(48.dp), MaterialTheme.colorScheme.primary)
                            Spacer(Modifier.height(8.dp))
                            Text("Find what anime a scene is from", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Upload a screenshot or paste a URL", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                        }
                    }

                    // Action buttons
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Camera button
                        OutlinedIconButton(
                            onClick = { /* Camera requires ActivityResultContracts.TakePicture() — add if needed */ },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoCamera, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Camera", style = MaterialTheme.typography.labelLarge)
                        }

                        // Gallery button (Photo Picker)
                        OutlinedIconButton(
                            onClick = { galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.PhotoLibrary, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("Gallery", style = MaterialTheme.typography.labelLarge)
                        }

                        // File picker (fallback)
                        OutlinedIconButton(
                            onClick = { fileLauncher.launch("image/*") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Outlined.Folder, null, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("File", style = MaterialTheme.typography.labelLarge)
                        }
                    }

                    // URL input
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = { urlInput = it },
                        label = { Text("Image URL") },
                        placeholder = { Text("https://example.com/screenshot.jpg") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            if (urlInput.isNotBlank()) {
                                IconButton(onClick = { vm.searchByUrl(urlInput) }) {
                                    Icon(Icons.Filled.Search, "Search")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }
            }

            // Loading
            if (state.loading) item {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }

            // Error
            if (state.error != null) item {
                Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(12.dp)) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.error)
                        Text(state.error!!, color = MaterialTheme.colorScheme.onErrorContainer, style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }

            // Results
            if (state.results.isNotEmpty()) item {
                Text("Results", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }

            items(state.results) { match ->
                TraceMatchCard(match, onAnimeClick)
            }
        }
    }
}

@Composable
private fun TraceMatchCard(match: TraceMatch, onAnimeClick: (Int) -> Unit) {
    val simPercent = match.similarityPercent
    val simColor = when {
        simPercent >= 90 -> Color(0xFF4CAF50)
        simPercent >= 70 -> Color(0xFFFFC107)
        simPercent >= 50 -> Color(0xFFFF9800)
        else -> Color(0xFFFF5722)
    }

    Surface(
        onClick = { match.anilistId?.let { onAnimeClick(it) } },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Row(Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
            // Thumbnail
            match.filename?.let { url ->
                AsyncImage(
                    model = ImageRequest.Builder(LocalContext.current).data(url).crossfade(true).build(),
                    contentDescription = null,
                    modifier = Modifier.width(80.dp).height(56.dp).clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(match.title?.romaji ?: match.title?.native ?: "Unknown", style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1)

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    // Similarity badge
                    Surface(color = simColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text("${simPercent}%", style = MaterialTheme.typography.labelSmall, color = simColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                    }

                    // Episode badge
                    match.episode?.let { ep ->
                        Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = RoundedCornerShape(6.dp)) {
                            Text("Ep $ep", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                        }
                    }

                    // Timestamp
                    match.timestamp?.let { ts ->
                        val min = (ts / 60).toInt()
                        val sec = (ts % 60).toInt()
                        Text(String.format("%d:%02d", min, sec), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Video preview button
            match.video?.let { url ->
                IconButton(onClick = { /* Open video preview */ }) {
                    Icon(Icons.Filled.PlayCircleFilled, null, tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}
