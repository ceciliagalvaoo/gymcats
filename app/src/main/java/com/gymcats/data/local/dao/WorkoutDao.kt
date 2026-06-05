package com.gymcats.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.gymcats.data.local.entities.Workout
import kotlinx.coroutines.flow.Flow

@Dao
interface WorkoutDao {
    @Query("SELECT * FROM workouts WHERE accountId = :accountId ORDER BY date DESC")
    fun getAllWorkouts(accountId: Long): Flow<List<Workout>>

    @Query("SELECT * FROM workouts WHERE accountId = :accountId AND isOpen = 1 LIMIT 1")
    fun getOpenWorkout(accountId: Long): Flow<Workout?>

    @Query("SELECT * FROM workouts WHERE accountId = :accountId AND date LIKE :month || '%'")
    fun getWorkoutsByMonth(accountId: Long, month: String): Flow<List<Workout>>

    @Query("SELECT COUNT(*) FROM workouts WHERE accountId = :accountId AND date LIKE :month || '%' AND isOpen = 0")
    fun countWorkoutsInMonth(accountId: Long, month: String): Flow<Int>

    @Query("SELECT AVG(durationMinutes) FROM workouts WHERE accountId = :accountId AND date LIKE :month || '%' AND isOpen = 0")
    fun avgDurationInMonth(accountId: Long, month: String): Flow<Float?>

    @Query("SELECT * FROM workouts WHERE accountId = :accountId AND isOpen = 0 ORDER BY date DESC, id DESC LIMIT 1")
    fun getLastClosedWorkout(accountId: Long): Flow<Workout?>

    @Query("SELECT * FROM workouts WHERE accountId = :accountId AND date >= :fromDate AND isOpen = 0 ORDER BY date ASC")
    suspend fun getWorkoutsFromDate(accountId: Long, fromDate: String): List<Workout>

    @Query("UPDATE workouts SET accountId = :newAccountId WHERE accountId = 0")
    suspend fun claimOrphanedWorkouts(newAccountId: Long): Int

    @Insert
    suspend fun insertWorkout(workout: Workout): Long

    @Delete
    suspend fun deleteWorkout(workout: Workout)

    @Update
    suspend fun updateWorkout(workout: Workout)
}
