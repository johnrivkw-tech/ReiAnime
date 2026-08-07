package com.rei.app.ui.screens.seasonal

import androidx.compose.animation.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaSort
import com.rei.app.domain.model.Season
import com.rei.app.ui.components.AnimeCard
import com.rei.app.ui.components.PremiumShimmer
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeasonalScreen(onAnimeClick: (Int) -> Unit, vm: SeasonalViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val currentYear = Calendar.getInstance().get(Calendar.YEAR)

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Seasonal", fontWeight = FontWeight.Bold)
                        Text("${state.season.name.lowercase().replaceFirstChar { it.uppercase() }} $currentYear", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton({ }) { Icon(Icons.Outlined.FilterList, "Filter", tint = MaterialTheme.colorScheme.onSurfaceVariant) }
                },
                scrollBehavior = scroll
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Season tabs with colored indicators
            ScrollableTabRow(
                state.season.ordinal,
                edgePadding = 0.dp,
                containerColor = MaterialTheme.colorScheme.surface,
                divider = {}
            ) {
                val seasonColors = listOf(Color(0xFF90CAF9), Color(0xFFA5D6A7), Color(0xFFFFF176), Color(0xFFFFCC80))
                Season.entries.forEachIndexed { index, season ->
                    Tab(
                        selected = season == state.season,
                        onClick = { vm.setSeason(season) },
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val symbols = listOf("\u2744", "\u2618", "\u2600", "\u2643")
                                Text(symbols[index], fontSize = 14.sp)
                                Text(
                                    season.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontWeight = if (season == state.season) FontWeight.Bold else FontWeight.Normal,
                                    color = if (season == state.season) seasonColors[index] else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    )
                }
            }

            // Sort chips
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(state.sortPop, { vm.setSort(true) }, label = { Text("Popular") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer))
                FilterChip(!state.sortPop, { vm.setSort(false) }, label = { Text("Score") }, colors = FilterChipDefaults.filterChipColors(selectedContainerColor = MaterialTheme.colorScheme.primaryContainer))
            }

            // Content
            when {
                state.loading -> {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(6) { PremiumShimmer(Modifier.fillMaxWidth().height(120.dp), RoundedCornerShape(14.dp)) }
                    }
                }
                state.anime.isNotEmpty() -> {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        item { Text("${state.anime.size} anime this season", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(bottom = 4.dp)) }
                        items(state.anime, key = { it.id }) { AnimeCard(it, { onAnimeClick(it.id) }, Modifier.fillMaxWidth()) }
                    }
                }
                else -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Icon(Icons.Outlined.Movie, null, Modifier.size(64.dp), MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
                            Text("No anime found", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text("Try a different season or sort", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}
