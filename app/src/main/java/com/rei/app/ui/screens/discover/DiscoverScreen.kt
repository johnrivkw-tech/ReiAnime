package com.rei.app.ui.screens.discover

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rei.app.ui.components.genreColor
import com.rei.app.ui.theme.LocalReiConfig

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiscoverScreen(onAnimeClick: (Int) -> Unit, onGenreClick: (String) -> Unit = {}) {
    val config = LocalReiConfig.current

    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Discover", fontWeight = FontWeight.Bold) },
            actions = {
                IconButton({}) { Icon(Icons.Outlined.Tune, "Filters", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        )
    }) { pv ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Hero discover banner
            item {
                Box(
                    Modifier.fillMaxWidth().height(180.dp).padding(horizontal = 16.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1A0A2E), Color(0xFF3DB4F2).copy(alpha = 0.3f), Color(0xFF1A0A2E)),
                                start = androidx.compose.ui.geometry.Offset.Zero,
                                end = androidx.compose.ui.geometry.Offset(1000f, 500f)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("\u2726", fontSize = 32.sp, color = Color(0xFF3DB4F2))
                        Text("Discover Your Next Favorite", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black, color = Color.White, shadow = Shadow(Color.Black.copy(alpha = 0.5f), androidx.compose.ui.geometry.Offset(0f, 2f), 4f))
                        Text("Explore genres, trending, and hidden gems", style = MaterialTheme.typography.bodyMedium, color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            // Genre cards — premium 2-column with gradient banners
            item {
                Text("Browse Genres", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
            }

            val genres = listOf(
                Triple("Action", "\u2694", "Battles, fights, heroes"),
                Triple("Romance", "\u2665", "Love, relationships, drama"),
                Triple("Comedy", "\u263A", "Laughs, parodies, fun"),
                Triple("Fantasy", "\u2726", "Magic, worlds, quests"),
                Triple("Sci-Fi", "\u25C6", "Future, tech, space"),
                Triple("Horror", "\u25C7", "Fear, suspense, dark"),
                Triple("Isekai", "\u25CE", "Other worlds, transport"),
                Triple("Slice of Life", "\u2615", "Daily life, calm, warm"),
                Triple("Mecha", "\u25A4", "Robots, pilots, battles"),
                Triple("Mystery", "\u2299", "Clues, detectives, puzzles"),
                Triple("Drama", "\u25B3", "Emotional, stories, real"),
                Triple("Sports", "\u26BD", "Competition, teams, wins"),
                Triple("Music", "\u266A", "Bands, idols, performances"),
                Triple("Supernatural", "\u25C8", "Spirits, powers, unknown"),
                Triple("Adventure", "\u25C7", "Journeys, exploration, quests"),
                Triple("Psychological", "\u25A0", "Mind games, deep, complex")
            )

            items(genres.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { (name, symbol, desc) ->
                        val gc = if (config.useColorfulGenres) genreColor(name) else MaterialTheme.colorScheme.primary
                        Box(
                            Modifier.weight(1f).height(100.dp).clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(gc.copy(alpha = 0.7f), gc.copy(alpha = 0.3f)),
                                        start = androidx.compose.ui.geometry.Offset.Zero,
                                        end = androidx.compose.ui.geometry.Offset(600f, 400f)
                                    )
                                )
                                .clickable { onGenreClick(name) },
                            contentAlignment = Alignment.BottomStart
                        ) {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text("$symbol $name", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White, maxLines = 1, overflow = TextOverflow.Ellipsis, shadow = Shadow(Color.Black.copy(alpha = 0.4f), androidx.compose.ui.geometry.Offset(0f, 1f), 2f))
                                Text(desc, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.75f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                            }
                        }
                    }
                    if (row.size == 1) { Spacer(Modifier.weight(1f)) }
                }
            }

            // Popular Tags section
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Popular Tags", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val tags = listOf("Overpowered MC", "Slow Burn", "Time Travel", "Reincarnation", "School Life", "Dark Fantasy", "Post-Apocalyptic", "Demons", "Superpowers", "Psychological", "Survival", "Shounen", "Seinen", "Josei", "Shoujo", "Henshin", "Love Triangle", "Military", "Revenge", "Underdog")
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        tags.forEach { tag ->
                            Surface(
                                shape = RoundedCornerShape(16.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)),
                                onClick = { onGenreClick(tag) }
                            ) {
                                Text(
                                    tag,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Year browser
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.CalendarMonth, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Browse by Year", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items((2026 downTo 2000).toList()) { year ->
                            Surface(
                                onClick = { onGenreClick("") },
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                            ) {
                                Text(
                                    "$year",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Season quick access
            item {
                Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(Icons.Outlined.WbSunny, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary)
                        Text("Quick Season Access", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Winter" to "\u2744", "Spring" to "\u2618", "Summer" to "\u2600", "Fall" to "\u2643").forEach { (season, sym) ->
                            Surface(
                                onClick = { onGenreClick("") },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))
                            ) {
                                Column(
                                    Modifier.padding(vertical = 12.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(sym, fontSize = 20.sp)
                                    Text(season, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Medium)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun FlowRow(horizontalArrangement: Arrangement.Horizontal, verticalArrangement: Arrangement.Vertical, content: @Composable () -> Unit) {
    androidx.compose.foundation.layout.FlowRow(horizontalArrangement = horizontalArrangement, verticalArrangement = verticalArrangement, content = content)
}
