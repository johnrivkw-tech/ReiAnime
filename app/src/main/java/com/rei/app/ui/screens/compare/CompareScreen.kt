package com.rei.app.ui.screens.compare

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class CompareEntry(
    val title: String,
    val yourScore: Int?,
    val theirScore: Int?,
    val yourStatus: String,
    val theirStatus: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompareScreen(
    username: String,
    entries: List<CompareEntry>,
    onBack: () -> Unit
) {
    val sharedCount = entries.count { it.yourScore != null && it.theirScore != null }
    val avgDiff = if (sharedCount > 0) {
        entries.filter { it.yourScore != null && it.theirScore != null }
            .map { kotlin.math.abs((it.yourScore!! - it.theirScore!!)) }
            .average()
    } else 0.0
    val highAgreement = entries.count { it.yourScore != null && it.theirScore != null && kotlin.math.abs(it.yourScore!! - it.theirScore!!) <= 1 }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Compare Lists", fontWeight = FontWeight.Bold)
                        Text("vs $username", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { pv ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pv),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Compatibility score
            item {
                val compatScore = if (sharedCount > 0) ((highAgreement.toFloat() / sharedCount) * 100).toInt() else 0
                val compatColor = when { compatScore >= 70 -> Color(0xFF4CAF50); compatScore >= 40 -> Color(0xFFFFC107); else -> Color(0xFFFF5722) }
                Card(shape = RoundedCornerShape(20.dp), colors = CardDefaults.cardColors(containerColor = compatColor.copy(alpha = 0.08f)), border = androidx.compose.foundation.BorderStroke(0.5.dp, compatColor.copy(alpha = 0.2f))) {
                    Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Compatibility", style = MaterialTheme.typography.labelLarge, color = compatColor, fontWeight = FontWeight.Bold)
                        Text("$compatScore%", style = MaterialTheme.typography.displayLarge, fontWeight = FontWeight.Black, color = compatColor)
                        Text("Based on $sharedCount shared ratings", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            StatPillar("Shared", "$sharedCount", MaterialTheme.colorScheme.primary)
                            StatPillar("Agree", "$highAgreement", Color(0xFF4CAF50))
                            StatPillar("Avg Diff", String.format("%.1f", avgDiff), Color(0xFFFFC107))
                        }
                    }
                }
            }

            // Legend
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                    LegendItem("\u25CF", "Both Scored", MaterialTheme.colorScheme.primary)
                    LegendItem("\u25CB", "Only You", Color(0xFF42A5F5))
                    LegendItem("\u25E6", "Only Them", Color(0xFFAB47BC))
                }
            }

            // Comparison entries
            items(entries, key = { it.title }) { entry ->
                CompareEntryRow(entry)
            }

            item { Spacer(Modifier.height(80.dp)) }
        }
    }
}

@Composable
private fun StatPillar(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun LegendItem(symbol: String, label: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(symbol, fontSize = 12.sp, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun CompareEntryRow(entry: CompareEntry) {
    val bothScored = entry.yourScore != null && entry.theirScore != null
    val scoreDiff = if (bothScored) entry.yourScore!! - entry.theirScore!! else null
    val diffColor = when {
        scoreDiff == null -> MaterialTheme.colorScheme.onSurfaceVariant
        scoreDiff > 0 -> Color(0xFF42A5F5)
        scoreDiff < 0 -> Color(0xFFAB47BC)
        else -> Color(0xFF4CAF50)
    }

    Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.surface, tonalElevation = 0.5.dp) {
        Row(Modifier.padding(horizontal = 12.dp, vertical = 10.dp).fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(entry.title, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium, maxLines = 1)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (entry.yourStatus.isNotBlank()) StatusDot(entry.yourStatus)
                    if (entry.theirStatus.isNotBlank()) StatusDot(entry.theirStatus)
                }
            }

            // Your score
            Text(
                entry.yourScore?.let { "$it" } ?: "\u2014",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (entry.yourScore != null) Color(0xFF42A5F5) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            // Diff indicator
            Text(
                scoreDiff?.let { if (it > 0) "+$it" else "$it" } ?: "",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = diffColor,
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )

            // Their score
            Text(
                entry.theirScore?.let { "$it" } ?: "\u2014",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = if (entry.theirScore != null) Color(0xFFAB47BC) else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                modifier = Modifier.width(28.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun StatusDot(status: String) {
    val color = when (status) {
        "COMPLETED" -> Color(0xFF4CAF50)
        "CURRENT" -> Color(0xFF42A5F5)
        "PLANNING" -> Color(0xFFFFC107)
        "DROPPED" -> Color(0xFFFF5722)
        "PAUSED" -> Color(0xFFFF9800)
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(Modifier.size(6.dp).background(color, RoundedCornerShape(3.dp)))
}
