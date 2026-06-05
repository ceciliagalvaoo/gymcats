package com.gymcats.data.repository

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.ExerciseLogDao
import com.gymcats.data.local.dao.WorkoutDao
import com.gymcats.data.local.entities.ExerciseLog
import com.gymcats.data.local.entities.Workout
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkoutRepository @Inject constructor(
    private val workoutDao: WorkoutDao,
    private val exerciseLogDao: ExerciseLogDao,
    private val sessionManager: SessionManager
) {
    fun getAllWorkouts(): Flow<List<Workout>> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(emptyList()) else workoutDao.getAllWorkouts(accountId)
    }

    fun getOpenWorkout(): Flow<Workout?> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else workoutDao.getOpenWorkout(accountId)
    }

    fun getLastClosedWorkout(): Flow<Workout?> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else workoutDao.getLastClosedWorkout(accountId)
    }

    fun countWorkoutsInMonth(month: String): Flow<Int> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(0) else workoutDao.countWorkoutsInMonth(accountId, month)
    }

    fun avgDurationInMonth(month: String): Flow<Float?> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else workoutDao.avgDurationInMonth(accountId, month)
    }

    fun getLogsForWorkout(workoutId: Long): Flow<List<ExerciseLog>> =
        exerciseLogDao.getLogsForWorkout(workoutId)

    suspend fun startWorkout(name: String, cyclePhase: String): Long {
        val accountId = sessionManager.getAuthenticatedAccountId()
            ?: throw IllegalStateException("Nenhuma conta autenticada.")
        val workout = Workout(
            accountId = accountId,
            name = name,
            date = LocalDate.now().toString(),
            durationMinutes = 0,
            cyclePhase = cyclePhase,
            isOpen = true
        )
        return workoutDao.insertWorkout(workout)
    }

    suspend fun addExerciseToWorkout(log: ExerciseLog) = exerciseLogDao.insertLog(log)
    suspend fun updateExerciseInWorkout(log: ExerciseLog) = exerciseLogDao.updateLog(log)
    suspend fun removeExerciseFromWorkout(log: ExerciseLog) = exerciseLogDao.deleteLog(log)
    suspend fun cancelWorkout(workout: Workout) = workoutDao.deleteWorkout(workout)

    suspend fun closeWorkout(workout: Workout, durationMinutes: Int) {
        workoutDao.updateWorkout(workout.copy(isOpen = false, durationMinutes = durationMinutes))
    }

    suspend fun closeWorkoutById(workoutId: Long, durationMinutes: Int) {
        val workout = workoutDao.getWorkoutById(workoutId) ?: return
        workoutDao.updateWorkout(workout.copy(isOpen = false, durationMinutes = durationMinutes))
    }
}
