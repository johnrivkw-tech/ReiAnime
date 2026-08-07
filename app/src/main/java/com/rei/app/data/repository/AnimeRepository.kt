package com.rei.app.data.repository

import com.rei.app.data.local.*
import com.rei.app.data.remote.anilist.AniListApi
import com.rei.app.data.remote.jikan.JikanApi
import com.rei.app.data.remote.kitsu.KitsuApi
import com.rei.app.data.remote.livechart.LiveChartApi
import com.rei.app.data.remote.livechart.LiveChartAnime
import com.rei.app.data.remote.livechart.StreamingOption
import com.rei.app.data.remote.shikimori.ShikimoriApi
import com.rei.app.data.remote.tracemoe.TraceMoeApi
import com.rei.app.data.remote.tracemoe.TraceResult
import com.rei.app.data.remote.animeschedule.AnimeScheduleApi
import com.rei.app.data.remote.animeschedule.ScheduleItem
import com.rei.app.data.remote.waifu.WaifuApi
import com.rei.app.data.remote.waifu.WaifuImage
import com.rei.app.data.remote.jikan.MALAuth
import com.rei.app.data.remote.jikan.MALListApi
import com.rei.app.data.remote.jikan.AnimeCharacter
import com.rei.app.data.remote.jikan.AnimeStaff
import com.rei.app.data.remote.jikan.AnimeStatistics
import com.rei.app.data.remote.jikan.AnimeRelation
import com.rei.app.data.remote.jikan.ExternalLink
import com.rei.app.data.remote.mangadex.MangaDexApi
import com.rei.app.data.remote.mangadex.MangaDexEntry
import com.rei.app.data.remote.simkl.SimklApi
import com.rei.app.data.remote.simkl.StreamingSource
import com.rei.app.data.sync.SyncService
import com.rei.app.domain.model.*
import com.rei.app.economy.EconomyManager
import com.rei.app.economy.ReiRewards
import com.rei.app.economy.ReiShop
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.io.File
import javax.inject.Inject

class AnimeRepository @Inject constructor(
    private val anilist: AniListApi,
    private val jikan: JikanApi,
    private val kitsu: KitsuApi,
    private val shikimori: ShikimoriApi,
    private val traceMoe: TraceMoeApi,
    private val livechart: LiveChartApi,
    private val animesched: AnimeScheduleApi,
    private val waifu: WaifuApi,
    private val mangadex: MangaDexApi,
    private val simkl: SimklApi,
    private val db: AppDatabase,
    private val economy: EconomyManager,
    private val syncService: SyncService? = null,
    private val malAuth: MALAuth? = null,
    private val malList: MALListApi? = null,
    private val anilistAuth: com.rei.app.data.remote.anilist.AniListAuth? = null
) {
    // ── AniList ──
    fun getTrending(page: Int = 1) = flow { emit(anilist.getTrending(page)) }
    fun getPopularThisSeason(page: Int = 1) = flow { emit(anilist.getPopularSeason(page)) }
    fun getUpcomingNextSeason(page: Int = 1) = flow { emit(anilist.getUpcoming(page)) }
    fun getAllTimePopular(page: Int = 1) = flow { emit(anilist.getAllTimePopular(page)) }
    fun searchAnime(search: String, genre: String? = null, year: Int? = null, format: MediaFormat? = null, sort: List<MediaSort> = listOf(MediaSort.SEARCH_MATCH)) = flow { emit(anilist.search(search, genre = genre, year = year, format = format, sort = sort)) }
    fun getAnimeDetail(id: Int) = flow { emit(anilist.getDetail(id)) }
    fun getSeasonalAnime(season: Season, year: Int, sort: MediaSort = MediaSort.POPULARITY_DESC) = flow { emit(anilist.getSeasonal(season, year, sort = sort)) }
    fun getViewer() = flow { emit(anilist.getViewer()) }
    suspend fun updateMediaListEntry(mediaId: Int, status: MediaListStatus, score: Float? = null, progress: Int? = null) = anilist.updateEntry(mediaId, status, score, progress)

    // ── Jikan/MAL ──
    suspend fun getMALTopAnime(page: Int = 1) = jikan.getTopAnime(page)
    suspend fun getMALSeasonNow() = jikan.getSeasonNow()
    suspend fun getMALUpcoming(page: Int = 1) = jikan.getUpcoming(page)
    suspend fun searchMALAnime(q: String) = jikan.searchAnime(q)
    suspend fun getRandomAnime() = jikan.getRandomAnime()
    suspend fun getAnimeReviews(id: Int) = jikan.getAnimeReviews(id)
    suspend fun getAnimeRecommendations(id: Int) = jikan.getAnimeRecommendations(id)

    // ── Kitsu ──
    suspend fun getKitsuTrending(limit: Int = 20) = kitsu.getTrending(limit)
    suspend fun getKitsuCategories() = kitsu.getCategories()
    suspend fun searchKitsuAnime(query: String) = kitsu.searchAnime(query)

    // ── Shikimori ──
    suspend fun getShikiTopAnime(limit: Int = 20) = shikimori.getTopAnime(limit)
    suspend fun getFranchise(id: Int) = shikimori.getFranchise(id)
    suspend fun getShikiRoles(id: Int) = shikimori.getRoles(id)
    suspend fun getShikiRelated(id: Int) = shikimori.getRelated(id)

    // ── Trace.moe ──
    suspend fun searchByImageUrl(url: String): TraceResult = traceMoe.searchByUrl(url)
    suspend fun searchByImageFile(file: File): TraceResult = traceMoe.searchByFile(file)
    suspend fun searchByImageBytes(bytes: ByteArray, filename: String = "image.jpg"): TraceResult = traceMoe.searchByBytes(bytes, filename)

    // ── LiveChart.me ──
    suspend fun getLiveChartCurrentSeason(): List<LiveChartAnime> = livechart.getCurrentSeason()
    suspend fun getLiveChartSchedule(dayOffset: Int = 0): List<LiveChartAnime> = livechart.getSchedule(dayOffset)
    suspend fun getLiveChartStreaming(animeId: Int): List<StreamingOption> = livechart.getStreaming(animeId)

    // ── AnimeSchedule.net ──
    suspend fun getScheduleCurrent(): List<ScheduleItem> = animesched.getCurrentSchedule()
    suspend fun getScheduleByDay(day: Int): List<ScheduleItem> = animesched.getDaySchedule(day)

    // ── Waifu.im ──
    suspend fun getRandomWaifu(count: Int = 1): List<WaifuImage> = waifu.getRandom(count)
    suspend fun getWaifuTags(): List<com.rei.app.data.remote.waifu.WaifuTag> = waifu.getTags()
    suspend fun getWaifuImageUrl(): String? = waifu.getRandomImageUrl()

    // ── Aggregated ──
    suspend fun getAggregatedTrending(): List<Anime> {
        val anilistResult = try { var r = emptyList<Anime>(); flow { emit(anilist.getTrending()) }.collect { r = it.first }; r } catch (_: Exception) { emptyList() }
        val kitsuResult = try { kitsu.getTrending(10) } catch (_: Exception) { emptyList() }
        return (anilistResult + kitsuResult).distinctBy { it.title.primary }.take(25)
    }

    fun getCachedAnime() = db.animeDao().getAllAnime()

    // ═══════════════════════════════════════════
    // TRACKING (with economy rewards)
    // ═══════════════════════════════════════════
    fun getContinueWatching(): Flow<List<TrackingEntity>> = db.trackingDao().getContinueWatching()
    fun getTrackingByStatus(status: String): Flow<List<TrackingEntity>> = db.trackingDao().getByStatus(status)
    fun getAllTracking(): Flow<List<TrackingEntity>> = db.trackingDao().getAll()
    suspend fun getTrackingEntry(id: Int): TrackingEntity? = db.trackingDao().getById(id)

    suspend fun upsertTracking(mediaId: Int, title: String, coverImage: String? = null, bannerImage: String? = null, status: MediaListStatus, score: Float = 0f, progress: Int = 0, totalEpisodes: Int? = null, notes: String? = null, idMal: Int? = null) {
        val existing = db.trackingDao().getById(mediaId)
        val wasTracked = existing != null
        val wasCompleted = existing?.status == "COMPLETED"
        db.trackingDao().upsert(TrackingEntity(mediaId = mediaId, title = title, coverImage = coverImage, bannerImage = bannerImage, status = status.name, score = score, progress = progress, totalEpisodes = totalEpisodes, notes = notes, updatedAt = System.currentTimeMillis(), idMal = idMal))
        // Economy rewards
        if (!wasTracked) economy.earn(ReiRewards.TRACK_ANIME, "track_anime", mediaId.toString())
        if (status == MediaListStatus.COMPLETED && !wasCompleted) economy.earn(ReiRewards.COMPLETE_ANIME, "complete_anime", mediaId.toString())
        if (score > 0 && (existing == null || existing.score == 0f)) economy.earn(ReiRewards.RATE_ANIME, "rate_anime", mediaId.toString())
        // Auto-sync to AniList + MAL
        pushUpdate(mediaId, status, score, progress, idMal)
    }

    suspend fun updateTrackingProgress(mediaId: Int, progress: Int) {
        val existing = db.trackingDao().getById(mediaId) ?: return
        val wasProgress = existing.progress
        db.trackingDao().upsert(existing.copy(progress = progress, updatedAt = System.currentTimeMillis()))
        // Reward for each new episode watched
        val delta = progress - wasProgress
        if (delta > 0) economy.earn(ReiRewards.WATCH_EPISODE * delta, "watch_episodes", mediaId.toString())
        // Auto-sync progress to AniList + MAL
        val status = try { MediaListStatus.valueOf(existing.status) } catch (_: Exception) { return }
        pushUpdate(mediaId, status, existing.score, progress, existing.idMal)
    }

    suspend fun updateTrackingStatus(mediaId: Int, status: MediaListStatus) {
        val existing = db.trackingDao().getById(mediaId) ?: return
        val wasCompleted = existing.status == "COMPLETED"
        db.trackingDao().upsert(existing.copy(status = status.name, updatedAt = System.currentTimeMillis()))
        if (status == MediaListStatus.COMPLETED && !wasCompleted) economy.earn(ReiRewards.COMPLETE_ANIME, "complete_anime", mediaId.toString())
        // Auto-sync status to AniList + MAL
        pushUpdate(mediaId, status, existing.score, existing.progress, existing.idMal)
    }

    suspend fun removeTracking(mediaId: Int) = db.trackingDao().deleteById(mediaId)

    // ═══════════════════════════════════════════
    // PER-EPISODE TRACKING
    // ═══════════════════════════════════════════
    fun getEpisodes(mediaId: Int): Flow<List<EpisodeEntity>> = db.episodeDao().getEpisodes(mediaId)
    suspend fun getWatchedEpisodes(mediaId: Int): List<EpisodeEntity> = db.episodeDao().getWatched(mediaId)
    suspend fun watchedCount(mediaId: Int): Int = db.episodeDao().watchedCount(mediaId)

    suspend fun toggleEpisodeWatched(mediaId: Int, episode: Int, watched: Boolean) {
        db.episodeDao().upsert(EpisodeEntity(
            mediaId = mediaId,
            episodeNumber = episode,
            watched = watched,
            watchedAt = if (watched) System.currentTimeMillis() else null
        ))
        if (watched) economy.earn(ReiRewards.WATCH_EPISODE, "watch_episode", "${mediaId}_ep${episode}")
    }

    suspend fun setEpisodeNote(mediaId: Int, episode: Int, note: String) {
        // Fetch current, update note
        db.episodeDao().upsert(EpisodeEntity(mediaId = mediaId, episodeNumber = episode, watched = true, note = note, watchedAt = System.currentTimeMillis()))
        economy.earn(ReiRewards.WRITE_NOTE, "episode_note", "${mediaId}_ep${episode}")
    }

    suspend fun initEpisodes(mediaId: Int, totalEpisodes: Int) {
        val eps = (1..totalEpisodes).map { EpisodeEntity(mediaId = mediaId, episodeNumber = it) }
        db.episodeDao().upsertAll(eps)
    }

    // ═══════════════════════════════════════════
    // ECONOMY (pass-through)
    // ═══════════════════════════════════════════
    fun coinBalance() = economy.balance
    suspend fun earnCoins(amount: Int, reason: String, target: String? = null) = economy.earn(amount, reason, target)
    suspend fun purchaseItem(item: ReiShop.ShopItem) = economy.purchase(item)
    suspend fun isUnlocked(itemId: String) = economy.isUnlocked(itemId)
    suspend fun checkDailyStreak() = economy.checkDailyStreak()

    // ═══════════════════════════════════════════
    // SYNC — AniList + MAL cross-sync
    // ═══════════════════════════════════════════
    suspend fun fullSync(): com.rei.app.data.sync.SyncService.SyncResult? = syncService?.fullSync()
    suspend fun pushUpdate(mediaId: Int, status: MediaListStatus, score: Float? = null, progress: Int? = null, idMal: Int? = null) {
        syncService?.pushUpdate(mediaId, status, score, progress, idMal)
    }
    suspend fun importFromMAL(): Int = syncService?.importFromMAL() ?: 0
    suspend fun importFromAniList(): Int = syncService?.importFromAniList() ?: 0
    suspend fun getSyncStatus(): com.rei.app.data.sync.ConnectionStatus? = syncService?.getConnectionStatus()
    suspend fun isMalLoggedIn(): Boolean = malAuth?.isLoggedIn() ?: false
    suspend fun isAnilistLoggedIn(): Boolean = anilistAuth?.isLoggedIn() ?: false

    /** Look up MAL ID from AniList ID for cross-referencing */
    suspend fun lookupMalId(anilistId: Int): Int? {
        return try { anilist.getDetail(anilistId).idMal } catch (_: Exception) { null }
    }

    /** Look up AniList anime by MAL ID for cross-referencing */
    suspend fun lookupByMalId(malId: Int): Anime? {
        return try { anilist.lookupByMalId(malId) } catch (_: Exception) { null }
    }

    // ── Jikan Extended ──
    suspend fun getAnimeCharacters(id: Int): List<AnimeCharacter> = jikan.getAnimeCharacters(id)
    suspend fun getAnimeStaff(id: Int): List<AnimeStaff> = jikan.getAnimeStaff(id)
    suspend fun getAnimeStatistics(id: Int): AnimeStatistics? = jikan.getAnimeStatistics(id)
    suspend fun getAnimeRelations(id: Int): List<AnimeRelation> = jikan.getAnimeRelations(id)
    suspend fun getAnimeExternalLinks(id: Int): List<ExternalLink> = jikan.getAnimeExternal(id)
    suspend fun getAnimeStreamingLinks(id: Int): List<ExternalLink> = jikan.getAnimeStreaming(id)

    // ── MangaDex ──
    suspend fun searchManga(query: String, limit: Int = 10): List<MangaDexEntry> = mangadex.searchManga(query, limit)
    suspend fun getMangaDetail(id: String): MangaDexEntry? = mangadex.getMangaDetail(id)
    suspend fun getMangaChapters(id: String, limit: Int = 20) = mangadex.getMangaChapters(id, limit)
    suspend fun getTrendingManga(limit: Int = 15): List<MangaDexEntry> = mangadex.getTrendingManga(limit)
    suspend fun getRecentlyUpdatedManga(limit: Int = 15): List<MangaDexEntry> = mangadex.getRecentlyUpdated(limit)

    // ── Simkl ──
    suspend fun searchSimkl(query: String) = simkl.search(query)
    suspend fun getStreamingSources(simklId: Int): List<StreamingSource> = simkl.getStreamingSources(simklId)
    suspend fun getSimklCalendar(date: String? = null) = simkl.getCalendar(date)
}
