package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gymcats.data.local.entities.ExerciseLog
import kotlinx.coroutines.flow.Flow

data class ExerciseProgression(
    val exerciseName: String,
    val weight: Float,
    val exerciseApiId: String?,
    val date: String
)

data class ExerciseFrequency(
    val exerciseName: String,
    val occurrences: Int
)

@Dao
interface ExerciseLogDao {
    @Query("SELECT * FROM exercise_logs WHERE workoutId = :workoutId")
    fun getLogsForWorkout(workoutId: Long): Flow<List<ExerciseLog>>

    @Query("SELECT DISTINCT exerciseName FROM exercise_logs ORDER BY exerciseName ASC")
    fun getDistinctExercises(): Flow<List<String>>

    @Query("""
        SELECT el.exerciseName, MAX(el.weight) AS weight, el.exerciseApiId, w.date
        FROM exercise_logs el
        INNER JOIN workouts w ON el.workoutId = w.id
        WHERE w.accountId = :accountId
        AND el.exerciseName = :exerciseName
        AND w.date >= :fromDate
        AND w.isOpen = 0
        GROUP BY w.id
        ORDER BY w.date ASC
    """)
    suspend fun getProgressionForExercise(
        accountId: Long,
        exerciseName: String,
        fromDate: String
    ): List<ExerciseProgression>

    @Query("SELECT DISTINCT exerciseName FROM exercise_logs WHERE workoutId IN (SELECT id FROM workouts WHERE accountId = :accountId AND date >= :fromDate AND isOpen = 0)")
    suspend fun getDistinctExercisesFromDate(accountId: Long, fromDate: String): List<String>

    @Query("""
        SELECT el.exerciseName, COUNT(*) AS occurrences
        FROM exercise_logs el
        INNER JOIN workouts w ON el.workoutId = w.id
        WHERE w.accountId = :accountId
        AND w.date >= :fromDate
        AND w.isOpen = 0
        GROUP BY el.exerciseName
        ORDER BY occurrences DESC, el.exerciseName ASC
    """)
    suspend fun getExerciseFrequencyFromDate(accountId: Long, fromDate: String): List<ExerciseFrequency>

    @Query("""
        SELECT COALESCE(SUM(el.sets), 0)
        FROM exercise_logs el
        INNER JOIN workouts w ON el.workoutId = w.id
        WHERE w.accountId = :accountId
        AND w.date >= :fromDate
        AND w.isOpen = 0
    """)
    suspend fun getTotalSetsFromDate(accountId: Long, fromDate: String): Int

    @Insert
    suspend fun insertLog(log: ExerciseLog)

    @Update
    suspend fun updateLog(log: ExerciseLog)

    @Delete
    suspend fun deleteLog(log: ExerciseLog)
}
