package com.example.lifelogger.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LogDao {

    // Grabs all entries, sorted with the newest ones at the top
    @Query("SELECT * FROM life_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<LifeLogEntry>>

    // Grabs entries for a specific user. If currentUserId is null, grabs guest logs.
    @Query("SELECT * FROM life_logs WHERE userId = :currentUserId OR (userId IS NULL AND :currentUserId IS NULL) ORDER BY timestamp DESC")
    fun getLogsForUser(currentUserId: String?): Flow<List<LifeLogEntry>>

    // Assigns a userId to any logs that were created while offline/logged out
    @Query("UPDATE life_logs SET userId = :newUserId WHERE userId IS NULL")
    suspend fun adoptGuestLogs(newUserId: String)

    // Saves a new entry. If the ID already exists, it replaces it.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(entry: LifeLogEntry)

    // Grabs only the entries that haven't been sent to Supabase yet
    @Query("SELECT * FROM life_logs WHERE isSyncedWithCloud = 0")
    suspend fun getUnsyncedLogs(): List<LifeLogEntry>

    // Updates a log to mark it as synced once the Supabase upload is successful
    @Query("UPDATE life_logs SET isSyncedWithCloud = 1 WHERE id = :logId")
    suspend fun markAsSynced(logId: String)

    @Query("DELETE FROM life_logs WHERE id = :logId")
    suspend fun deleteLogById(logId: String)

    @Query("SELECT * FROM life_logs WHERE id = :logId")
    suspend fun getLogById(logId: String): LifeLogEntry?
}