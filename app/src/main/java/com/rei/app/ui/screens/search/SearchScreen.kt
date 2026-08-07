package com.rei.app.ui.screens.search

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.domain.model.Anime
import com.rei.app.domain.model.MediaFormat
import com.rei.app.ui.components.*
import com.rei.app.ui.theme.LocalReiConfig
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(onAnimeClick: (Int) -> Unit, initialGenre: String = "", vm: SearchViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val config = LocalReiConfig.current
    var query by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var searchHistory by remember { mutableStateOf(listOf("Jujutsu Kaisen", "Frieren", "Solo Leveling", "Demon Slayer", "Attack on Titan")) }
    var showFilters by remember { mutableStateOf(false) }
    var genre by remember { mutableStateOf<String?>(if (initialGenre.isNotBlank()) initialGenre else null) }
    var year by remember { mutableStateOf<Int?>(null) }
    var format by remember { mutableStateOf<MediaFormat?>(null) }
    val scroll = TopAppBarDefaults.enterAlwaysScrollBehavior(rememberTopAppBarState())
    val scope = rememberCoroutineScope()

    // Auto-search when initialGenre is provided
    LaunchedEffect(initialGenre) {
        if (initialGenre.isNotBlank()) {
            query = initialGenre
            delay(100)
            vm.search("", genre = initialGenre)
            isSearching = true
        }
    }

    // Debounced search
    LaunchedEffect(query) {
        if (query.length >= 2) {
            delay(400)
            vm.search(query, genre, year, format)
            isSearching = true
        } else if (query.isEmpty()) {
            isSearching = false
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scroll.nestedScrollConnection),
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("Search anime...", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary) },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                Row {
                                    IconButton({ query = ""; vm.clear(); isSearching = false }) {
                                        Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(28.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            unfocusedBorderColor = Color.Transparent,
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            cursorColor = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyLarge
                    )
                },
                actions = {
                    IconButton({ showFilters = !showFilters }) {
                        Icon(
                            if (showFilters) Icons.Filled.FilterList else Icons.Outlined.FilterList,
                            "Filters",
                            tint = if (showFilters) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                scrollBehavior = scroll
            )
        }
    ) { pv ->
        Column(Modifier.fillMaxSize().padding(pv)) {
            // Filter panel
            AnimatedVisibility(showFilters, enter = expandVertically(), exit = shrinkVertically()) {
                Surface(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        Text("Genre", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item { GenreChip("All", genre == null) { genre = null } }
                            items(listOf("Action", "Romance", "Comedy", "Fantasy", "Sci-Fi", "Horror", "Slice of Life", "Drama", "Adventure", "Mecha", "Isekai", "Mystery")) {
                                GenreChip(it, genre == it) { genre = it; if (query.length >= 2) vm.search(query, genre, year, format) }
                            }
                        }
                        Text("Format", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            item { GenreChip("All", format == null) { format = null } }
                            items(MediaFormat.entries.filter { it.name != "MUSIC" }) {
                                GenreChip(it.name.replace("_", " "), format == it) { format = it; if (query.length >= 2) vm.search(query, genre, year, format) }
                            }
                        }
                    }
                }
            }

            // Content
            when {
                // Idle — show history + genre browse
                query.isEmpty() -> {
                    LazyColumn(Modifier.fillMaxSize(), contentPadding = PaddingValues(vertical = 8.dp)) {
                        // Search history
                        if (searchHistory.isNotEmpty()) {
                            item {
                                Row(
                                    Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Recent", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                    TextButton({ searchHistory = emptyList() }) {
                                        Text("Clear All", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                            items(searchHistory) { item ->
                                Row(
                                    Modifier.fillMaxWidth().clickable {
                                        query = item
                                        vm.search(item, genre, year, format)
                                    }.padding(horizontal = 16.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(Icons.Outlined.History, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    IconButton({
                                        searchHistory = searchHistory.filter { it != item }
                                    }) {
                                        Icon(Icons.Filled.Close, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                                    }
                                }
                            }
                        }

                        // Trending suggestions
                        item {
                            Row(
                                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Trending", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("\u25B2", fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                        val trending = listOf("Frieren: Beyond Journey's End", "Solo Leveling", "Jujutsu Kaisen S2", "Mushoku Tensei S2", "Blue Lock S2", "Spy x Family S3", "Dandadan", "Kaiju No. 8")
                        items(trending) { title ->
                            Row(
                                Modifier.fillMaxWidth().clickable {
                                    query = title
                                    searchHistory = listOf(title) + searchHistory.filter { it != title }
                                    vm.search(title, genre, year, format)
                                }.padding(horizontal = 16.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Icon(Icons.Outlined.TrendingUp, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                                Text(title, style = MaterialTheme.typography.bodyMedium)
                            }
                        }

                        // Genre browse grid
                        item {
                            Text("Browse by Genre", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                        }
                        val genres = listOf(
                            "Action" to "\u2694", "Romance" to "\u2665", "Comedy" to "\u263A", "Fantasy" to "\u2726",
                            "Sci-Fi" to "\u25C6", "Horror" to "\u25C7", "Slice of Life" to "\u2615", "Isekai" to "\u25CE",
                            "Mecha" to "\u25A4", "Mystery" to "\u2299", "Drama" to "\u25B3", "Adventure" to "\u25C7"
                        )
                        items(genres.chunked(3)) { row ->
                            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                row.forEach { (g, sym) ->
                                    val gc = if (config.useColorfulGenres) genreColor(g) else MaterialTheme.colorScheme.onSurfaceVariant
                                    Surface(
                                        onClick = {
                                            genre = g
                                            vm.search("", genre = g)
                                            query = g
                                        },
                                        modifier = Modifier.weight(1f),
                                        color = gc.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(0.5.dp, gc.copy(alpha = 0.25f))
                                    ) {
                                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                            Text(sym, fontSize = 16.sp, color = gc)
                                            Text(g, style = MaterialTheme.typography.bodyMedium, color = gc, fontWeight = FontWeight.Medium)
                                        }
                                    }
                                }
                                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
                            }
                        }
                    }
                }
                // Loading
                state is SearchState.Loading -> {
                    LazyColumn(Modifier.fillMaxSize()) {
                        items(4) {
                            Row(Modifier.padding(horizontal = 16.dp, vertical = 6.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                                PremiumShimmer(Modifier.size(64.dp).height(90.dp), RoundedCornerShape(8.dp))
                                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    PremiumShimmer(Modifier.fillMaxWidth().height(16.dp), RoundedCornerShape(4.dp))
                                    PremiumShimmer(Modifier.width(120.dp).height(12.dp), RoundedCornerShape(4.dp))
                                }
                            }
                        }
                    }
                }
                // Results
                state is SearchState.Success -> {
                    val results = (state as SearchState.Success).results
                    if (results.isEmpty()) {
                        SearchEmptyState(query)
                    } else {
                        LazyColumn(
                            Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Results count
                            item {
                                Text(
                                    "${results.size} result${if (results.size != 1) "s" else ""} for \"$query\"",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(bottom = 4.dp)
                                )
                            }
                            items(results) { anime ->
                                AnimeCard(anime, {
                                    onAnimeClick(anime.id)
                                    // Add to history
                                    searchHistory = listOf(anime.title.primary) + searchHistory.filter { it != anime.title.primary }.take(9)
                                }, Modifier.fillMaxWidth(), showProgress = true, progress = anime.mediaListEntry?.progress ?: 0, total = anime.episodes)
                            }
                        }
                    }
                }
                // Error
                state is SearchState.Error -> {
                    NetworkErrorState({ vm.search(query, genre, year, format) })
                }
            }
        }
    }
}
