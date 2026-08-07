package com.rei.app.ui.screens.profile

import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.geometry.Offset
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.rei.app.domain.model.User

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(onSettings: () -> Unit, onAnimeClick: (Int) -> Unit, onShop: () -> Unit, onCollections: () -> Unit = {}, onCompare: () -> Unit = {}, vm: ProfileViewModel = hiltViewModel()) {
    val state by vm.state.collectAsState()
    val coinBalance by vm.coinBalance.collectAsState()
    val streak by vm.streak.collectAsState()

    Scaffold(topBar = { TopAppBar(title = { Text("Profile", fontWeight = FontWeight.Bold) }, actions = {
        Surface(shape = RoundedCornerShape(16.dp), color = Color(0xFF7C4DFF).copy(alpha = 0.15f), onClick = onShop) {
            Row(Modifier.padding(horizontal = 10.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("\u25C8", fontSize = 14.sp, color = Color(0xFF7C4DFF))
                Text("$coinBalance", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF))
            }
        }
        Spacer(Modifier.width(4.dp))
        IconButton(onSettings) { Icon(Icons.Filled.Settings, "Settings") }
    }) }) { pv ->
        LazyColumn(Modifier.fillMaxSize().padding(pv), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            when (state) {
                is ProfileState.Success -> { val u = (state as ProfileState.Success).user
                    if (u != null) {
                        // Banner
                        item { Box(Modifier.fillMaxWidth().height(200.dp)) { u.bannerImage?.let { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(), null, modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop, alpha = 0.6f) }; Box(Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.background)))) } }

                        // Avatar + name + bio
                        item {
                            Column(Modifier.padding(horizontal = 20.dp).offset(y = (-40).dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Box {
                                    u.avatar?.large?.let { AsyncImage(model = ImageRequest.Builder(LocalContext.current).data(it).crossfade(true).build(), "Avatar", modifier = Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.surfaceVariant), contentScale = ContentScale.Crop) }
                                    // Online indicator
                                    Box(Modifier.size(18.dp).clip(CircleShape).background(Color(0xFF4CAF50)).align(Alignment.BottomEnd).border(2.dp, MaterialTheme.colorScheme.background, CircleShape))
                                }
                                Spacer(Modifier.height(8.dp))
                                Text(u.name, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                                u.about?.let { Text(it.take(120), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2) }
                            }
                        }

                        // Stats card — premium glassmorphic
                        item {
                            Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f), shape = RoundedCornerShape(20.dp), border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.12f))) {
                                Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) { Icon(Icons.Filled.BarChart, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.primary); Text("Statistics", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) }
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        ProfileStat("\u25B6", u.statistics?.anime?.count?.toString() ?: "0", "Anime", Color(0xFF3DB4F2))
                                        ProfileStat("\u2713", "${(u.statistics?.anime?.minutesWatched ?: 0) / 60}h", "Watched", Color(0xFF4CAF50))
                                        ProfileStat("\u2605", u.statistics?.anime?.meanScore?.let { "${it.toInt()}" } ?: "0", "Mean", Color(0xFFFFC107))
                                        ProfileStat("\u25C8", u.statistics?.manga?.count?.toString() ?: "0", "Manga", Color(0xFF7C4DFF))
                                    }
                                }
                            }
                        }

                        // Coin + Streak premium card
                        item {
                            Surface(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), shape = RoundedCornerShape(20.dp), color = Color(0xFF7C4DFF).copy(alpha = 0.06f), border = BorderStroke(1.dp, Color(0xFF7C4DFF).copy(alpha = 0.15f))) {
                                Row(Modifier.padding(18.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly, verticalAlignment = Alignment.CenterVertically) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) { Text("\u25C8", fontSize = 24.sp, color = Color(0xFF7C4DFF)); Text("$coinBalance", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF7C4DFF)); Text("Coins", style = MaterialTheme.typography.labelSmall) }
                                    VerticalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f), modifier = Modifier.height(40.dp).width(1.dp))
                                    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) { Text("\u25B2", fontSize = 24.sp, color = Color(0xFFFF6D00)); Text("${streak}d", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFFFF6D00)); Text("Streak", style = MaterialTheme.typography.labelSmall) }
                                }
                            }
                        }

                        // Quick Actions
                        item { Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileAction("Shop", Icons.Filled.ShoppingBag, Color(0xFF7C4DFF), Modifier.weight(1f)) { onShop() }
                            ProfileAction("Collections", Icons.Filled.Folder, Color(0xFFFF4081), Modifier.weight(1f)) { onCollections() }
                            ProfileAction("Compare", Icons.Filled.Compare, Color(0xFFFFC107), Modifier.weight(1f)) { onCompare() }
                        }}
                        item { Row(Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            ProfileAction("Favorites", Icons.Filled.Favorite, Color(0xFFE91E63), Modifier.weight(1f)) {}
                            ProfileAction("Reviews", Icons.Filled.RateReview, Color(0xFF2196F3), Modifier.weight(1f)) {}
                            ProfileAction("Settings", Icons.Filled.Settings, Color(0xFFFF5722), Modifier.weight(1f)) { onSettings() }
                        }}
                    } else item { NotLoggedIn(onSettings, onShop) }
                }
                else -> item { NotLoggedIn(onSettings, onShop) }
            }
            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun ProfileStat(icon: String, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(2.dp)) {
        Text(icon, fontSize = 16.sp, color = color)
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun VerticalDivider(modifier: Modifier = Modifier, color: Color = Color.Gray) {
    Box(modifier.width(1.dp).fillMaxHeight().background(color))
}

@Composable
private fun ProfileAction(label: String, icon: ImageVector, color: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp), color = color.copy(alpha = 0.08f), border = BorderStroke(0.5.dp, color.copy(alpha = 0.2f))) {
        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = color)
        }
    }
}

@Composable
private fun NotLoggedIn(onSettings: () -> Unit, onShop: () -> Unit) = Column(Modifier.fillMaxWidth().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
    Box(Modifier.size(80.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)), contentAlignment = Alignment.Center) { Icon(Icons.Outlined.PersonOutline, null, Modifier.size(40.dp), MaterialTheme.colorScheme.primary) }
    Text("Sign in to AniList", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
    Text("Track your anime, sync lists, and discover your next favorite.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) { Button(onSettings, shape = RoundedCornerShape(12.dp)) { Icon(Icons.Filled.Login, null); Spacer(Modifier.width(8.dp)); Text("Connect") }; OutlinedButton(onShop, shape = RoundedCornerShape(12.dp)) { Text("\u25C8"); Spacer(Modifier.width(4.dp)); Text("Shop") } }
}
