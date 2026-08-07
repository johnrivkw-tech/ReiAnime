package com.rei.app.util

import com.rei.app.data.local.*
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Backup & Restore utility for Rei tracking data.
 * Exports all tracking entries, episodes, and economy data as JSON.
 * Import reconstitutes the data into Room.
 */
@Singleton
class BackupUtil @Inject constructor(
    private val db: AppDatabase
) {
    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

    @Serializable
    data class ReiBackup(
        val version: Int = 2,
        val timestamp: Long = System.currentTimeMillis(),
        val tracking: List<TrackingBackupEntry>,
        val episodes: List<EpisodeBackupEntry>,
        val economy: List<EconomyBackupEntry>
    )

    @Serializable
    data class TrackingBackupEntry(
        val mediaId: Int, val title: String, val coverImage: String?, val bannerImage: String?,
        val status: String, val score: Float, val progress: Int, val totalEpisodes: Int?,
        val notes: String?, val updatedAt: Long, val idMal: Int?, val syncHash: Long
    )

    @Serializable
    data class EpisodeBackupEntry(
        val mediaId: Int, val episodeNumber: Int, val watched: Boolean,
        val watchedAt: Long?, val note: String?, val rewatchCount: Int
    )

    @Serializable
    data class EconomyBackupEntry(
        val key: String, val longValue: Long
    )

    /** Export all data as a JSON string */
    suspend fun export(): String {
        val tracking = db.trackingDao().getAll().first().map { e ->
            TrackingBackupEntry(e.mediaId, e.title, e.coverImage, e.bannerImage, e.status, e.score, e.progress, e.totalEpisodes, e.notes, e.updatedAt, e.idMal, e.syncHash)
        }
        val episodes = db.episodeDao().let { dao ->
            // Get all episodes by iterating tracked anime
            tracking.flatMap { t ->
                try { dao.getWatched(t.mediaId) } catch (_: Exception) { emptyList() }
            }.map { e ->
                EpisodeBackupEntry(e.mediaId, e.episodeNumber, e.watched, e.watchedAt, e.note, e.rewatchCount)
            }
        }
        val economy = listOf(
            EconomyBackupEntry("balance", db.economyDao().get("balance")?.longValue ?: 0L),
            EconomyBackupEntry("totalEarned", db.economyDao().totalEarned() ?: 0L),
            EconomyBackupEntry("totalSpent", db.economyDao().totalSpent() ?: 0L)
        )
        val backup = ReiBackup(tracking = tracking, episodes = episodes, economy = economy)
        return json.encodeToString(ReiBackup.serializer(), backup)
    }

    /** Import from a JSON string. Returns number of tracking entries imported. */
    suspend fun import(jsonString: String): Int {
        val backup = json.decodeFromString(ReiBackup.serializer(), jsonString)
        var count = 0
        backup.tracking.forEach { t ->
            db.trackingDao().upsert(TrackingEntity(
                mediaId = t.mediaId, title = t.title, coverImage = t.coverImage, bannerImage = t.bannerImage,
                status = t.status, score = t.score, progress = t.progress, totalEpisodes = t.totalEpisodes,
                notes = t.notes, updatedAt = t.updatedAt, idMal = t.idMal, syncHash = t.syncHash
            ))
            count++
        }
        backup.episodes.forEach { e ->
            db.episodeDao().upsert(EpisodeEntity(
                mediaId = e.mediaId, episodeNumber = e.episodeNumber, watched = e.watched,
                watchedAt = e.watchedAt, note = e.note, rewatchCount = e.rewatchCount
            ))
        }
        backup.economy.forEach { e ->
            db.economyDao().set(EconomyEntity(key = e.key, longValue = e.longValue))
        }
        return count
    }

    /** Quick stats about the backup without full import */
    fun preview(jsonString: String): ReiBackup? {
        return try { json.decodeFromString(ReiBackup.serializer(), jsonString) } catch (_: Exception) { null }
    }
}
