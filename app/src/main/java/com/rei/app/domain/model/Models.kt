package com.rei.app.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Anime(
    val id: Int,
    val idMal: Int? = null,
    val title: AnimeTitle,
    val type: MediaType = MediaType.ANIME,
    val format: MediaFormat? = null,
    val status: MediaStatus? = null,
    val description: String? = null,
    val startDate: FuzzyDate? = null,
    val endDate: FuzzyDate? = null,
    val season: Season? = null,
    val seasonYear: Int? = null,
    val episodes: Int? = null,
    val duration: Int? = null,
    val chapters: Int? = null,
    val volumes: Int? = null,
    val coverImage: CoverImage,
    val bannerImage: String? = null,
    val genres: List<String> = emptyList(),
    val tags: List<MediaTag> = emptyList(),
    val studios: List<Studio> = emptyList(),
    val isFavourite: Boolean = false,
    val averageScore: Int? = null,
    val meanScore: Int? = null,
    val popularity: Int? = null,
    val trending: Int? = null,
    val favourites: Int? = null,
    val siteUrl: String? = null,
    val trailer: MediaTrailer? = null,
    val nextAiringEpisode: AiringSchedule? = null,
    val mediaListEntry: MediaList? = null,
    val recommendations: List<Anime> = emptyList()
)

@Serializable
data class AnimeTitle(
    val romaji: String? = null,
    val english: String? = null,
    val native: String? = null,
    val userPreferred: String? = null
) {
    val primary: String get() = userPreferred ?: english ?: romaji ?: native ?: "Unknown"
}

@Serializable
data class CoverImage(
    val extraLarge: String? = null,
    val large: String? = null,
    val medium: String? = null,
    val color: String? = null
) {
    val best: String? get() = extraLarge ?: large ?: medium
}

@Serializable
data class FuzzyDate(val year: Int? = null, val month: Int? = null, val day: Int? = null) {
    override fun toString(): String = listOfNotNull(year, month, day).joinToString("-")
}

@Serializable
data class MediaTag(val id: Int, val name: String, val description: String? = null, val rank: Int? = null, val isMediaSpoiler: Boolean = false)

@Serializable
data class Studio(val id: Int, val name: String, val isAnimationStudio: Boolean = false)

@Serializable
data class MediaTrailer(val id: String? = null, val site: String? = null, val thumbnail: String? = null)

@Serializable
data class AiringSchedule(val airingAt: Long, val timeUntilAiring: Int, val episode: Int)

@Serializable
data class MediaList(
    val id: Int,
    val status: MediaListStatus,
    val score: Float = 0f,
    val progress: Int = 0,
    val progressVolumes: Int = 0,
    val repeat: Int = 0,
    val private: Boolean = false,
    val notes: String? = null,
    val startedAt: FuzzyDate? = null,
    val completedAt: FuzzyDate? = null
)

@Serializable
data class User(
    val id: Int,
    val name: String,
    val avatar: UserAvatar? = null,
    val bannerImage: String? = null,
    val about: String? = null,
    val statistics: UserStatistics? = null,
    val siteUrl: String? = null
)

@Serializable
data class UserAvatar(val large: String? = null, val medium: String? = null)

@Serializable
data class UserStatistics(val anime: AnimeStatistics? = null, val manga: MangaStatistics? = null)

@Serializable
data class AnimeStatistics(val count: Int = 0, val meanScore: Float = 0f, val minutesWatched: Int = 0, val episodesWatched: Int = 0)

@Serializable
data class MangaStatistics(val count: Int = 0, val meanScore: Float = 0f, val chaptersRead: Int = 0, val volumesRead: Int = 0)

enum class MediaType { ANIME, MANGA }
enum class MediaFormat { TV, TV_SHORT, MOVIE, SPECIAL, OVA, ONA, MUSIC, MANGA, NOVEL, ONE_SHOT }
enum class MediaStatus { FINISHED, RELEASING, NOT_YET_RELEASED, CANCELLED, HIATUS }
enum class Season { WINTER, SPRING, SUMMER, FALL }
enum class MediaSort { POPULARITY_DESC, SCORE_DESC, TRENDING_DESC, TITLE_ROMAJI, TITLE_ENGLISH, START_DATE_DESC, START_DATE, ID, UPDATED_AT_DESC, SEARCH_MATCH, FAVOURITES_DESC }

enum class MediaListStatus {
    CURRENT, PLANNING, COMPLETED, DROPPED, PAUSED, REPEATING;
    val displayName: String get() = when (this) {
        CURRENT -> "Watching"; PLANNING -> "Plan to Watch"; COMPLETED -> "Completed"
        DROPPED -> "Dropped"; PAUSED -> "On Hold"; REPEATING -> "Rewatching"
    }
    val icon: String get() = when (this) {
        CURRENT -> "▶"; PLANNING -> "≡"; COMPLETED -> "✓"
        DROPPED -> "\u2715"; PAUSED -> "\u2225"; REPEATING -> "\u21BB"
    }
}

/** AniList user list entry for sync */
data class AniListListItem(
    val mediaId: Int = 0,
    val status: MediaListStatus = MediaListStatus.CURRENT,
    val score: Float = 0f,
    val progress: Int = 0,
    val idMal: Int? = null,
    val title: AnimeTitle = AnimeTitle(),
    val coverImage: CoverImage = CoverImage(),
    val episodes: Int? = null,
    val updatedAt: Long = 0L
)
