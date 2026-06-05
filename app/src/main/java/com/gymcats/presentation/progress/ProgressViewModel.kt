package com.gymcats.presentation.progress

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.ExerciseLogDao
import com.gymcats.data.local.dao.ProgressPhotoWithWorkout
import com.gymcats.data.local.dao.WorkoutDao
import com.gymcats.data.repository.CycleRepository
import com.gymcats.data.repository.ProgressRepository
import com.gymcats.domain.model.CyclePhase
import com.gymcats.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ProgressUiState(
    val periodMonths: Int = 1,
    val totalWorkouts: Int = 0,
    val avgWorkoutMinutes: Int = 0,
    val avgDisposition: Float = 0f,
    val avgSleepQuality: Float = 0f,
    val avgCrampsByPhase: Map<String, Float> = emptyMap(),
    val moodDistributionByPhase: Map<String, Map<String, Int>> = emptyMap(),
    val exerciseNames: List<String> = emptyList(),
    val photos: List<ProgressPhotoWithWorkout> = emptyList(),
    val mostFrequentExercise: String = "-",
    val avgEnergy: Float = 0f,
    val topGain: String = "-",
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class ProgressViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val progressRepository: ProgressRepository,
    private val exerciseLogDao: ExerciseLogDao,
    private val workoutDao: WorkoutDao,
    private val sessionManager: SessionManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProgressUiState())
    val uiState: StateFlow<ProgressUiState> = _uiState.asStateFlow()

    init { loadData(1) }

    fun setPeriod(months: Int) {
        _uiState.value = _uiState.value.copy(periodMonths = months)
        loadData(months)
    }

    private fun loadData(months: Int) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val accountId = sessionManager.getAuthenticatedAccountId()
            if (accountId == null) {
                _uiState.value = _uiState.value.copy(isLoading = false)
                return@launch
            }
            val fromDate = DateUtils.fromDate(months)
            val cycleLogs = cycleRepository.getLogsFromDate(fromDate)
            val normalizedLogs = cycleLogs.map { it.copy(cyclePhase = normalizePhaseName(it.cyclePhase)) }

            val avgDisposition = normalizedLogs.map { it.disposition.toFloat() }.average().toFloatOrZero()
            val avgSleepQuality = normalizedLogs.map { it.sleepQuality.toFloat() }.average().toFloatOrZero()
            val avgCrampsByPhase = normalizedLogs
                .groupBy { it.cyclePhase }
                .mapKeys { (phaseName, _) -> phaseLabel(phaseName) }
                .mapValues { (_, logs) ->
                    logs.map { if (it.cramps) 5f else 0f }.average().toFloatOrZero()
                }
                .filterValues { it > 0f }
            val moodDistributionByPhase = normalizedLogs
                .filter { it.mood.isNotBlank() }
                .groupBy { phaseLabel(it.cyclePhase) }
                .mapValues { (_, logs) -> logs.groupingBy { it.mood }.eachCount() }

            val overallAvg = normalizedLogs.map { it.energyLevel.toFloat() }.average().toFloatOrZero()

            val workouts = workoutDao.getWorkoutsFromDate(accountId, fromDate)
            val workoutCount = workouts.size
            val avgWorkoutMinutes = workouts.map { it.durationMinutes.toFloat() }.average().toFloatOrZero().toInt()
            val exercises = exerciseLogDao.getDistinctExercisesFromDate(accountId, fromDate)
            val mostFrequentExercise = exerciseLogDao.getExerciseFrequencyFromDate(accountId, fromDate)
                .firstOrNull()
                ?.exerciseName
                ?: "-"
            val topGain = exercises.mapNotNull { exerciseName ->
                val progression = exerciseLogDao.getProgressionForExercise(accountId, exerciseName, fromDate)
                if (progression.size < 2) return@mapNotNull null

                val gain = progression.last().weight - progression.first().weight
                exerciseName to gain
            }
                .maxByOrNull { it.second }
                ?.let { (exerciseName, gain) ->
                    if (gain > 0f) "$exerciseName (+${"%.1f".format(gain)} kg)" else "-"
                }
                ?: "-"

            progressRepository.getPhotosFromDate(fromDate).collect { photos ->
                _uiState.value = _uiState.value.copy(
                    totalWorkouts = workoutCount,
                    avgWorkoutMinutes = avgWorkoutMinutes,
                    avgDisposition = avgDisposition,
                    avgSleepQuality = avgSleepQuality,
                    avgCrampsByPhase = avgCrampsByPhase,
                    moodDistributionByPhase = moodDistributionByPhase,
                    exerciseNames = exercises,
                    photos = photos,
                    mostFrequentExercise = mostFrequentExercise,
                    avgEnergy = overallAvg,
                    topGain = topGain,
                    isLoading = false
                )
            }
        }
    }

    private fun phaseLabel(phaseName: String): String {
        return CyclePhase.values().firstOrNull { it.name == normalizePhaseName(phaseName) }?.label ?: phaseName
    }

    private fun normalizePhaseName(phaseName: String): String {
        return when (phaseName.trim().uppercase()) {
            "MENSTRUAL" -> CyclePhase.MENSTRUAL.name
            "FOLICULAR" -> CyclePhase.FOLICULAR.name
            "OVULATORY", "OVULATORIA" -> CyclePhase.OVULATORIA.name
            "LUTEAL", "LUTEA" -> CyclePhase.LUTEA.name
            else -> phaseName
        }
    }

    private fun Double.toFloatOrZero(): Float {
        return if (isNaN()) 0f else toFloat()
    }
}
