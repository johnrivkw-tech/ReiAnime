package com.rei.app.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.math.min
import kotlin.math.pow

/**
 * Retry with exponential backoff for API calls.
 * Default: 3 retries with 1s, 2s, 4s delays.
 */
suspend fun <T> retryWithBackoff(
    maxRetries: Int = 3,
    initialDelayMs: Long = 1000,
    maxDelayMs: Long = 10_000,
    factor: Double = 2.0,
    block: suspend () -> T
): T {
    var lastException: Exception? = null
    repeat(maxRetries + 1) { attempt ->
        try {
            return block()
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxRetries) {
                val delayMs = min(initialDelayMs * factor.pow(attempt).toLong(), maxDelayMs)
                delay(delayMs)
            }
        }
    }
    throw lastException ?: IllegalStateException("Retry failed with no exception")
}

/**
 * Flow-based retry with backoff — for use with repository flows.
 */
fun <T> flowWithRetry(
    maxRetries: Int = 2,
    block: suspend () -> Flow<T>
): Flow<T> = flow {
    var lastException: Exception? = null
    repeat(maxRetries + 1) { attempt ->
        try {
            block().collect { emit(it) }
            return@flow
        } catch (e: Exception) {
            lastException = e
            if (attempt < maxRetries) {
                delay(1000L * (attempt + 1))
            }
        }
    }
    throw lastException ?: IllegalStateException("Flow retry failed")
}

/**
 * Content descriptions for accessibility (TalkBack support).
 */
object ReiContentDescription {
    const val ANIME_CARD = "Anime card"
    const val SCORE_BADGE = "Score rating"
    const val HERO_CAROUSEL = "Featured anime carousel"
    const val NAV_HOME = "Navigate to home"
    const val NAV_SEARCH = "Navigate to search"
    const val NAV_DISCOVER = "Navigate to discover"
    const val NAV_WATCH = "Navigate to streaming"
    const val NAV_PROFILE = "Navigate to profile"
    const val TRACK_BUTTON = "Add to tracking list"
    const val FAVORITE_BUTTON = "Toggle favorite"
    const val SHARE_BUTTON = "Share anime"
    const val PLAY_BUTTON = "Play episode"
    const val COIN_BALANCE = "Rei coin balance"
    const val STREAK_COUNT = "Daily login streak"
    const val SHOP_ITEM = "Shop item"
    const val PURCHASE_BUTTON = "Purchase item"
    const val RECOMMENDATION = "AI recommendation"
    const val QUIZ_ANSWER = "Quiz answer option"
    const val GENRE_CHIP = "Genre filter"
    const val SEARCH_BAR = "Search anime"
    const val PULL_TO_REFRESH = "Pull to refresh"
    const val BACK_BUTTON = "Go back"
    const val SETTINGS = "Open settings"
    const val NOTIFICATION = "Notifications"
    const val EPISODE_GRID = "Episode tracking grid"
    const val FRANCHISE_TREE = "Franchise watch order"
}

/**
 * Network state for UI rendering.
 */
sealed class NetworkState<out T> {
    data object Idle : NetworkState<Nothing>()
    data object Loading : NetworkState<Nothing>()
    data class Success<T>(val data: T) : NetworkState<T>()
    data class Error(val message: String, val isOffline: Boolean = false, val lastSyncMs: Long? = null) : NetworkState<Nothing>()

    val isLoading get() = this is Loading
    val isSuccess get() = this is Success
    val isError get() = this is Error
}

/**
 * Format a "last synced" timestamp for error states.
 */
fun formatLastSync(timestampMs: Long?): String {
    if (timestampMs == null) return "never"
    val diff = System.currentTimeMillis() - timestampMs
    return when {
        diff < 60_000 -> "just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        else -> "${diff / 86_400_000}d ago"
    }
}
