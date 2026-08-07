package com.rei.app.ui.screens.calendar

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaStatus
import com.rei.app.ui.theme.LocalReiConfig
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(onAnimeClick: (Int) -> Unit, vm: CalendarViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val today = LocalDate.now().dayOfWeek
    val config = LocalReiConfig.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Airing Schedule", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton({ }) { Icon(Icons.Outlined.Refresh, "Refresh", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Day selector — premium with today highlight
            ScrollableTabRow(
                selectedTabIndex = state.selectedDay.ordinal,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                DayOfWeek.entries.forEach { day ->
                    val isToday = day == today
                    val isSelected = day == state.selectedDay
                    Tab(
                        selected = isSelected,
                        onClick = { vm.setDay(day) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    day.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                if (isToday) {
                                    Box(Modifier.size(6.dp).background(MaterialTheme.colorScheme.primary, CircleShape))
                                }
                            }
                        }
                    )
                }
            }

            // Count header
            if (state.anime.isNotEmpty()) {
                Text(
                    "${state.anime.size} anime airing on ${state.selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}s",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
            }

            // Airing list
            if (state.anime.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)), contentAlignment = Alignment.Center) {
                            Icon(Icons.Filled.CalendarMonth, null, Modifier.size(40.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        }
                        Text("No anime airing", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("on ${state.selectedDay.getDisplayName(TextStyle.FULL, Locale.getDefault())}s", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(state.anime, key = { it.id }) { anime ->
                        AiringCard(anime, onAnimeClick)
                    }
                }
            }
        }
    }
}

@Composable
private fun AiringCard(anime: Anime, onClick: (Int) -> Unit) {
    val config = LocalReiConfig.current
    Surface(
        onClick = { onClick(anime.id) },
        shape = RoundedCornerShape(config.borderRadius.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
    ) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.spacedBy(14.dp), verticalAlignment = Alignment.CenterVertically) {
            // Time indicator — premium countdown
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(56.dp)) {
                val timeLeft = anime.nextAiringEpisode?.timeUntilAiring ?: 0
                val hours = timeLeft / 3600
                val minutes = (timeLeft % 3600) / 60
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (hours < 6) Color(0xFFFF4081).copy(alpha = 0.12f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ) {
                    Column(Modifier.padding(horizontal = 8.dp, vertical = 6.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(if (hours < 24) "${hours}h" else "${hours / 24}d", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = if (hours < 6) Color(0xFFFF4081) else MaterialTheme.colorScheme.primary)
                        if (hours < 24 && hours > 0) Text("${minutes}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                    }
                }
            }

            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(anime.title.primary, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    val statusColor = when (anime.status) { MediaStatus.RELEASING -> Color(0xFF4CAF50); else -> MaterialTheme.colorScheme.onSurfaceVariant }
                    Box(Modifier.size(6.dp).clip(CircleShape).background(statusColor))
                    anime.nextAiringEpisode?.let { next -> Text("Episode ${next.episode}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    anime.format?.let { Text("\u2022 ${it.name.replace("_", " ")}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                }
                anime.mediaListEntry?.let { entry ->
                    val progress = entry.progress
                    val total = anime.episodes ?: 0
                    if (total > 0) {
                        LinearProgressIndicator(progress = { progress.toFloat() / total }, modifier = Modifier.fillMaxWidth().height(3.dp).clip(RoundedCornerShape(2.dp)), color = MaterialTheme.colorScheme.primary, trackColor = MaterialTheme.colorScheme.surfaceVariant)
                    }
                }
            }

            // Score badge
            anime.meanScore?.let { score ->
                Surface(color = when { score >= 80 -> Color(0xFF4CAF50); score >= 60 -> Color(0xFF8BC34A); else -> Color(0xFFFFC107) }.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
                    Text("${score / 10.0}", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = when { score >= 80 -> Color(0xFF4CAF50); score >= 60 -> Color(0xFF8BC34A); else -> Color(0xFFFFC107) })
                }
            }
        }
    }
}

data class CalendarState(val selectedDay: DayOfWeek = LocalDate.now().dayOfWeek, val anime: List<Anime> = emptyList(), val loading: Boolean = false)
