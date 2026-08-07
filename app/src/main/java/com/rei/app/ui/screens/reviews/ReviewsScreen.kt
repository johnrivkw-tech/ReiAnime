package com.rei.app.ui.screens.reviews

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.rei.app.ui.components.PremiumEmptyState
import com.rei.app.ui.components.PremiumShimmer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewsScreen(
    animeId: Int,
    animeTitle: String,
    onBack: () -> Unit,
    vm: ReviewsViewModel = hiltViewModel()
) {
    val reviews by vm.reviews.collectAsState()
    val isLoading by vm.isLoading.collectAsState()

    LaunchedEffect(animeId) { vm.loadReviews(animeId) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Reviews", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text(animeTitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    }
                },
                navigationIcon = { IconButton(onBack) { Icon(Icons.Filled.ArrowBack, "Back") } }
            )
        }
    ) { pv ->
        if (isLoading) {
            LazyColumn(Modifier.fillMaxSize().padding(pv)) {
                items(5) {
                    Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        PremiumShimmer(Modifier.size(40.dp), CircleShape)
                        Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            PremiumShimmer(Modifier.width(120.dp).height(14.dp), RoundedCornerShape(4.dp))
                            PremiumShimmer(Modifier.fillMaxWidth().height(60.dp), RoundedCornerShape(8.dp))
                        }
                    }
                }
            }
        } else if (reviews.isEmpty()) {
            Box(Modifier.fillMaxSize().padding(pv), contentAlignment = Alignment.Center) {
                PremiumEmptyState(
                    icon = Icons.Outlined.RateReview,
                    title = "No reviews yet",
                    subtitle = "Be the first to share your thoughts"
                )
            }
        } else {
            LazyColumn(
                Modifier.fillMaxSize().padding(pv),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(16.dp)
            ) {
                // Summary card
                item {
                    val avgScore = if (reviews.isNotEmpty()) reviews.map { it.score }.average() else 0.0
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                    ) {
                        Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${(avgScore / 10).toInt()}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("avg score", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("${reviews.size}", style = MaterialTheme.typography.displaySmall, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Text("reviews", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }

                items(reviews) { review ->
                    ReviewCard(review)
                }
            }
        }
    }
}

@Composable
private fun ReviewCard(review: ReviewItem) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f))
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // User + score row
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Avatar placeholder
                Box(
                    Modifier.size(36.dp).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        review.username.firstOrNull()?.uppercase() ?: "?",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(Modifier.weight(1f)) {
                    Text(review.username, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                    Text(review.date, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                // Score badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                ) {
                    Text(
                        "${review.score / 10}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            // Review text
            Text(
                review.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
            // Helpful row
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TextButton({}) {
                    Icon(Icons.Outlined.ThumbUp, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("${review.helpful}", style = MaterialTheme.typography.labelSmall)
                }
                TextButton({}) {
                    Icon(Icons.Outlined.ThumbDown, null, Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Report", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

// Domain model
data class ReviewItem(
    val id: Int = 0,
    val username: String = "",
    val score: Int = 0,
    val text: String = "",
    val date: String = "",
    val helpful: Int = 0,
    val avatar: String? = null
)
