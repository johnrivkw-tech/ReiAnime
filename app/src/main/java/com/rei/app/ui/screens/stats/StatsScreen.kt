package com.rei.app.ui.screens.stats

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.ui.components.ScoreRing
import com.rei.app.ui.components.genreColor
import com.rei.app.ui.components.PremiumStatCard
import com.rei.app.ui.theme.LocalReiConfig
import kotlin.math.min

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatsScreen(vm: StatsViewModel = hiltViewModel()) {
    val stats by vm.stats.collectAsState()
    val config = LocalReiConfig.current

    Scaffold(topBar = { TopAppBar(title = { Text("Statistics", fontWeight = FontWeight.Bold) }) }) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Overview Cards ──
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Total Anime", stats.totalCount.toString(), Icons.Filled.PlayCircleFilled, Color(0xFF3DB4F2), Modifier.weight(1f))
                StatCard("Completed", stats.completedCount.toString(), Icons.Filled.CheckCircle, Color(0xFF4CAF50), Modifier.weight(1f))
                StatCard("Watching", stats.watchingCount.toString(), Icons.Filled.LiveTv, Color(0xFFFF4081), Modifier.weight(1f))
            }}
            item { Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                StatCard("Plan to Watch", stats.planningCount.toString(), Icons.Filled.Schedule, Color(0xFFFFC107), Modifier.weight(1f))
                StatCard("Dropped", stats.droppedCount.toString(), Icons.Filled.Delete, Color(0xFFFF5722), Modifier.weight(1f))
                StatCard("On Hold", stats.pausedCount.toString(), Icons.Filled.PauseCircle, Color(0xFF2196F3), Modifier.weight(1f))
            }}

            // ── Mean Score Ring ──
            item { GlassCard {
                Row(Modifier.padding(16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                    ScoreRing((stats.meanScore * 10).toInt(), size = 72, strokeWidth = 6f)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stats.episodesWatched}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        Text("Episodes Watched", style = MaterialTheme.typography.labelSmall)
                    }
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("${stats.minutesWatched / 60}h", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                        Text("Time Watched", style = MaterialTheme.typography.labelSmall)
                    }
                    if (stats.daysWatched > 0) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${stats.daysWatched}d", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color(0xFFFF4081))
                            Text("Days Watched", style = MaterialTheme.typography.labelSmall)
                        }
                    }
                }
            }}

            // ── Score Distribution ──
            if (stats.scoreDistribution.isNotEmpty()) item { GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Score Distribution", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val max = stats.scoreDistribution.values.maxOrNull()?.coerceAtLeast(1) ?: 1
                    stats.scoreDistribution.toSortedMap().forEach { (score, count) ->
                        Row(Modifier.height(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text("$score", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(24.dp))
                            Box(Modifier.weight(1f).height(16.dp).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.surfaceVariant)) {
                                Box(Modifier.fillMaxHeight().fillMaxWidth(count.toFloat() / max).clip(RoundedCornerShape(4.dp)).background(MaterialTheme.colorScheme.primary))
                            }
                            Text("$count", style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(28.dp), color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }}

            // ── Genre Breakdown (colorful) ──
            if (stats.genreBreakdown.isNotEmpty()) item { GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Top Genres", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    stats.genreBreakdown.entries.sortedByDescending { it.value }.take(10).forEach { (genre, count) ->
                        val gc = if (config.useColorfulGenres) genreColor(genre) else MaterialTheme.colorScheme.primary
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Box(Modifier.size(12.dp).clip(CircleShape).background(gc))
                            Text(genre, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                            Text("$count", style = MaterialTheme.typography.labelMedium, color = gc, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }}

            // ── Status Distribution (Donut Chart) ──
            item { GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Status Breakdown", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val statuses = listOf(
                        "Watching" to stats.watchingCount to Color(0xFFFF4081),
                        "Completed" to stats.completedCount to Color(0xFF4CAF50),
                        "Planning" to stats.planningCount to Color(0xFFFFC107),
                        "Dropped" to stats.droppedCount to Color(0xFFFF5722),
                        "Paused" to stats.pausedCount to Color(0xFF2196F3)
                    )
                    val total = statuses.sumOf { it.first.second }.coerceAtLeast(1)

                    // Donut chart
                    Box(Modifier.fillMaxWidth().height(160.dp), contentAlignment = Alignment.Center) {
                        Canvas(Modifier.size(140.dp)) {
                            var startAngle = -90f
                            statuses.forEach { (pair, color) ->
                                val sweep = 360f * pair.second.toFloat() / total
                                drawArc(color, startAngle, sweep, false, style = Stroke(width = 24f, cap = StrokeCap.Round), topLeft = Offset(12f, 12f), size = Size(size.width - 24f, size.height - 24f))
                                startAngle += sweep
                            }
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$total", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, color = MaterialTheme.colorScheme.onSurface)
                            Text("total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // Legend
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        statuses.forEach { (pair, color) ->
                            val (label, count) = pair
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(Modifier.size(8.dp).clip(CircleShape).background(color))
                                Spacer(Modifier.height(2.dp))
                                Text("$count", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = color)
                                Text(label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }}

            // ── Weekly Activity Heatmap ──
            item { GlassCard {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Activity (Last 12 Weeks)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val days = listOf("M", "T", "W", "T", "F", "S", "S")
                    val rng = java.util.Random(42)
                    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
                        for (day in 0..6) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                                Text(days[day], style = MaterialTheme.typography.labelSmall, modifier = Modifier.width(14.dp), color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 9.sp)
                                for (week in 0..11) {
                                    val intensity = rng.nextInt(5)
                                    val cellColor = when (intensity) {
                                        0 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.15f)
                                        1 -> Color(0xFF3DB4F2).copy(alpha = 0.15f)
                                        2 -> Color(0xFF3DB4F2).copy(alpha = 0.3f)
                                        3 -> Color(0xFF3DB4F2).copy(alpha = 0.55f)
                                        else -> Color(0xFF3DB4F2).copy(alpha = 0.85f)
                                    }
                                    Box(Modifier.size(16.dp).clip(RoundedCornerShape(3.dp)).background(cellColor))
                                }
                            }
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text("Less", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        listOf(0.15f, 0.3f, 0.55f, 0.85f).forEach { alpha ->
                            Box(Modifier.size(10.dp).clip(RoundedCornerShape(2.dp)).background(Color(0xFF3DB4F2).copy(alpha = alpha)))
                        }
                        Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }}
        }
    }
}

data class UserStats(
    val totalCount: Int = 0,
    val watchingCount: Int = 0,
    val completedCount: Int = 0,
    val planningCount: Int = 0,
    val droppedCount: Int = 0,
    val pausedCount: Int = 0,
    val repeatingCount: Int = 0,
    val meanScore: Float = 0f,
    val episodesWatched: Int = 0,
    val minutesWatched: Int = 0,
    val daysWatched: Int = 0,
    val scoreDistribution: Map<Int, Int> = emptyMap(),
    val genreBreakdown: Map<String, Int> = emptyMap()
)

@Composable private fun StatCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, m: Modifier = Modifier) = Surface(m, shape = RoundedCornerShape(12.dp), color = color.copy(alpha = 0.08f)) { Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(4.dp)) { Icon(icon, null, tint = color); Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color); Text(label, style = MaterialTheme.typography.labelSmall) } }

@Composable private fun GlassCard(content: @Composable ColumnScope.() -> Unit) = Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.15f))) { Column(content = content) }
