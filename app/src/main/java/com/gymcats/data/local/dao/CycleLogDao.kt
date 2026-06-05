package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.gymcats.data.local.entities.CycleLog
import kotlinx.coroutines.flow.Flow

@Dao
interface CycleLogDao {
    data class PhaseMetric(
        val cyclePhase: String,
        val value: Float
    )

    @Query("SELECT * FROM cycle_logs WHERE accountId = :accountId ORDER BY date DESC")
    fun getAllLogs(accountId: Long): Flow<List<CycleLog>>

    @Query("SELECT AVG(energyLevel) FROM cycle_logs WHERE accountId = :accountId AND cyclePhase = :phase AND date >= :fromDate")
    suspend fun avgEnergyForPhase(accountId: Long, phase: String, fromDate: String): Float?

    @Query("SELECT AVG(disposition) FROM cycle_logs WHERE accountId = :accountId AND date >= :fromDate")
    suspend fun avgDisposition(accountId: Long, fromDate: String): Float?

    @Query("SELECT AVG(sleepQuality) FROM cycle_logs WHERE accountId = :accountId AND date >= :fromDate")
    suspend fun avgSleepQuality(accountId: Long, fromDate: String): Float?

    @Query("""
        SELECT cyclePhase, AVG(CASE WHEN cramps THEN 1.0 ELSE 0.0 END) AS value
        FROM cycle_logs
        WHERE accountId = :accountId AND date >= :fromDate
        GROUP BY cyclePhase
    """)
    suspend fun avgCrampsByPhase(accountId: Long, fromDate: String): List<PhaseMetric>

    @Query("SELECT * FROM cycle_logs WHERE accountId = :accountId AND date >= :fromDate ORDER BY date ASC")
    suspend fun getLogsFromDate(accountId: Long, fromDate: String): List<CycleLog>

    @Query("SELECT * FROM cycle_logs WHERE accountId = :accountId AND date = :date LIMIT 1")
    suspend fun getLogForDate(accountId: Long, date: String): CycleLog?

    @Query("UPDATE cycle_logs SET accountId = :newAccountId WHERE accountId = 0")
    suspend fun claimOrphanedLogs(newAccountId: Long): Int

    @Insert
    suspend fun insertLog(log: CycleLog)
}
