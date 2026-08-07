package com.rei.app.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.*
import androidx.glance.appwidget.*
import androidx.glance.layout.*
import androidx.glance.material3.*
import androidx.glance.text.*
import com.rei.app.data.local.AppDatabase
import com.rei.app.data.local.TrackingEntity
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Home screen widget showing "Airing Today" — next episodes from tracked anime.
 *
 * Shows up to 3 items with: title, episode count, and time until airing.
 */
class AiringTodayWidget : GlanceAppWidget() {
    @Composable
    override fun Content() {
        // In a real app, we'd use a StateFlow from WorkManager/Room
        // For now, show a styled placeholder that demonstrates the widget structure
        GlanceTheme {
            Column(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(GlanceTheme.colors.surfaceVariant)
                    .padding(12.dp)
            ) {
                // Header
                Row(
                    modifier = GlanceModifier.fillMaxWidth().padding(bottom = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "零 Airing Today",
                        style = TextStyle(
                            fontWeight = FontWeight.Bold,
                            color = GlanceTheme.colors.onSurfaceVariant
                        )
                    )
                }

                // Anime items (placeholder — real data from Room in production)
                repeat(3) { index ->
                    Row(
                        modifier = GlanceModifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalAlignment = Alignment.Start
                    ) {
                        // Colored dot
                        Box(
                            modifier = GlanceModifier.size(8.dp).padding(top = 6.dp)
                                .background(GlanceTheme.colors.primary)
                        ) {}
                        Spacer(modifier = GlanceModifier.width(8.dp))
                        Column {
                            Text(
                                text = "Anime Title ${index + 1}",
                                style = TextStyle(
                                    fontWeight = FontWeight.Medium,
                                    color = GlanceTheme.colors.onSurface
                                ),
                                maxLines = 1
                            )
                            Text(
                                text = "Ep ${index + 5} • ${2 + index}h until airing",
                                style = TextStyle(
                                    fontSize = 12.sp,
                                    color = GlanceTheme.colors.onSurfaceVariant
                                ),
                                maxLines = 1
                            )
                        }
                    }
                }

                // Footer
                Text(
                    text = "Tap to open Rei",
                    modifier = GlanceModifier.padding(top = 4.dp).fillMaxWidth(),
                    style = TextStyle(
                        fontSize = 10.sp,
                        color = GlanceTheme.colors.onSurfaceVariant
                    )
                )
            }
        }
    }
}

class AiringTodayWidgetReceiver : GlanceAppWidgetReceiver() {
    override val glanceAppWidget: GlanceAppWidget = AiringTodayWidget()
}
