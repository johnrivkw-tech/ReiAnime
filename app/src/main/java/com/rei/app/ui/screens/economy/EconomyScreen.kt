package com.rei.app.ui.screens.economy

import androidx.compose.animation.*
import androidx.compose.foundation.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.economy.ReiShop

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EconomyScreen(vm: EconomyViewModel = hiltViewModel()) {
    val balance by vm.balance.collectAsState()
    val streak by vm.streak.collectAsState()
    val unlockedIds by vm.unlockedIds.collectAsState()
    var selectedCategory by remember { mutableStateOf("Themes") }
    var showPurchaseDialog by remember { mutableStateOf<ReiShop.ShopItem?>(null) }
    var justPurchased by remember { mutableStateOf<String?>(null) }

    Scaffold(topBar = {
        TopAppBar(title = {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Rei Shop", fontWeight = FontWeight.Bold)
            }
        }, actions = {
            // Coin balance badge
            Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF7C4DFF).copy(alpha = 0.15f)) {
                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("◈", fontSize = 16.sp)
                    Text("$balance", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                }
            }
            Spacer(Modifier.width(8.dp))
        })
    }) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {

            // ── Balance Hero Card ──
            item {
                Surface(shape = RoundedCornerShape(20.dp), color = Color(0xFF7C4DFF).copy(alpha = 0.08f), border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.2f))) {
                    Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("◈", fontSize = 36.sp)
                        Text(balance.toString(), style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Black, color = Color(0xFF7C4DFF))
                        Text("Rei Coins", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        if (streak > 0) {
                            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFF6D00).copy(alpha = 0.12f)) {
                                Row(Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("▲", fontSize = 14.sp)
                                    Text("${streak} day streak", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00))
                                }
                            }
                        }
                    }
                }
            }

            // ── How to Earn ──
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Earn Coins", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    val earnMethods = listOf(
                        "Daily Login" to "+20C", "Track an Anime" to "+10C",
                        "Complete an Anime" to "+50C", "Rate an Anime" to "+5C",
                        "Watch Episode" to "+2C", "Random Discover" to "+1C",
                        "First Search/Day" to "+5C", "Write a Note" to "+3C",
                        "Scene Search" to "+2C", "Favorite" to "+3C"
                    )
                    earnMethods.chunked(2).forEach { row ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { (label, coins) ->
                                Surface(Modifier.weight(1f), shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)) {
                                    Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text(label, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(coins, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // ── Category Tabs ──
            item {
                ScrollableTabRow(selectedTabIndex = ReiShop.CATEGORIES.indexOf(selectedCategory).coerceAtLeast(0), edgePadding = 0.dp, containerColor = MaterialTheme.colorScheme.surface, divider = {}) {
                    ReiShop.CATEGORIES.forEach { cat ->
                        Tab(selected = cat == selectedCategory, onClick = { selectedCategory = cat }, text = { Text(cat, fontWeight = if (cat == selectedCategory) FontWeight.Bold else FontWeight.Normal) })
                    }
                }
            }

            // ── Shop Items ──
            val items = ReiShop.ALL_ITEMS.filter { it.category == selectedCategory }
            items(items) { item ->
                val unlocked = item.id in unlockedIds
                val canAfford = balance >= item.cost
                val isJustPurchased = justPurchased == item.id
                val isMythic = item.rarity == "Mythic"
                val rarityColor = ReiShop.RARITY_COLORS[item.rarity] ?: Color.Gray

                Surface(
                    onClick = { if (!unlocked) showPurchaseDialog = item },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isMythic && !unlocked) Color(0xFF1A0A2E).copy(alpha = 0.85f)
                           else if (unlocked) Color(0xFF4CAF50).copy(alpha = 0.06f)
                           else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    border = BorderStroke(
                        if (isMythic) 1.5.dp else if (unlocked) 1.dp else 0.5.dp,
                        if (isMythic) rarityColor.copy(alpha = 0.6f)
                        else if (unlocked) Color(0xFF4CAF50).copy(alpha = 0.3f)
                        else MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
                    )
                ) {
                    Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        // Emoji icon — mythic gets animated glow ring
                        Box(Modifier.size(44.dp).clip(RoundedCornerShape(12.dp)).background(
                            if (isMythic) Color(0xFFFF1744).copy(alpha = 0.18f)
                            else if (unlocked) Color(0xFF4CAF50).copy(alpha = 0.1f)
                            else if (canAfford) Color(0xFF7C4DFF).copy(alpha = 0.1f)
                            else MaterialTheme.colorScheme.surfaceVariant
                        ), contentAlignment = Alignment.Center) {
                            Text(item.emoji, fontSize = if (isMythic) 24.sp else 22.sp, color = if (isMythic) Color.White else Color.Unspecified)
                        }

                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                            Text(item.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis, color = if (isMythic) Color.White else Color.Unspecified)
                            // Description for mythic items
                            if (isMythic && item.description.isNotBlank()) {
                                Text(item.description, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f), maxLines = 2, overflow = TextOverflow.Ellipsis, lineHeight = 14.sp)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                                Surface(color = rarityColor.copy(alpha = if (isMythic) 0.3f else 0.15f), shape = RoundedCornerShape(4.dp)) {
                                    Text(item.rarity, style = MaterialTheme.typography.labelSmall, color = if (isMythic) Color.White else rarityColor, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp))
                                }
                                Text(item.category, style = MaterialTheme.typography.labelSmall, color = if (isMythic) Color.White.copy(alpha = 0.5f) else MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        // Price / Status
                        if (unlocked) {
                            Surface(shape = RoundedCornerShape(8.dp), color = Color(0xFF4CAF50).copy(alpha = 0.15f)) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(Icons.Filled.CheckCircle, null, Modifier.size(14.dp), Color(0xFF4CAF50))
                                    Text("Owned", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFF4CAF50))
                                }
                            }
                        } else {
                            Surface(shape = RoundedCornerShape(8.dp), color = if (isMythic) Color(0xFFFF1744).copy(alpha = 0.2f) else if (canAfford) Color(0xFF7C4DFF).copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant) {
                                Row(Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(if (isMythic) "\u2742" else "\u25C8", fontSize = 12.sp, color = if (isMythic) Color(0xFFFF1744) else Color.Unspecified)
                                    Text("${item.cost}", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (isMythic) Color(0xFFFF1744) else if (canAfford) Color(0xFF7C4DFF) else MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }

    // Purchase confirmation dialog
    showPurchaseDialog?.let { item ->
        val canAfford = balance >= item.cost
        AlertDialog(
            onDismissRequest = { showPurchaseDialog = null },
            title = { Text("Unlock ${item.name}?") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(item.emoji, fontSize = 32.sp)
                        Text(item.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    }
                    Divider()
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Cost:", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("◈"); Text("${item.cost}", fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF)) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Your balance:", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("◈"); Text("$balance", fontWeight = FontWeight.Bold) }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("After:", style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) { Text("◈"); Text("${balance - item.cost}", fontWeight = FontWeight.Bold, color = if (canAfford) Color(0xFF4CAF50) else MaterialTheme.colorScheme.error) }
                    }
                    if (!canAfford) {
                        Surface(color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f), shape = RoundedCornerShape(8.dp)) {
                            Text("Not enough coins! Keep tracking anime to earn more.", modifier = Modifier.padding(8.dp), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            },
            confirmButton = {
                if (canAfford) FilledTonalButton(onClick = {
                    vm.purchase(item)
                    justPurchased = item.id
                    showPurchaseDialog = null
                }) { Text("Unlock ${item.cost}") }
            },
            dismissButton = { TextButton({ showPurchaseDialog = null }) { Text("Cancel") } }
        )
    }
}
