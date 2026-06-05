package com.gymcats.presentation.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.local.entities.ExerciseLog
import com.gymcats.data.local.entities.Workout
import com.gymcats.data.remote.models.ExerciseResponse
import com.gymcats.data.repository.ExerciseRepository
import com.gymcats.data.repository.UserRepository
import com.gymcats.data.repository.WorkoutRepository
import com.gymcats.domain.model.CyclePhase
import com.gymcats.domain.usecase.GetCyclePhaseUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class ExerciseEntry(
    val exerciseName: String,
    val exerciseApiId: String? = null,
    val muscleGroup: String = "",
    val sets: String = "3",
    val reps: String = "10",
    val weight: String = "0.0"
)

data class WorkoutUiState(
    val workout: Workout? = null,
    val exercises: List<ExerciseLog> = emptyList(),
    val elapsedSeconds: Long = 0L,
    val currentPhase: CyclePhase = CyclePhase.FOLICULAR,
    val isLoading: Boolean = false,
    val error: String? = null,
    val showExerciseSelector: Boolean = false,
    val workoutCancelled: Boolean = false,
    val closedWorkoutId: Long? = null,
    val closedStartTimeMs: Long = 0L,
    // Exercise selector state
    val searchQuery: String = "",
    val searchResults: List<ExerciseResponse> = emptyList(),
    val isSearching: Boolean = false,
    val searchError: String? = null,
    val bodyParts: List<String> = emptyList(),
    val targets: List<String> = emptyList(),
    val equipments: List<String> = emptyList(),
    val selectedFilter: String = "",
    val selectedFilterType: String = "",
    val isLoadingExerciseDetail: Boolean = false,
    val selectedExercise: ExerciseResponse? = null
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val workoutRepository: WorkoutRepository,
    private val exerciseRepository: ExerciseRepository,
    private val userRepository: UserRepository,
    private val getCyclePhaseUseCase: GetCyclePhaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var startTimeMs: Long = 0L

    init {
        viewModelScope.launch {
            val profile = userRepository.getProfile().first()
            val phase = profile?.let {
                try { getCyclePhaseUseCase(LocalDate.parse(it.lastPeriodDate), it.cycleLength) }
                catch (e: Exception) { CyclePhase.FOLICULAR }
            } ?: CyclePhase.FOLICULAR

            workoutRepository.getOpenWorkout().collect { openWorkout ->
                if (openWorkout != null) {
                    startTimer()
                    workoutRepository.getLogsForWorkout(openWorkout.id).collect { logs ->
                        _uiState.value = _uiState.value.copy(
                            workout = openWorkout,
                            exercises = logs,
                            currentPhase = phase
                        )
                    }
                } else {
                    _uiState.value = _uiState.value.copy(workout = null, currentPhase = phase)
                }
            }
        }
        loadFilters()
    }

    fun startNewWorkout(name: String) {
        viewModelScope.launch {
            val phase = _uiState.value.currentPhase.name
            workoutRepository.startWorkout(name.ifBlank { "Treino" }, phase)
            startTimer()
        }
    }

    private fun startTimer() {
        if (timerJob?.isActive == true) return
        startTimeMs = System.currentTimeMillis()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.value = _uiState.value.copy(
                    elapsedSeconds = (System.currentTimeMillis() - startTimeMs) / 1000
                )
            }
        }
    }

    fun addExercise(entry: ExerciseEntry) {
        viewModelScope.launch {
            val workoutId = _uiState.value.workout?.id ?: return@launch
            val log = ExerciseLog(
                workoutId = workoutId,
                exerciseName = entry.exerciseName.trim().lowercase().replaceFirstChar { it.uppercaseChar() },
                exerciseApiId = entry.exerciseApiId,
                muscleGroup = entry.muscleGroup,
                sets = entry.sets.toIntOrNull() ?: 1,
                reps = entry.reps.toIntOrNull() ?: 1,
                weight = entry.weight.toFloatOrNull() ?: 0f
            )
            workoutRepository.addExerciseToWorkout(log)
        }
    }

    fun updateExercise(log: ExerciseLog, entry: ExerciseEntry) {
        viewModelScope.launch {
            workoutRepository.updateExerciseInWorkout(
                log.copy(
                    exerciseName = entry.exerciseName.trim().lowercase().replaceFirstChar { it.uppercaseChar() },
                    exerciseApiId = entry.exerciseApiId,
                    muscleGroup = entry.muscleGroup,
                    sets = entry.sets.toIntOrNull() ?: 1,
                    reps = entry.reps.toIntOrNull() ?: 1,
                    weight = entry.weight.toFloatOrNull() ?: 0f
                )
            )
        }
    }

    fun removeExercise(log: ExerciseLog) {
        viewModelScope.launch { workoutRepository.removeExerciseFromWorkout(log) }
    }

    fun closeWorkout() {
        val workout = _uiState.value.workout ?: return
        if (_uiState.value.exercises.isEmpty()) {
            _uiState.value = _uiState.value.copy(error = "Adicione ao menos um exercício antes de encerrar.")
            return
        }
        _uiState.value = _uiState.value.copy(
            closedWorkoutId = workout.id,
            closedStartTimeMs = startTimeMs
        )
    }

    fun cancelWorkout() {
        val workout = _uiState.value.workout ?: return
        viewModelScope.launch {
            workoutRepository.cancelWorkout(workout)
            timerJob?.cancel()
            _uiState.value = _uiState.value.copy(
                workout = null,
                exercises = emptyList(),
                elapsedSeconds = 0L,
                workoutCancelled = true,
                showExerciseSelector = false,
                selectedExercise = null
            )
        }
    }

    fun showExerciseSelector() {
        _uiState.value = _uiState.value.copy(showExerciseSelector = true)
    }

    fun hideExerciseSelector() {
        _uiState.value = _uiState.value.copy(
            showExerciseSelector = false,
            searchQuery = "",
            searchResults = emptyList(),
            selectedExercise = null,
            selectedFilter = "",
            selectedFilterType = ""
        )
    }

    fun searchExercises(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        if (query.length < 2) {
            _uiState.value = _uiState.value.copy(searchResults = emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true, searchError = null)
            exerciseRepository.searchByName(query)
                .onSuccess { results ->
                    _uiState.value = _uiState.value.copy(searchResults = results, isSearching = false)
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        searchError = "Verifique sua conexão.",
                        isSearching = false
                    )
                }
        }
    }

    fun applyFilter(value: String, type: String) {
        _uiState.value = _uiState.value.copy(selectedFilter = value, selectedFilterType = type)
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSearching = true)
            val result = when (type) {
                "bodypart" -> exerciseRepository.getByBodyPart(value)
                "target" -> exerciseRepository.getByTarget(value)
                "equipment" -> exerciseRepository.getByEquipment(value)
                else -> Result.success(emptyList())
            }
            result.onSuccess { _uiState.value = _uiState.value.copy(searchResults = it, isSearching = false) }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        searchError = "Não foi possível carregar os exercícios.",
                        isSearching = false
                    )
                }
        }
    }

    fun selectExercise(exercise: ExerciseResponse) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoadingExerciseDetail = true,
                searchError = null
            )
            exerciseRepository.getDetail(exercise.id)
                .onSuccess { detail ->
                    _uiState.value = _uiState.value.copy(
                        selectedExercise = detail,
                        isLoadingExerciseDetail = false
                    )
                }
                .onFailure {
                    _uiState.value = _uiState.value.copy(
                        isLoadingExerciseDetail = false,
                searchError = "Não foi possível carregar os detalhes do exercício."
                    )
                }
        }
    }

    fun clearSelectedExercise() {
        _uiState.value = _uiState.value.copy(
            selectedExercise = null,
            isLoadingExerciseDetail = false
        )
    }

    private fun loadFilters() {
        viewModelScope.launch {
            exerciseRepository.getBodyParts().onSuccess { list ->
                _uiState.value = _uiState.value.copy(bodyParts = list)
            }
            exerciseRepository.getTargets().onSuccess { list ->
                _uiState.value = _uiState.value.copy(targets = list)
            }
            exerciseRepository.getEquipment().onSuccess { list ->
                _uiState.value = _uiState.value.copy(equipments = list)
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearClosedWorkoutId() {
        _uiState.value = _uiState.value.copy(closedWorkoutId = null)
    }

    fun clearWorkoutCancelled() {
        _uiState.value = _uiState.value.copy(workoutCancelled = false)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}

