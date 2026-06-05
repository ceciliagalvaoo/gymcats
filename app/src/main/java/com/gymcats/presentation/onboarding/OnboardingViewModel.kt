package com.gymcats.presentation.onboarding

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.local.entities.UserProfile
import com.gymcats.data.repository.UserRepository
import com.gymcats.util.scheduleCycleReminder
import com.gymcats.util.scheduleWorkoutReminder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import javax.inject.Inject

data class OnboardingUiState(
    val step: Int = 0,
    val name: String = "",
    val goal: String = "Hipertrofia",
    val lastPeriodDate: String = LocalDate.now().minusDays(14).toString(),
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val selectedDays: Set<DayOfWeek> = setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
    val trainingHour: Int = 7,
    val isSaving: Boolean = false,
    val isDone: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    fun setName(value: String) = update { copy(name = value) }
    fun setGoal(value: String) = update { copy(goal = value) }
    fun setLastPeriodDate(value: String) = update { copy(lastPeriodDate = value) }
    fun setCycleLength(value: Int) = update { copy(cycleLength = value) }
    fun setPeriodLength(value: Int) = update { copy(periodLength = value) }
    fun setTrainingHour(value: Int) = update { copy(trainingHour = value) }
    fun toggleDay(day: DayOfWeek) = update {
        val newDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
        copy(selectedDays = newDays)
    }

    fun nextStep() = update { copy(step = step + 1) }
    fun prevStep() = update { copy(step = (step - 1).coerceAtLeast(0)) }

    fun saveProfile() {
        viewModelScope.launch {
            val s = _uiState.value
            if (s.name.isBlank()) {
                update { copy(error = "Informe seu nome.") }
                return@launch
            }
            update { copy(isSaving = true) }
            val profile = UserProfile(
                name = s.name,
                goal = s.goal,
                lastPeriodDate = s.lastPeriodDate,
                cycleLength = s.cycleLength,
                periodLength = s.periodLength,
                preferredTrainingDays = s.selectedDays.joinToString(",") { it.name },
                preferredTrainingHour = s.trainingHour,
                notificationsEnabled = true
            )
            userRepository.saveProfile(profile)
            scheduleWorkoutReminder(context, s.selectedDays.toList(), s.trainingHour)
            scheduleCycleReminder(
                context,
                LocalDate.parse(s.lastPeriodDate),
                s.cycleLength
            )
            update { copy(isSaving = false, isDone = true) }
        }
    }

    fun clearError() = update { copy(error = null) }
    private fun update(block: OnboardingUiState.() -> OnboardingUiState) {
        _uiState.value = _uiState.value.block()
    }
}
