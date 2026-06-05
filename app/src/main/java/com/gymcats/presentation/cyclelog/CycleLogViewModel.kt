package com.gymcats.presentation.cyclelog

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.local.entities.CycleLog
import com.gymcats.data.repository.CycleRepository
import com.gymcats.data.repository.UserRepository
import com.gymcats.data.repository.WorkoutRepository
import com.gymcats.domain.model.CyclePhase
import com.gymcats.domain.usecase.GetCyclePhaseUseCase
import com.gymcats.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

val MOOD_OPTIONS = listOf("Exausta", "Cansada", "Neutra", "Animada", "Euforica")

data class CycleLogUiState(
    val energyLevel: Int = 3,
    val disposition: Int = 3,
    val cramps: Boolean = false,
    val mood: String = "",
    val sleepQuality: Int = 3,
    val notes: String = "",
    val currentPhase: CyclePhase = CyclePhase.FOLICULAR,
    val isSaving: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class CycleLogViewModel @Inject constructor(
    private val cycleRepository: CycleRepository,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val getCyclePhaseUseCase: GetCyclePhaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CycleLogUiState())
    val uiState: StateFlow<CycleLogUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val profile = userRepository.getProfile().first()
            val phase = profile?.let {
                try { getCyclePhaseUseCase(LocalDate.parse(it.lastPeriodDate), it.cycleLength) }
                catch (e: Exception) { CyclePhase.FOLICULAR }
            } ?: CyclePhase.FOLICULAR
            _uiState.value = _uiState.value.copy(currentPhase = phase)
        }
    }

    fun setEnergy(value: Int) = update { copy(energyLevel = value) }
    fun setDisposition(value: Int) = update { copy(disposition = value) }
    fun setCramps(value: Boolean) = update { copy(cramps = value) }
    fun setMood(value: String) = update { copy(mood = value, error = null) }
    fun setSleep(value: Int) = update { copy(sleepQuality = value) }
    fun setNotes(value: String) = update { copy(notes = value) }

    fun save(workoutId: Long, startTimeMs: Long) {
        if (_uiState.value.mood.isBlank()) {
            update { copy(error = "Selecione um humor antes de continuar.") }
            return
        }
        viewModelScope.launch {
            update { copy(isSaving = true) }
            val s = _uiState.value
            val durationMinutes = ((System.currentTimeMillis() - startTimeMs) / 60_000).toInt()
            workoutRepository.closeWorkoutById(workoutId, durationMinutes)
            val log = CycleLog(
                date = DateUtils.today(),
                energyLevel = s.energyLevel,
                disposition = s.disposition,
                mood = s.mood,
                cramps = s.cramps,
                sleepQuality = s.sleepQuality,
                notes = s.notes,
                cyclePhase = s.currentPhase.name
            )
            cycleRepository.insertLog(log)
            update { copy(isSaving = false, isSaved = true) }
        }
    }

    fun clearError() = update { copy(error = null) }
    private fun update(block: CycleLogUiState.() -> CycleLogUiState) {
        _uiState.value = _uiState.value.block()
    }
}
