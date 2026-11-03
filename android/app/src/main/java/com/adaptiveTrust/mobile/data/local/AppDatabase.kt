package com.adaptiveTrust.mobile.data.local

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.flow.Flow

// ─────────────────────────────────────────────────────────────────────────────
// Entities
// ─────────────────────────────────────────────────────────────────────────────

/**
 * Cached user row — stores only the summarized fields the mobile app needs.
 * Raw behavioral logs never leave the server (data minimization).
 */
@Entity(tableName = "cached_users")
data class CachedUser(
    @PrimaryKey val userId: String,
    val username: String,
    val department: String?,
    val finalScore: Int,
    val baseScore: Int,
    val humanAdjustment: Int,
    val status: String,          // "Green" | "Yellow" | "Red"
    val statusColorHex: String,
    val flagImpossibleTravel: Boolean,
    val flagFailedLogins: Boolean,
    val flagUnknownDevice: Boolean,
    val lastLocation: String?,
    val lastUpdated: String,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Cached alert — limited ring buffer (newest 200 kept).
 */
@Entity(tableName = "cached_alerts")
data class CachedAlert(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val alertId: Long,
    val userId: String,
    val severity: String,        // "CRITICAL" | "HIGH" | "MEDIUM" | "LOW"
    val message: String,
    val ruleTriggered: String?,
    val createdAt: String,
    val acknowledged: Boolean = false,
    val cachedAt: Long = System.currentTimeMillis()
)

/**
 * Local nudge log — audit trail of all admin actions taken from this device.
 */
@Entity(tableName = "nudge_log")
data class NudgeLogEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val username: String,
    val action: String,          // "Verify" | "Approve" | "Suspend"
    val whDelta: Int,
    val reason: String?,
    val newScore: Int,
    val newStatus: String,
    val biometricVerified: Boolean = true,
    val performedAt: Long = System.currentTimeMillis()
)

// ─────────────────────────────────────────────────────────────────────────────
// DAOs
// ─────────────────────────────────────────────────────────────────────────────

@Dao
interface UserCacheDao {

    /** Observe live user list sorted by score ascending (riskiest first). */
    @Query("SELECT * FROM cached_users ORDER BY finalScore ASC")
    fun observeAll(): Flow<List<CachedUser>>

    @Query("SELECT * FROM cached_users WHERE status = :status ORDER BY finalScore ASC")
    fun observeByStatus(status: String): Flow<List<CachedUser>>

    @Query("SELECT * FROM cached_users WHERE userId = :userId LIMIT 1")
    suspend fun getById(userId: String): CachedUser?

    @Query("SELECT COUNT(*) FROM cached_users WHERE status = 'Red'")
    fun observeRedCount(): Flow<Int>

    @Upsert
    suspend fun upsertAll(users: List<CachedUser>)

    @Upsert
    suspend fun upsert(user: CachedUser)

    /** Remove stale entries older than [maxAgeMs] that weren't refreshed. */
    @Query("DELETE FROM cached_users WHERE cachedAt < :cutoff")
    suspend fun deleteStale(cutoff: Long)

    @Query("DELETE FROM cached_users WHERE userId = :userId")
    suspend fun deleteById(userId: String)

    @Query("DELETE FROM cached_users")
    suspend fun clear()
}

@Dao
interface AlertCacheDao {

    @Query("SELECT * FROM cached_alerts ORDER BY cachedAt DESC LIMIT :limit")
    fun observeRecent(limit: Int = 200): Flow<List<CachedAlert>>

    @Query("SELECT * FROM cached_alerts WHERE acknowledged = 0 ORDER BY cachedAt DESC")
    fun observeUnacknowledged(): Flow<List<CachedAlert>>

    @Query("SELECT COUNT(*) FROM cached_alerts WHERE acknowledged = 0")
    fun observeUnacknowledgedCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(alert: CachedAlert)

    @Query("UPDATE cached_alerts SET acknowledged = 1 WHERE id = :id")
    suspend fun acknowledge(id: Long)

    @Query("UPDATE cached_alerts SET acknowledged = 1")
    suspend fun acknowledgeAll()

    /** Keep only the newest [keepCount] alerts to bound storage. */
    @Query("""
        DELETE FROM cached_alerts WHERE id NOT IN (
            SELECT id FROM cached_alerts ORDER BY cachedAt DESC LIMIT :keepCount
        )
    """)
    suspend fun trimToLimit(keepCount: Int = 200)

    @Query("DELETE FROM cached_alerts")
    suspend fun clear()
}

@Dao
interface NudgeLogDao {

    @Query("SELECT * FROM nudge_log ORDER BY performedAt DESC")
    fun observeAll(): Flow<List<NudgeLogEntry>>

    @Query("SELECT * FROM nudge_log WHERE userId = :userId ORDER BY performedAt DESC")
    fun observeForUser(userId: String): Flow<List<NudgeLogEntry>>

    @Insert
    suspend fun insert(entry: NudgeLogEntry)

    @Query("DELETE FROM nudge_log WHERE performedAt < :cutoff")
    suspend fun deleteOlderThan(cutoff: Long)
}

// ─────────────────────────────────────────────────────────────────────────────
// Database
// ─────────────────────────────────────────────────────────────────────────────

@Database(
    entities = [CachedUser::class, CachedAlert::class, NudgeLogEntry::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun userCacheDao(): UserCacheDao
    abstract fun alertCacheDao(): AlertCacheDao
    abstract fun nudgeLogDao(): NudgeLogDao

    companion object {
        const val DATABASE_NAME = "adaptrust.db"

        /** Passphrase encryption hook — in production, integrate SQLCipher here. */
        fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                DATABASE_NAME
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
