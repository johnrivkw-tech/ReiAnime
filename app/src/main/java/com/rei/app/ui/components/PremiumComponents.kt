package com.rei.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.ui.theme.LocalReiConfig

// ═══════════════════════════════════════════
// PREMIUM SHIMMER SKELETON — gradient sweep
// ═══════════════════════════════════════════
@Composable
fun PremiumShimmer(
    modifier: Modifier = Modifier,
    shape: androidx.compose.ui.graphics.Shape = RoundedCornerShape(8.dp)
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    )
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            tween(durationMillis = 1200, easing = LinearEasing),
            RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )
    Box(
        modifier = modifier.clip(shape).background(brush)
    )
}

/** Full skeleton for a card row */
@Composable
fun CardRowSkeleton(count: Int = 3, modifier: Modifier = Modifier) {
    Row(modifier.padding(horizontal = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        repeat(count) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                PremiumShimmer(Modifier.width(140.dp).height(200.dp), RoundedCornerShape(12.dp))
                PremiumShimmer(Modifier.width(100.dp).height(14.dp), RoundedCornerShape(4.dp))
                PremiumShimmer(Modifier.width(60.dp).height(10.dp), RoundedCornerShape(4.dp))
            }
        }
    }
}

/** Full skeleton for a detail page */
@Composable
fun DetailSkeleton() {
    LazyColumn(Modifier.fillMaxSize()) {
        item { PremiumShimmer(Modifier.fillMaxWidth().height(340.dp)) }
        item {
            Row(Modifier.padding(horizontal = 24.dp, vertical = 16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                PremiumShimmer(Modifier.width(110.dp).height(155.dp), RoundedCornerShape(14.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    PremiumShimmer(Modifier.fillMaxWidth().height(24.dp), RoundedCornerShape(4.dp))
                    PremiumShimmer(Modifier.width(180.dp).height(16.dp), RoundedCornerShape(4.dp))
                    PremiumShimmer(Modifier.width(100.dp).height(14.dp), RoundedCornerShape(4.dp))
                }
            }
        }
        items(5) {
            PremiumShimmer(
                Modifier.fillMaxWidth().height(60.dp).padding(horizontal = 24.dp, vertical = 4.dp),
                RoundedCornerShape(12.dp)
            )
        }
    }
}

/** Skeleton for list items */
@Composable
fun ListSkeleton(count: Int = 5) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), contentPadding = PaddingValues(16.dp)) {
        items(count) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), verticalAlignment = Alignment.CenterVertically) {
                PremiumShimmer(Modifier.size(56.dp), RoundedCornerShape(8.dp))
                Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    PremiumShimmer(Modifier.fillMaxWidth().height(16.dp), RoundedCornerShape(4.dp))
                    PremiumShimmer(Modifier.width(120.dp).height(12.dp), RoundedCornerShape(4.dp))
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM EMPTY STATES
// ═══════════════════════════════════════════
@Composable
fun PremiumEmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector = Icons.Outlined.SearchOff,
    title: String = "Nothing here yet",
    subtitle: String? = null,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier.fillMaxWidth().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Animated icon container
        val infiniteTransition = rememberInfiniteTransition(label = "empty_pulse")
        val scale by infiniteTransition.animateFloat(
            0.9f, 1.1f,
            infiniteRepeatable(tween(2000), RepeatMode.Reverse),
            label = "empty_scale"
        )
        Box(
            Modifier.size(80.dp).graphicsLayer { scaleX = scale; scaleY = scale }
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.08f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                icon, null,
                Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
            )
        }
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface
        )
        subtitle?.let {
            Text(
                it,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        actionLabel?.let { label ->
            Spacer(Modifier.height(4.dp))
            FilledTonalButton(
                onAction ?: {},
                shape = RoundedCornerShape(12.dp)
            ) { Text(label) }
        }
    }
}

@Composable
fun TrackingEmptyState(onDiscover: () -> Unit) = PremiumEmptyState(
    icon = Icons.Outlined.BookmarkBorder,
    title = "No tracked anime",
    subtitle = "Start tracking anime to see them here",
    actionLabel = "Discover Anime",
    onAction = onDiscover
)

@Composable
fun SearchEmptyState(query: String) = PremiumEmptyState(
    icon = Icons.Outlined.SearchOff,
    title = "No results for \"$query\"",
    subtitle = "Try different keywords or check the spelling"
)

@Composable
fun NetworkErrorState(onRetry: () -> Unit) = PremiumEmptyState(
    icon = Icons.Outlined.CloudOff,
    title = "Connection lost",
    subtitle = "Check your internet and try again",
    actionLabel = "Retry",
    onAction = onRetry
)

// ═══════════════════════════════════════════
// PREMIUM BOTTOM SHEET
// ═══════════════════════════════════════════
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PremiumBottomSheet(
    onDismiss: () -> Unit,
    title: String,
    subtitle: String? = null,
    icon: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    ) {
        Column(Modifier.padding(horizontal = 20.dp)) {
            // Drag handle area
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                icon?.invoke()
                Column(Modifier.weight(1f)) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    subtitle?.let {
                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                IconButton(onDismiss) {
                    Icon(Icons.Filled.Close, null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            Spacer(Modifier.height(12.dp))
            content()
            Spacer(Modifier.height(24.dp))
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM SNACKBAR / TOAST
// ═══════════════════════════════════════════
@Composable
fun PremiumSnackbar(
    message: String,
    action: String? = null,
    onAction: (() -> Unit)? = null,
    onDismiss: () -> Unit,
    icon: @Composable (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.inverseSurface,
        shadowElevation = 8.dp,
        tonalElevation = 2.dp,
        modifier = Modifier.padding(16.dp)
    ) {
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            icon?.invoke()
            Text(
                message,
                Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.inverseOnSurface,
                fontWeight = FontWeight.Medium
            )
            action?.let { label ->
                TextButton(onAction ?: {}) {
                    Text(label, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM CHIP GROUP (for filters/tags)
// ═══════════════════════════════════════════
@Composable
fun PremiumChipGroup(
    chips: List<String>,
    selectedChip: String?,
    onChipSelected: (String?) -> Unit,
    modifier: Modifier = Modifier,
    useColors: Boolean = true
) {
    LazyRow(
        modifier.padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        contentPadding = PaddingValues(horizontal = 20.dp)
    ) {
        items(chips) { chip ->
            val selected = chip == selectedChip
            val chipColor = if (useColors) genreColor(chip) else MaterialTheme.colorScheme.primary
            Surface(
                onClick = { onChipSelected(if (selected) null else chip) },
                shape = RoundedCornerShape(20.dp),
                color = if (selected) chipColor.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                border = if (selected) BorderStroke(1.dp, chipColor.copy(alpha = 0.5f)) else BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))
            ) {
                Text(
                    chip,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                    color = if (selected) chipColor else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM STAT CARD — glassmorphic
// ═══════════════════════════════════════════
@Composable
fun PremiumStatCard(
    label: String,
    value: String,
    icon: String,
    accentColor: Color = MaterialTheme.colorScheme.primary,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier,
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        border = BorderStroke(0.5.dp, accentColor.copy(alpha = 0.15f))
    ) {
        Column(
            Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(icon, fontSize = 20.sp)
            Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = accentColor)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = TextAlign.Center)
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM RANK BADGE — for top anime
// ═══════════════════════════════════════════
@Composable
fun RankBadge(
    rank: Int,
    modifier: Modifier = Modifier
) {
    val (bg, fg) = when {
        rank == 1 -> Pair(Color(0xFFFFD700), Color(0xFF1A1A1A))  // Gold
        rank == 2 -> Pair(Color(0xFFC0C0C0), Color(0xFF1A1A1A))  // Silver
        rank == 3 -> Pair(Color(0xFFCD7F32), Color.White)         // Bronze
        rank <= 10 -> Pair(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f), MaterialTheme.colorScheme.primary)
        else -> Pair(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f), MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Surface(
        modifier.size(28.dp),
        shape = CircleShape,
        color = bg
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "$rank",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = fg
            )
        }
    }
}

// ═══════════════════════════════════════════
// PREMIUM PROGRESS INDICATOR — with label
// ═══════════════════════════════════════════
@Composable
fun PremiumProgressIndicator(
    current: Int,
    total: Int,
    label: String? = null,
    modifier: Modifier = Modifier
) {
    val fraction = if (total > 0) current.toFloat() / total else 0f
    val color = when {
        fraction >= 1f -> Color(0xFF4CAF50)
        fraction >= 0.75f -> Color(0xFF8BC34A)
        fraction >= 0.5f -> Color(0xFFFFC107)
        else -> MaterialTheme.colorScheme.primary
    }
    Column(modifier, verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (label != null) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("$current/$total", style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
        LinearProgressIndicator(
            progress = { fraction },
            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
            strokeCap = StrokeCap.Round
        )
    }
}

// ═══════════════════════════════════════════
// PREMIUM QUICK ACTION BUTTON
// ═══════════════════════════════════════════
@Composable
fun QuickActionButton(
    icon: String,
    label: String,
    color: Color = MaterialTheme.colorScheme.primary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = color.copy(alpha = 0.08f),
        border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))
    ) {
        Column(
            Modifier.padding(vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(icon, fontSize = 18.sp)
            Text(
                label,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ═══════════════════════════════════════════
// WAIFU IMAGE CARD — for random anime art
// ═══════════════════════════════════════════
@Composable
fun WaifuImageCard(
    imageUrl: String,
    source: String? = null,
    dominantColor: String = "#000000",
    modifier: Modifier = Modifier
) {
    val config = LocalReiConfig.current
    Card(
        modifier,
        shape = RoundedCornerShape(config.borderRadius.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = config.cardElevation.dp)
    ) {
        Box {
            AsyncImage(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl).crossfade(true).build(),
                contentDescription = "Anime art",
                modifier = Modifier.fillMaxWidth().aspectRatio(0.7f),
                contentScale = ContentScale.Crop
            )
            // Source overlay
            if (source != null) {
                Box(
                    Modifier.fillMaxWidth().align(Alignment.BottomStart)
                        .background(Brush.verticalGradient(listOf(Color.Transparent, Color.Black.copy(alpha = 0.7f))))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        source,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.7f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
