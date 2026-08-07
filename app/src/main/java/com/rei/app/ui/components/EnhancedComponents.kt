package com.rei.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.rei.app.ui.theme.LocalReiConfig

// ═══════════════════════════════════════════
// Gradient Animated Background
// ═══════════════════════════════════════════
@Composable
fun AnimatedGradientBackground(
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        Color(0xFF0A0A12), Color(0xFF1A1A3E), Color(0xFF0D0D2A), Color(0xFF0A0A12)
    )
) {
    val infiniteTransition = rememberInfiniteTransition(label = "gradient")
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(8000), RepeatMode.Reverse),
        label = "offset"
    )
    Box(modifier = modifier.background(
        Brush.linearGradient(
            colors = colors,
            start = Offset(0f, offset * 1000f),
            end = Offset(1000f, (1f - offset) * 1000f)
        )
    ))
}

// ═══════════════════════════════════════════
// Neon Glow Card
// ═══════════════════════════════════════════
@Composable
fun NeonGlowCard(
    modifier: Modifier = Modifier,
    glowColor: Color = MaterialTheme.colorScheme.primary,
    glowRadius: Float = 12f,
    content: @Composable () -> Unit
) {
    val config = LocalReiConfig.current
    Surface(
        modifier = modifier.drawWithContent {
            drawContent()
            // Outer glow layers
            drawRoundRect(
                color = glowColor.copy(alpha = 0.05f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(config.borderRadius.dp.toPx()),
                style = Stroke(width = 4f * density)
            )
            drawRoundRect(
                color = glowColor.copy(alpha = 0.1f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(config.borderRadius.dp.toPx()),
                style = Stroke(width = 2f * density)
            )
        },
        shape = RoundedCornerShape(config.borderRadius.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, glowColor.copy(alpha = 0.4f)),
        tonalElevation = 2.dp
    ) { content() }
}

// ═══════════════════════════════════════════
// Frosted Glass Card
// ═══════════════════════════════════════════
@Composable
fun FrostedGlassCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    val config = LocalReiConfig.current
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(config.borderRadius.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.1f)),
        tonalElevation = 0.dp,
        shadowElevation = config.cardElevation.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

// ═══════════════════════════════════════════
// Animated Score Ring (circular progress)
// ═══════════════════════════════════════════
@Composable
fun ScoreRing(
    score: Int,
    modifier: Modifier = Modifier,
    size: Int = 56,
    strokeWidth: Float = 4f
) {
    val animatedScore = remember { Animatable(0f) }
    LaunchedEffect(score) { animatedScore.animateTo(score / 100f, tween(800)) }
    val scoreColor = when { score >= 80 -> Color(0xFF4CAF50); score >= 60 -> Color(0xFF8BC34A); score >= 40 -> Color(0xFFFFC107); else -> Color(0xFFFF5722) }
    val r = size.dp / 2

    Box(modifier = modifier.size(size.dp), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size.dp)) {
            val strokeWidthPx = strokeWidth * density
            drawCircle(Color.White.copy(alpha = 0.1f), radius = (size.dp.toPx() - strokeWidthPx) / 2, style = Stroke(strokeWidthPx))
            drawArc(
                scoreColor, -90f, -360f * animatedScore.value, false,
                style = Stroke(strokeWidthPx, cap = StrokeCap.Round),
                topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                size = androidx.compose.ui.geometry.Size(size.dp.toPx() - strokeWidthPx, size.dp.toPx() - strokeWidthPx)
            )
        }
        Text("${score / 10.0}", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold, color = scoreColor)
    }
}

// ═══════════════════════════════════════════
// Pulse Animation
// ═══════════════════════════════════════════
@Composable
fun PulseEffect(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit
) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val scale by infinite.animateFloat(0.95f, 1.05f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse")
    val alpha by infinite.animateFloat(0.7f, 1f, infiniteRepeatable(tween(1500), RepeatMode.Reverse), label = "pulse_alpha")
    Box(modifier.graphicsLayer { scaleX = scale; scaleY = scale }) {
        content()
    }
}

// ═══════════════════════════════════════════
// Staggered Fade In
// ═══════════════════════════════════════════
@Composable
fun StaggeredFadeIn(
    index: Int,
    content: @Composable () -> Unit
) {
    val delay = index * 80L
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { kotlinx.coroutines.delay(delay); visible = true }
    AnimatedVisibility(visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { it / 4 }) {
        content()
    }
}

// ═══════════════════════════════════════════
// Marquee Text (scrolling)
// ═══════════════════════════════════════════
@Composable
fun MarqueeText(text: String, modifier: Modifier = Modifier) {
    val offset = remember { Animatable(0f) }
    LaunchedEffect(text) { while (true) { offset.animateTo(-200f, tween(6000)); offset.snapTo(0f); kotlinx.coroutines.delay(2000) } }
    Row(modifier = modifier.horizontalScroll(rememberScrollState(), enabled = false).offset { IntOffset(offset.value.toInt(), 0) }) {
        Text(text, maxLines = 1, overflow = TextOverflow.Ellipsis, style = MaterialTheme.typography.bodyMedium)
    }
}
