package com.rei.app.ui.screens.episodes

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.data.local.EpisodeEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EpisodeGridScreen(
    mediaId: Int,
    title: String,
    totalEpisodes: Int,
    onBack: () -> Unit,
    vm: EpisodeGridViewModel = hiltViewModel()
) {
    val episodes by vm.episodes.collectAsState()
    var noteEpisode by remember { mutableStateOf<Int?>(null) }
    var noteText by remember { mutableStateOf("") }

    LaunchedEffect(mediaId) { vm.load(mediaId, totalEpisodes) }

    val watchedCount = episodes.count { it.watched }
    val progress = if (totalEpisodes > 0) watchedCount.toFloat() / totalEpisodes else 0f

    Scaffold(topBar = {
        TopAppBar(
            title = {
                Column {
                    Text("Episodes", fontWeight = FontWeight.Bold)
                    Text(title, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                }
            },
            navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } },
            actions = {
                // Quick actions
                TextButton(onClick = { vm.markAllWatched(mediaId, totalEpisodes) }) { Text("All ✓") }
                TextButton(onClick = { vm.markAllUnwatched(mediaId) }) { Text("Reset") }
            }
        )
    }) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Progress summary
            Surface(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Text("Progress", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("$watchedCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            Text("/ $totalEpisodes", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${(progress * 100).toInt()}% complete", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        val remaining = totalEpisodes - watchedCount
                        if (remaining > 0) Text("$remaining remaining", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Episode Grid
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 52.dp),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.weight(1f)
            ) {
                items((1..totalEpisodes).toList()) { epNum ->
                    val ep = episodes.find { it.episodeNumber == epNum }
                    val watched = ep?.watched == true
                    val hasNote = ep?.note != null

                    EpisodeSquare(
                        number = epNum,
                        watched = watched,
                        hasNote = hasNote,
                        onClick = { vm.toggleEpisode(mediaId, epNum, !watched) },
                        onLongClick = {
                            noteEpisode = epNum
                            noteText = ep?.note ?: ""
                        }
                    )
                }
            }
        }
    }

    // Note dialog
    noteEpisode?.let { epNum ->
        AlertDialog(
            onDismissRequest = { noteEpisode = null },
            title = { Text("Episode $epNum Note") },
            text = {
                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    label = { Text("Your notes...") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    vm.setNote(mediaId, epNum, noteText)
                    noteEpisode = null
                }) { Text("Save") }
            },
            dismissButton = { TextButton({ noteEpisode = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun EpisodeSquare(
    number: Int,
    watched: Boolean,
    hasNote: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    val bgColor = when {
        watched -> MaterialTheme.colorScheme.primary
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val contentColor = when {
        watched -> MaterialTheme.colorScheme.onPrimary
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        onClick = onClick,
        modifier = Modifier.size(48.dp),
        shape = RoundedCornerShape(10.dp),
        color = bgColor,
        border = if (!watched) BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)) else null
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "$number",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (watched) FontWeight.Bold else FontWeight.Normal,
                    color = contentColor,
                    textAlign = TextAlign.Center
                )
                if (hasNote) {
                    Box(Modifier.size(4.dp).clip(CircleShape).background(contentColor))
                }
            }
        }
    }
}
