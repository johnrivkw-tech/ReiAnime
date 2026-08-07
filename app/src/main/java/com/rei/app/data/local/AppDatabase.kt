package com.rei.app.data.local

import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// ═══════════════════════════════════════════
// CACHED ANIME
// ═══════════════════════════════════════════
@Entity(tableName = "anime")
data class AnimeEntity(
    @PrimaryKey val id: Int,
    val titlePreferred: String? = null,
    val coverLarge: String? = null,
    val bannerImage: String? = null,
    val format: String? = null,
    val status: String? = null,
    val episodes: Int? = null,
    val meanScore: Int? = null,
    val popularity: Int? = null,
    val genres: String = "",
    val season: String? = null,
    val seasonYear: Int? = null,
    var isFavourite: Boolean = false
)

// ═══════════════════════════════════════════
// TRACKING
// ═══════════════════════════════════════════
@Entity(tableName = "tracking", primaryKeys = ["mediaId"])
data class TrackingEntity(
    val mediaId: Int,
    val title: String,
    val coverImage: String? = null,
    val bannerImage: String? = null,
    val status: String = "CURRENT",
    val score: Float = 0f,
    val progress: Int = 0,
    val progressVolumes: Int = 0,
    val totalEpisodes: Int? = null,
    val repeatCount: Int = 0,
    val notes: String? = null,
    val private: Boolean = false,
    val startedAt: String? = null,
    val completedAt: String? = null,
    val updatedAt: Long = System.currentTimeMillis(),
    val idMal: Int? = null,
    val syncHash: Long = 0
)

// ═══════════════════════════════════════════
// PER-EPISODE TRACKING
// ═══════════════════════════════════════════
@Entity(tableName = "episodes", primaryKeys = ["mediaId", "episodeNumber"])
data class EpisodeEntity(
    val mediaId: Int,
    val episodeNumber: Int,
    val watched: Boolean = false,
    val note: String? = null,
    val watchedAt: Long? = null,
    val rewatchCount: Int = 0
)

// ═══════════════════════════════════════════
// ECONOMY — Rei Coins
// ═══════════════════════════════════════════
@Entity(tableName = "economy")
data class EconomyEntity(
    @PrimaryKey val key: String,       // "balance", "total_earned", "streak", "last_daily"
    val longValue: Long = 0,
    val stringValue: String = ""
)

@Entity(tableName = "transactions", primaryKeys = ["id"])
data class TransactionEntity(
    val id: Long = System.currentTimeMillis(),
    val type: String,                   // "EARN", "SPEND"
    val amount: Int,
    val reason: String,                 // "track_anime", "complete_anime", "unlock_theme", etc.
    val targetId: String? = null,       // what was bought/earned on
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "unlocks", primaryKeys = ["itemId"])
data class UnlockEntity(
    val itemId: String,                 // e.g. "theme_STRAWBERRY", "accent_AURORA", "card_GLASS"
    val cost: Int,
    val unlockedAt: Long = System.currentTimeMillis()
)

// ═══════════════════════════════════════════
// DAOs
// ═══════════════════════════════════════════
@Dao
interface AnimeDao {
    @Query("SELECT * FROM anime ORDER BY popularity DESC") fun getAllAnime(): Flow<List<AnimeEntity>>
    @Query("SELECT * FROM anime WHERE isFavourite = 1") fun getFavourites(): Flow<List<AnimeEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun insertAll(anime: List<AnimeEntity>)
    @Update suspend fun update(anime: AnimeEntity)
}

@Dao
interface TrackingDao {
    @Query("SELECT * FROM tracking WHERE status = :status ORDER BY updatedAt DESC") fun getByStatus(status: String): Flow<List<TrackingEntity>>
    @Query("SELECT * FROM tracking WHERE status IN ('CURRENT', 'REPEATING') ORDER BY updatedAt DESC") fun getContinueWatching(): Flow<List<TrackingEntity>>
    @Query("SELECT * FROM tracking ORDER BY updatedAt DESC") fun getAll(): Flow<List<TrackingEntity>>
    @Query("SELECT * FROM tracking WHERE mediaId = :id") suspend fun getById(id: Int): TrackingEntity?
    @Query("SELECT COUNT(*) FROM tracking WHERE status = :status") suspend fun countByStatus(status: String): Int
    @Query("SELECT COUNT(*) FROM tracking") suspend fun totalCount(): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(entry: TrackingEntity)
    @Delete suspend fun delete(entry: TrackingEntity)
    @Query("DELETE FROM tracking WHERE mediaId = :id") suspend fun deleteById(id: Int)
    @Query("SELECT * FROM tracking WHERE idMal = :malId") suspend fun getByMalId(malId: Int): TrackingEntity?
    @Query("SELECT * FROM tracking WHERE idMal IS NOT NULL") suspend fun getWithMalId(): List<TrackingEntity>
}

@Dao
interface EpisodeDao {
    @Query("SELECT * FROM episodes WHERE mediaId = :mediaId ORDER BY episodeNumber") fun getEpisodes(mediaId: Int): Flow<List<EpisodeEntity>>
    @Query("SELECT * FROM episodes WHERE mediaId = :mediaId AND watched = 1 ORDER BY episodeNumber") suspend fun getWatched(mediaId: Int): List<EpisodeEntity>
    @Query("SELECT COUNT(*) FROM episodes WHERE mediaId = :mediaId AND watched = 1") suspend fun watchedCount(mediaId: Int): Int
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsert(episode: EpisodeEntity)
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun upsertAll(episodes: List<EpisodeEntity>)
    @Query("DELETE FROM episodes WHERE mediaId = :mediaId") suspend fun deleteForAnime(mediaId: Int)
}

@Dao
interface EconomyDao {
    @Query("SELECT * FROM economy WHERE `key` = :key") suspend fun get(key: String): EconomyEntity?
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun set(entity: EconomyEntity)
    @Query("SELECT * FROM transactions ORDER BY timestamp DESC LIMIT :limit") fun recentTransactions(limit: Int = 50): Flow<List<TransactionEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addTransaction(tx: TransactionEntity)
    @Query("SELECT * FROM unlocks WHERE itemId = :itemId") suspend fun getUnlock(itemId: String): UnlockEntity?
    @Query("SELECT * FROM unlocks") fun allUnlocks(): Flow<List<UnlockEntity>>
    @Insert(onConflict = OnConflictStrategy.REPLACE) suspend fun addUnlock(unlock: UnlockEntity)
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'EARN'") suspend fun totalEarned(): Long?
    @Query("SELECT SUM(amount) FROM transactions WHERE type = 'SPEND'") suspend fun totalSpent(): Long?
}

@Database(entities = [
    AnimeEntity::class, TrackingEntity::class, EpisodeEntity::class,
    EconomyEntity::class, TransactionEntity::class, UnlockEntity::class
], version = 4, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun animeDao(): AnimeDao
    abstract fun trackingDao(): TrackingDao
    abstract fun episodeDao(): EpisodeDao
    abstract fun economyDao(): EconomyDao
}

val MIGRATION_3_4 = object : Migration(3, 4) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE tracking ADD COLUMN idMal INTEGER")
        db.execSQL("ALTER TABLE tracking ADD COLUMN syncHash INTEGER NOT NULL DEFAULT 0")
    }
}
