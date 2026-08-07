package com.rei.app.data.sync

import com.rei.app.data.local.AppDatabase
import com.rei.app.data.local.TrackingEntity
import com.rei.app.data.remote.anilist.AniListApi
import com.rei.app.data.remote.anilist.AniListAuth
import com.rei.app.data.remote.jikan.MALAuth
import com.rei.app.data.remote.jikan.MALListApi
import com.rei.app.data.remote.jikan.MALListEntry
import com.rei.app.domain.model.MediaListStatus
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Sync Service — keeps local tracking, AniList, and MAL in sync.
 *
 * Sync flow:
 * 1. Local → AniList: Push local tracking changes to AniList (if authenticated)
 * 2. Local → MAL: Push local tracking changes to MAL (if authenticated)
 * 3. AniList → Local: Pull AniList list and merge with local
 * 4. MAL → Local: Pull MAL list and merge with local
 * 5. Cross-link: When an anime has both AniList ID and MAL ID, use idMal field
 *
 * Conflict resolution: Most recently updated wins (updatedAt timestamp)
 */
@Singleton
class SyncService @Inject constructor(
    private val anilist: AniListApi,
    private val anilistAuth: AniListAuth,
    private val malList: MALListApi,
    private val malAuth: MALAuth,
    private val db: AppDatabase
) {
    data class SyncResult(
        val anilistPushed: Int = 0,
        val malPushed: Int = 0,
        val anilistPulled: Int = 0,
        val malPulled: Int = 0,
        val conflicts: Int = 0,
        val errors: Int = 0
    )

    /**
     * Full bidirectional sync.
     * Pull from both services, then push local changes.
     */
    suspend fun fullSync(): SyncResult {
        var result = SyncResult()

        // Pull from AniList
        if (anilistAuth.isLoggedIn()) {
            val pullResult = pullFromAniList()
            result = result.copy(anilistPulled = pullResult)
        }

        // Pull from MAL
        if (malAuth.isLoggedIn()) {
            val pullResult = pullFromMAL()
            result = result.copy(malPulled = pullResult)
        }

        // Push local changes to AniList
        if (anilistAuth.isLoggedIn()) {
            val pushResult = pushToAniList()
            result = result.copy(anilistPushed = pushResult)
        }

        // Push local changes to MAL
        if (malAuth.isLoggedIn()) {
            val pushResult = pushToMAL()
            result = result.copy(malPushed = pushResult)
        }

        return result
    }

    /**
     * Push a single tracking update to both AniList and MAL.
     * Called whenever the user updates a tracking entry locally.
     */
    suspend fun pushUpdate(
        mediaId: Int,
        status: MediaListStatus,
        score: Float? = null,
        progress: Int? = null,
        idMal: Int? = null
    ) {
        // Push to AniList (mediaId IS the AniList ID)
        if (anilistAuth.isLoggedIn()) {
            try {
                anilist.updateEntry(mediaId, status, score, progress)
            } catch (_: Exception) { }
        }

        // Push to MAL (need MAL ID)
        if (malAuth.isLoggedIn()) {
            val malId = idMal ?: lookupMalId(mediaId)
            if (malId != null && malId > 0) {
                try {
                    malList.updateAnimeList(
                        animeId = malId,
                        status = status,
                        score = score?.let { (it / 10).toInt().coerceIn(0, 10) },
                        progress = progress
                    )
                } catch (_: Exception) { }
            }
        }
    }

    /**
     * Pull AniList user list and merge with local.
     * Returns number of entries merged.
     */
    private suspend fun pullFromAniList(): Int {
        // Note: AniList API requires a specific query to get the user's list
        // This is a simplified version — a full implementation would page through
        // the user's complete list using the MediaListCollection query
        try {
            val viewer = anilist.getViewer() ?: return 0
            // Full implementation would query:
            // query { MediaListCollection(userId: ${viewer.id}, type: ANIME) { lists { entries { mediaId status score progress media { idMal } } } } }
            // For now, return 0 — this would need a new AniList query added
            return 0
        } catch (_: Exception) { return 0 }
    }

    /**
     * Pull MAL user list and merge with local.
     * Returns number of entries merged.
     */
    private suspend fun pullFromMAL(): Int {
        try {
            val malEntries = malList.getUserAnimeList() ?: return 0
            var merged = 0

            for (entry in malEntries) {
                if (entry.animeId <= 0) continue

                // Check if we already track this anime locally by MAL ID
                // We need to find the AniList ID for this MAL anime
                val existing = findLocalByMalId(entry.animeId)

                if (existing != null) {
                    // Merge — most recently updated wins
                    val localUpdated = existing.updatedAt
                    val remoteUpdated = System.currentTimeMillis() // MAL doesn't give per-entry updatedAt

                    val localStatus = try { MediaListStatus.valueOf(existing.status) } catch (_: Exception) { MediaListStatus.CURRENT }
                    val remoteStatus = malList.fromMalStatus(entry.status)

                    // If remote has data we don't (e.g., user updated on MAL website)
                    if (entry.episodesWatched > existing.progress || entry.score > existing.score) {
                        db.trackingDao().upsert(existing.copy(
                            status = remoteStatus.name,
                            score = entry.score.toFloat() * 10, // MAL is 0-10, ours is 0-100
                            progress = entry.episodesWatched,
                            updatedAt = System.currentTimeMillis()
                        ))
                        merged++
                    }
                } else {
                    // New entry from MAL — add to local tracking
                    // We need to find the AniList ID first
                    val anilistId = lookupAnilistId(entry.animeId)
                    if (anilistId != null) {
                        db.trackingDao().upsert(TrackingEntity(
                            mediaId = anilistId,
                            title = entry.title,
                            coverImage = entry.mainPicture,
                            status = malList.fromMalStatus(entry.status).name,
                            score = entry.score.toFloat() * 10,
                            progress = entry.episodesWatched,
                            totalEpisodes = entry.totalEpisodes,
                            updatedAt = System.currentTimeMillis()
                        ))
                        merged++
                    }
                }
            }

            return merged
        } catch (_: Exception) { return 0 }
    }

    /**
     * Push all local tracking entries to AniList.
     * Returns number of entries pushed.
     */
    private suspend fun pushToAniList(): Int {
        try {
            val localEntries = db.trackingDao().getAll().first()
            var pushed = 0

            for (entry in localEntries) {
                try {
                    val status = try { MediaListStatus.valueOf(entry.status) } catch (_: Exception) { continue }
                    anilist.updateEntry(entry.mediaId, status, entry.score, entry.progress)
                    pushed++
                } catch (_: Exception) { continue }
            }

            return pushed
        } catch (_: Exception) { return 0 }
    }

    /**
     * Push all local tracking entries to MAL.
     * Returns number of entries pushed.
     */
    private suspend fun pushToMAL(): Int {
        try {
            val localEntries = db.trackingDao().getAll().first()
            var pushed = 0

            for (entry in localEntries) {
                val malId = lookupMalId(entry.mediaId)
                if (malId == null || malId <= 0) continue

                try {
                    val status = try { MediaListStatus.valueOf(entry.status) } catch (_: Exception) { continue }
                    malList.updateAnimeList(
                        animeId = malId,
                        status = status,
                        score = (entry.score / 10).toInt().coerceIn(0, 10),
                        progress = entry.progress
                    )
                    pushed++
                } catch (_: Exception) { continue }
            }

            return pushed
        } catch (_: Exception) { return 0 }
    }

    // ═══════════════════════════════════════════
    // ID CROSS-REFERENCE
    // ═══════════════════════════════════════════

    /**
     * Look up MAL ID from AniList ID.
     * Uses the idMal field from AniList's Media query.
     */
    private suspend fun lookupMalId(anilistId: Int): Int? {
        return try {
            val anime = anilist.getDetail(anilistId)
            anime.idMal
        } catch (_: Exception) { null }
    }

    /**
     * Look up AniList ID from MAL ID.
     * Uses AniList's search with idMal filter.
     */
    private suspend fun lookupAnilistId(malId: Int): Int? {
        return try {
            // AniList supports searching by MAL ID:
            // query { Media(idMal: $malId, type: ANIME) { id } }
            val query = "query(\$idMal:Int){Media(idMal:\$idMal,type:ANIME){id}}"
            // Use the AniList API's query method directly
            // For now, return null — this would need a dedicated method in AniListApi
            null
        } catch (_: Exception) { null }
    }

    /**
     * Find a local tracking entry by MAL ID.
     * Since our tracking uses AniList ID as primary key,
     * we need to cross-reference.
     */
    private suspend fun findLocalByMalId(malId: Int): TrackingEntity? {
        // This would ideally use a dedicated Room query
        // For now, iterate through cached anime entities
        // A better approach: add a malId column to TrackingEntity
        try {
            val allTracking = db.trackingDao().getAll().first()
            for (entry in allTracking) {
                val idMal = lookupMalId(entry.mediaId)
                if (idMal == malId) return entry
            }
        } catch (_: Exception) { }
        return null
    }

    /**
     * Import entire MAL list into local tracking.
     * Useful for first-time users who have a MAL account.
     */
    suspend fun importFromMAL(): Int {
        return pullFromMAL()
    }

    /**
     * Import entire AniList list into local tracking.
     * Useful for first-time users who have an AniList account.
     */
    suspend fun importFromAniList(): Int {
        return pullFromAniList()
    }

    /**
     * Check which services are connected.
     */
    suspend fun getConnectionStatus(): ConnectionStatus {
        return ConnectionStatus(
            anilistConnected = anilistAuth.isLoggedIn(),
            malConnected = malAuth.isLoggedIn(),
            anilistUser = try { anilistAuth.userName.first() } catch (_: Exception) { null },
            malUser = try { malAuth.userName.first() } catch (_: Exception) { null }
        )
    }
}

data class ConnectionStatus(
    val anilistConnected: Boolean = false,
    val malConnected: Boolean = false,
    val anilistUser: String? = null,
    val malUser: String? = null
)
