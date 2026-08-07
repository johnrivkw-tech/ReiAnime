package com.rei.app.ui.components

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * "What's New" bottom sheet shown on app updates.
 * Shows the latest features and improvements.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhatsNewSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(Modifier.padding(horizontal = 20.dp).verticalScroll(rememberScrollState())) {
            // Header
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("What's New", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Black)
                    Text("v2.2.0", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
                IconButton(onDismiss) {
                    Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(16.dp))

            // Feature sections
            FeatureSection(
                title = "8 API Sources",
                icon = "\u25C8",
                items = listOf(
                    "AnimeSchedule.net \u2014 Per-day airing timetables",
                    "Waifu.im \u2014 Anime art gallery with 23 categories",
                    "AniList + MAL + Kitsu + Shikimori + Trace.moe + LiveChart"
                )
            )

            FeatureSection(
                title = "AniList \u2194 MAL Sync",
                icon = "\u21C4",
                items = listOf(
                    "Full bidirectional sync between AniList and MAL",
                    "Auto-push tracking updates to both services",
                    "MAL OAuth login with PKCE security",
                    "ID cross-reference for seamless sync"
                )
            )

            FeatureSection(
                title = "18 Theme Modes",
                icon = "\u2726",
                items = listOf(
                    "Cyberpunk \u2014 Magenta/cyan neon aesthetics",
                    "Vaporwave \u2014 Pink/teal retro vibes",
                    "Deep Purple, Arctic, Terracotta \u2014 New unique palettes"
                )
            )

            FeatureSection(
                title = "80+ Customization Options",
                icon = "\u2699",
                items = listOf(
                    "Font family, card layout, banner style",
                    "Blur navigation bars, rank badges, compact cards",
                    "Notification controls, haptic on scroll",
                    "HTTP cache size, WebSocket beta"
                )
            )

            FeatureSection(
                title = "Premium Components",
                icon = "\u25C6",
                items = listOf(
                    "Gradient sweep shimmer loading",
                    "Animated empty states with pulse",
                    "Search debouncing + history + trending",
                    "Donut chart + activity heatmap in stats",
                    "Haptic feedback with 7 patterns"
                )
            )

            FeatureSection(
                title = "New Screens",
                icon = "\u25A0",
                items = listOf(
                    "Waifu Gallery \u2014 Anime art browser",
                    "Reviews \u2014 Anime review cards with voting",
                    "Detail tabs \u2014 Overview / Episodes / Related"
                )
            )

            Spacer(Modifier.height(16.dp))

            // Dismiss button
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Let's Go", fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun FeatureSection(
    title: String,
    icon: String,
    items: List<String>
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(icon, fontSize = 16.sp, color = MaterialTheme.colorScheme.primary)
            Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        }
        items.forEach { item ->
            Row(Modifier.padding(start = 24.dp, bottom = 2.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Box(Modifier.size(4.dp).background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f), RoundedCornerShape(2.dp)))
                Text(item, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, lineHeight = 16.sp)
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
