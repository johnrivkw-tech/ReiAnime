package com.rei.app.ui.components

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

// ═══════════════════════════════════════════
// SEARCH DEBOUNCING
// ═══════════════════════════════════════════
class SearchDebouncer(private val delayMs: Long = 400L) {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private val _debouncedQuery = MutableStateFlow("")
    val debouncedQuery: StateFlow<String> = _debouncedQuery.asStateFlow()

    suspend fun start() {
        _query
            .debounce(delayMs)
            .distinctUntilChanged()
            .collect { _debouncedQuery.value = it }
    }

    fun update(query: String) { _query.value = query }
    fun clear() { _query.value = "" }
}

// ═══════════════════════════════════════════
// SEARCH HISTORY (in-memory, persisted by caller)
// ═══════════════════════════════════════════
class SearchHistory(private val maxItems: Int = 20) {
    private val _history = MutableStateFlow<List<String>>(emptyList())
    val history: StateFlow<List<String>> = _history.asStateFlow()

    fun add(query: String) {
        if (query.isBlank()) return
        val current = _history.value.filter { it != query }.toMutableList()
        current.add(0, query)
        _history.value = current.take(maxItems)
    }

    fun remove(query: String) {
        _history.value = _history.value.filter { it != query }
    }

    fun clear() { _history.value = emptyList() }
}

// ═══════════════════════════════════════════
// PREMIUM SEARCH BAR
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: (String) -> Unit,
    onClear: () -> Unit,
    placeholder: String = "Search anime...",
    modifier: Modifier = Modifier,
    active: Boolean = true,
    onActiveChange: (Boolean) -> Unit = {},
    history: List<String> = emptyList(),
    onHistoryDelete: (String) -> Unit = {},
    onHistoryClear: () -> Unit = {},
    suggestions: List<String> = emptyList()
) {
    SearchBar(
        query = query,
        onQueryChange = onQueryChange,
        onSearch = { onSearch(it) },
        active = active,
        onActiveChange = onActiveChange,
        modifier = modifier,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = { Icon(Icons.Filled.Search, null, tint = MaterialTheme.colorScheme.primary) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onClear(); onQueryChange("") }) {
                    Icon(Icons.Filled.Close, "Clear", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        },
        shape = RoundedCornerShape(28.dp),
        colors = SearchBarDefaults.colors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
            inputFieldColors = TextFieldDefaults.colors(
                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    ) {
        // Search history
        if (query.isEmpty() && history.isNotEmpty()) {
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Recent", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                TextButton(onHistoryClear) {
                    Text("Clear All", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            history.take(5).forEach { item ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSearch(item) }.padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.History, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text(item, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                    IconButton({ onHistoryDelete(item) }) {
                        Icon(Icons.Filled.Close, null, Modifier.size(14.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
        }

        // Trending suggestions
        if (query.isEmpty() && suggestions.isNotEmpty()) {
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Trending", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                Text("\u25B2", color = MaterialTheme.colorScheme.primary)
            }
            suggestions.take(8).forEach { suggestion ->
                Row(
                    Modifier.fillMaxWidth().clickable { onSearch(suggestion) }.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Outlined.TrendingUp, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                    Text(suggestion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM FILTER CHIPS ROW
// ═══════════════════════════════════════════
@Composable
fun FilterChipRow(
    filters: List<Pair<String, String>>,  // (id, label)
    selectedFilters: Set<String>,
    onToggle: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyRow(
        modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(filters) { (id, label) ->
            val selected = id in selectedFilters
            FilterChip(
                selected = selected,
                onClick = { onToggle(id) },
                label = { Text(label, style = MaterialTheme.typography.labelMedium) },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                    selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                ),
                border = FilterChipDefaults.filterChipBorder(
                    borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f),
                    selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                    enabled = true,
                    selected = selected
                )
            )
        }
    }
}
