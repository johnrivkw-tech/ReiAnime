package com.rei.app.ui.components.cosmetics

import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

// ═══════════════════════════════════════════════════════════
// PARTICLE BURST — Celebratory animation for completions/coins
// ═══════════════════════════════════════════════════════════

data class Particle(
    val x: Float,
    val y: Float,
    val vx: Float,
    val vy: Float,
    val color: Color,
    val size: Float,
    val symbol: String,
    val lifeMs: Long
)

@Composable
fun ParticleBurstOverlay(
    active: Boolean,
    colors: List<Color> = listOf(Color(0xFFFFD700), Color(0xFFFF6D00), Color(0xFF7C4DFF), Color(0xFF4CAF50), Color(0xFFFF4081)),
    symbols: List<String> = listOf("\u2605", "\u2726", "\u25C8", "\u2742", "\u2666", "\u25C6", "\u2735", "\u265B"),
    onFinished: () -> Unit = {}
) {
    if (!active) return

    val particles = remember {
        (0..24).map {
            val angle = Random.nextFloat() * 2f * kotlin.math.PI.toFloat()
            val speed = 1.5f + Random.nextFloat() * 3f
            Particle(
                x = 0.5f, y = 0.5f,
                vx = cos(angle) * speed,
                vy = sin(angle) * speed - 2f,  // upward bias
                color = colors.random(),
                size = 6f + Random.nextFloat() * 10f,
                symbol = symbols.random(),
                lifeMs = 800L + Random.nextLong(0, 600)
            )
        }
    }

    val infinite = rememberInfiniteTransition(label = "particles")
    val progress by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(1400), RepeatMode.Restart),
        label = "particle_progress"
    )

    Box(Modifier.fillMaxSize()) {
        particles.forEach { p ->
            val t = progress
            val x = p.x + p.vx * t * 0.3f
            val y = p.y + p.vy * t * 0.3f + 0.5f * t * t  // gravity
            val alpha = (1f - t).coerceIn(0f, 1f)
            val scale = 1f - t * 0.5f

            if (alpha > 0.05f) {
                Text(
                    p.symbol,
                    fontSize = p.size.sp,
                    color = p.color.copy(alpha = alpha),
                    modifier = Modifier
                        .offset(x = (x * 300 - 150).dp, y = (y * 400 - 200).dp)
                        .graphicsLayer { scaleX = scale; scaleY = scale; this.alpha = alpha }
                )
            }
        }
    }

    // Auto-dismiss after animation
    LaunchedEffect(active) {
        if (active) {
            kotlinx.coroutines.delay(1500)
            onFinished()
        }
    }
}

// ═══════════════════════════════════════════════════════════
// ANIMATED COIN COUNTER — Numbers roll up smoothly
// ═══════════════════════════════════════════════════════════

@Composable
fun AnimatedCoinCounter(
    targetBalance: Int,
    modifier: Modifier = Modifier,
    coinColor: Color = Color(0xFF7C4DFF),
    symbol: String = "\u25C8"
) {
    val animatedBalance = remember { Animatable(0f) }
    LaunchedEffect(targetBalance) {
        animatedBalance.animateTo(
            targetBalance.toFloat(),
            spring(stiffness = Spring.StiffnessLow, dampingRatio = Spring.DampingRatioMediumBouncy)
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = coinColor.copy(alpha = 0.1f),
        border = BorderStroke(0.5.dp, coinColor.copy(alpha = 0.3f))
    ) {
        Row(
            Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Spinning coin effect
            val infinite = rememberInfiniteTransition(label = "coin_spin")
            val rotation by infinite.animateFloat(
                0f, 360f,
                infiniteRepeatable(tween(3000), RepeatMode.Restart),
                label = "coin_rot"
            )
            Text(
                symbol,
                fontSize = 18.sp,
                color = coinColor,
                modifier = Modifier.graphicsLayer { rotationZ = rotation }
            )
            Text(
                "${animatedBalance.value.toInt()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = coinColor
            )
        }
    }
}

// ═══════════════════════════════════════════════════════════
// SPRING PRESS CARD — Bouncy press animation
// ═══════════════════════════════════════════════════════════

@Composable
fun SpringPressCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val pressed = remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed.value) {
        if (pressed.value) {
            scale.animateTo(0.96f, spring(stiffness = Spring.StiffnessHigh))
            scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy))
        }
    }

    Surface(
        onClick = {
            pressed.value = true
            onClick()
            // Reset after animation
        },
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        shape = RoundedCornerShape(14.dp)
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════
// GLOWING DOT — Animated pulsing dot for airing status
// ═══════════════════════════════════════════════════════════

@Composable
fun GlowingDot(
    color: Color,
    modifier: Modifier = Modifier,
    size: Int = 8
) {
    val infinite = rememberInfiniteTransition(label = "glow_dot")
    val alpha by infinite.animateFloat(
        0.5f, 1f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow_alpha"
    )
    val scale by infinite.animateFloat(
        0.8f, 1.2f,
        infiniteRepeatable(tween(1200), RepeatMode.Reverse),
        label = "glow_scale"
    )

    Box(
        modifier
            .size(size.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(CircleShape)
            .background(color.copy(alpha = alpha))
    )
}

// ═══════════════════════════════════════════════════════════
// SHIMMER TEXT — Loading text placeholder
// ═══════════════════════════════════════════════════════════

@Composable
fun ShimmerText(
    width: Int = 120,
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "shimmer_text")
    val offset by infinite.animateFloat(
        0f, 1f,
        infiniteRepeatable(tween(1500), RepeatMode.Restart),
        label = "shimmer_offset"
    )

    Box(
        modifier
            .width(width.dp)
            .height(14.dp)
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f + offset * 0.15f))
    )
}

// ═══════════════════════════════════════════════════════════
// RIPPLE BUTTON — Custom ripple with color accent
// ═══════════════════════════════════════════════════════════

@Composable
fun RippleButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = MaterialTheme.colorScheme.primary,
    contentColor: Color = MaterialTheme.colorScheme.onPrimary,
    icon: String? = null
) {
    val pressed = remember { mutableStateOf(false) }
    val scale = remember { Animatable(1f) }

    LaunchedEffect(pressed.value) {
        if (pressed.value) {
            scale.animateTo(0.95f, tween(100))
            scale.animateTo(1f, spring(stiffness = Spring.StiffnessMedium, dampingRatio = Spring.DampingRatioMediumBouncy))
            pressed.value = false
        }
    }

    FilledTonalButton(
        onClick = { pressed.value = true; onClick() },
        modifier = modifier.graphicsLayer {
            scaleX = scale.value
            scaleY = scale.value
        },
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = containerColor.copy(alpha = 0.15f),
            contentColor = containerColor
        )
    ) {
        if (icon != null) {
            Text(icon, fontSize = 14.sp, modifier = Modifier.padding(end = 4.dp))
        }
        Text(text, fontWeight = FontWeight.Bold)
    }
}

// ═══════════════════════════════════════════════════════════
// TIME-OF-DAY GRADIENT — Ambient background that shifts
// ═══════════════════════════════════════════════════════════

@Composable
fun TimeOfDayGradient(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
    val gradientColors = when (hour) {
        in 5..7 -> listOf(Color(0xFF1A0A2E), Color(0xFF2D1B69), Color(0xFFFF6D00).copy(alpha = 0.2f))   // Dawn
        in 8..11 -> listOf(Color(0xFF0D47A1).copy(alpha = 0.3f), Color(0xFF42A5F5).copy(alpha = 0.15f), Color(0xFF0D47A1).copy(alpha = 0.2f))  // Morning
        in 12..16 -> listOf(Color(0xFF1A237E).copy(alpha = 0.2f), Color(0xFF7C4DFF).copy(alpha = 0.1f), Color(0xFF1A237E).copy(alpha = 0.15f))  // Afternoon
        in 17..19 -> listOf(Color(0xFF1A0A2E), Color(0xFFFF6D00).copy(alpha = 0.15f), Color(0xFFE91E63).copy(alpha = 0.1f))  // Sunset
        in 20..22 -> listOf(Color(0xFF0A0A12), Color(0xFF1A1A3E).copy(alpha = 0.8f), Color(0xFF0D0D2A))  // Evening
        else -> listOf(Color(0xFF050510), Color(0xFF0A0A1A), Color(0xFF050510))  // Deep night
    }

    Box(
        modifier.background(
            androidx.compose.ui.graphics.Brush.verticalGradient(gradientColors)
        )
    ) {
        content()
    }
}

// ═══════════════════════════════════════════════════════════
// SEASON COLORS — Auto-detect current season for accents
// ═══════════════════════════════════════════════════════════

object SeasonPalette {
    data class SeasonColors(
        val primary: Color,
        val secondary: Color,
        val tertiary: Color,
        val background: Color,
        val symbol: String,
        val name: String
    )

    val WINTER = SeasonColors(Color(0xFF42A5F5), Color(0xFF80DEEA), Color(0xFFE1F5FE), Color(0xFF0D1B2A), "\u2744", "Winter")
    val SPRING = SeasonColors(Color(0xFFFF80AB), Color(0xFFCE93D8), Color(0xFFF8BBD0), Color(0xFF1A0A2E), "\u2618", "Spring")
    val SUMMER = SeasonColors(Color(0xFFFF6D00), Color(0xFFFFAB40), Color(0xFFFFE0B2), Color(0xFF1A0A00), "\u2600", "Summer")
    val FALL = SeasonColors(Color(0xFFFF5722), Color(0xFF8D6E63), Color(0xFFFFCCBC), Color(0xFF1A0A00), "\u2643", "Fall")

    fun current(): SeasonColors {
        val month = java.util.Calendar.getInstance().get(java.util.Calendar.MONTH) + 1
        return when (month) {
            12, 1, 2 -> WINTER
            3, 4, 5 -> SPRING
            6, 7, 8 -> SUMMER
            else -> FALL
        }
    }
}
