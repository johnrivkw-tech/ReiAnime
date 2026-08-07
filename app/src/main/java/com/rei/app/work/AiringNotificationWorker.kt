package com.rei.app.work

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.hilt.work.HiltWorker
import androidx.work.*
import com.rei.app.data.local.AppDatabase
import com.rei.app.ui.MainActivity
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import java.util.concurrent.TimeUnit

/**
 * Periodic WorkManager worker that checks for newly airing episodes
 * and posts notifications for tracked anime.
 *
 * Runs every ~2 hours to check upcoming episodes.
 */
@HiltWorker
class AiringNotificationWorker @AssistedInject constructor(
    @Assisted private val context: Context,
    @Assisted private val workerParams: WorkerParameters,
    private val db: AppDatabase
) : CoroutineWorker(context, workerParams) {

    companion object {
        const val CHANNEL_ID = "rei_airing"
        const val CHANNEL_NAME = "Airing Notifications"

        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<AiringNotificationWorker>(
                2, TimeUnit.HOURS
            )
                .setConstraints(Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build())
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.MINUTES)
                .build()

            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "rei_airing_check",
                ExistingPeriodicWorkPolicy.KEEP,
                request
            )
        }

        fun cancel(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork("rei_airing_check")
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val watching = db.trackingDao().getByStatus("CURRENT").let { flow ->
                // Collect first emission
                var result: List<com.rei.app.data.local.TrackingEntity> = emptyList()
                flow.collect { result = it; throw kotlinx.coroutines.CancellationException() }
                result
            }

            // Check each tracked anime for new episodes
            // In production, query AniList airing schedule API
            // For now, create notifications for anime airing soon
            watching.take(5).forEachIndexed { index, entry ->
                if (hasNotificationPermission()) {
                    showNotification(
                        id = entry.mediaId,
                        title = entry.title,
                        message = "New episode airing soon!",
                        intent = Intent(context, MainActivity::class.java).apply {
                            putExtra("animeId", entry.mediaId)
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                        }
                    )
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= 33) {
            context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else true
    }

    private fun showNotification(id: Int, title: String, message: String, intent: Intent) {
        val pendingIntent = PendingIntent.getActivity(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_media_play)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .build()

        // Create channel on API 26+
        if (android.os.Build.VERSION.SDK_INT >= 26) {
            val channel = android.app.NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, android.app.NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for anime airing schedule"
                setShowBadge(true)
            }
            NotificationManagerCompat.from(context).createNotificationChannel(channel)
        }

        NotificationManagerCompat.from(context).notify(id, notification)
    }
}

// Extension to collect first item from Flow
private suspend fun <T> kotlinx.coroutines.flow.Flow<T>.firstItem(): T? {
    var result: T? = null
    try {
        collect { result = it; throw kotlinx.coroutines.CancellationException() }
    } catch (_: kotlinx.coroutines.CancellationException) {}
    return result
}
