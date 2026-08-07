package com.rei.app.ui.screens.onboarding

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

data class OnboardPage(
    val icon: String,
    val title: String,
    val subtitle: String,
    val desc: String,
    val gradient: List<Color>,
    val features: List<String>
)

@Composable
fun OnboardingScreen(onFinish: () -> Unit) {
    val pages = listOf(
        OnboardPage(
            icon = "\u25C8", title = "Rei \u96F6", subtitle = "Your Anime Universe",
            desc = "Track, discover, and customize your anime experience like never before.",
            gradient = listOf(Color(0xFF0A0A2E), Color(0xFF1A1A4E), Color(0xFF0D0D2A)),
            features = listOf("8 API Sources", "17+ Screens", "93 Shop Items")
        ),
        OnboardPage(
            icon = "\u25A0", title = "All Your Anime", subtitle = "One App, Every Source",
            desc = "AniList + MyAnimeList + Kitsu + Shikimori + LiveChart + Trace.moe + AnimeSchedule + Waifu.im",
            gradient = listOf(Color(0xFF0D081A), Color(0xFF161040), Color(0xFF0D081A)),
            features = listOf("Aggregated Search", "Franchise Trees", "Scene Recognition")
        ),
        OnboardPage(
            icon = "\u2726", title = "Deep Customization", subtitle = "Make It Yours",
            desc = "18 theme modes, 16 accent colors, 6 card styles, 80+ options — all persisted.",
            gradient = listOf(Color(0xFF1A0A14), Color(0xFF221028), Color(0xFF1A0A14)),
            features = listOf("18 Themes", "80+ Options", "Premium Shimmer")
        ),
        OnboardPage(
            icon = "\u25C6", title = "Rei Coins", subtitle = "Earn & Unlock",
            desc = "Track anime to earn coins. Spend them on themes, effects, and premium content.",
            gradient = listOf(Color(0xFF0A141A), Color(0xFF0F1E28), Color(0xFF0A141A)),
            features = listOf("17 Earn Actions", "93 Shop Items", "4 Rarity Tiers")
        ),
        OnboardPage(
            icon = "\u25B2", title = "Smart Features", subtitle = "Never Miss a Beat",
            desc = "Airing schedule, per-episode tracking, watch order trees, and real-time notifications.",
            gradient = listOf(Color(0xFF0E0E10), Color(0xFF18181B), Color(0xFF0E0E10)),
            features = listOf("Episode Grid", "Watch Order", "Daily Streaks")
        )
    )

    val pager = rememberPagerState(pageCount = { pages.size })
    val isLast = pager.currentPage == pages.size - 1
    val scope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        // Background gradient that changes per page
        val currentPage = pager.currentPage
        val bgGradient = pages[currentPage].gradient
        Box(Modifier.fillMaxSize().background(Brush.verticalGradient(bgGradient)))

        HorizontalPager(
            state = pager,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            val p = pages[page]
            val pageOffset = ((pager.currentPage - page).toFloat()).let { kotlin.math.abs(it) }
            val scale = 1f - (pageOffset * 0.1f)
            val alpha = 1f - (pageOffset * 0.5f)

            Column(
                Modifier.fillMaxSize().padding(32.dp).graphicsLayer {
                    scaleX = scale; scaleY = scale; this.alpha = alpha
                },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Icon with animated glow
                val infiniteTransition = rememberInfiniteTransition(label = "onboard_glow")
                val glowScale by infiniteTransition.animateFloat(
                    0.9f, 1.15f,
                    infiniteRepeatable(tween(2000), RepeatMode.Reverse),
                    label = "glow"
                )
                Box(Modifier.size(130.dp), contentAlignment = Alignment.Center) {
                    // Outer glow ring
                    Box(Modifier.size(130.dp * glowScale).clip(CircleShape).background(
                        Brush.radialGradient(
                            listOf(p.gradient.first().copy(alpha = 0.3f), Color.Transparent),
                            radius = 130.dp.value * glowScale
                        )
                    ))
                    // Inner circle
                    Box(Modifier.size(100.dp).clip(CircleShape).background(
                        Brush.radialGradient(
                            listOf(p.gradient.first(), p.gradient.last()),
                            radius = 100.dp.value
                        )
                    ), contentAlignment = Alignment.Center) {
                        Text(p.icon, fontSize = 40.sp, color = Color.White)
                    }
                }

                Spacer(Modifier.height(36.dp))
                Text(p.title, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Black, textAlign = TextAlign.Center, color = Color.White)
                Spacer(Modifier.height(4.dp))
                Text(p.subtitle, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center, color = p.gradient.first().copy(alpha = 0.8f).let { Color(0xFF3DB4F2) })
                Spacer(Modifier.height(14.dp))
                Text(p.desc, style = MaterialTheme.typography.bodyLarge, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, lineHeight = 24.sp)

                // Feature pills
                Spacer(Modifier.height(24.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    p.features.forEach { feature ->
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(0.5.dp, Color.White.copy(alpha = 0.15f))
                        ) {
                            Text(
                                feature,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White.copy(alpha = 0.85f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Bottom bar with progress
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 32.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Progress indicators
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                repeat(pages.size) { i ->
                    val selected = i == pager.currentPage
                    Box(
                        Modifier
                            .height(4.dp)
                            .width(if (selected) 28.dp else 12.dp)
                            .clip(RoundedCornerShape(2.dp))
                            .background(
                                if (selected) Color(0xFF3DB4F2)
                                else Color.White.copy(alpha = 0.2f)
                            )
                            .animateContentSize()
                    )
                }
            }

            // Primary button
            Button(
                onClick = {
                    if (isLast) onFinish()
                    else scope.launch { pager.animateScrollToPage(pager.currentPage + 1) }
                },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF3DB4F2),
                    contentColor = Color.White
                )
            ) {
                Text(
                    if (isLast) "Get Started" else "Continue",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge
                )
                Spacer(Modifier.width(8.dp))
                Icon(
                    if (isLast) Icons.Filled.RocketLaunch else Icons.Filled.ArrowForward,
                    null,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!isLast) {
                TextButton(onClick = onFinish) {
                    Text("Skip", color = Color.White.copy(alpha = 0.5f))
                }
            }
        }
    }
}
