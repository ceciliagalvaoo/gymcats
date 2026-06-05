package com.gymcats.presentation.profile

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.debug.DebugDataSeeder
import com.gymcats.data.local.entities.UserProfile
import com.gymcats.data.remote.auth.TokenManager
import com.gymcats.data.repository.AccountRepository
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

data class ProfileUiState(
    val name: String = "",
    val goal: String = "",
    val lastPeriodDate: String = "",
    val cycleLength: Int = 28,
    val periodLength: Int = 5,
    val selectedDays: Set<DayOfWeek> = emptySet(),
    val trainingHour: Int = 7,
    val notificationsEnabled: Boolean = true,
    val isSaving: Boolean = false,
    val isSeedingDebugData: Boolean = false,
    val isSaved: Boolean = false,
    val error: String? = null,
    val debugSeedMessage: String? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val accountRepository: AccountRepository,
    private val userRepository: UserRepository,
    private val debugDataSeeder: DebugDataSeeder,
    private val tokenManager: TokenManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
    private val _isLoggedOut = MutableStateFlow(false)
    val isLoggedOut: StateFlow<Boolean> = _isLoggedOut.asStateFlow()

    init {
        viewModelScope.launch {
            userRepository.getProfile().collect { profile ->
                profile?.let {
                    val days = it.preferredTrainingDays.split(",")
                        .mapNotNull { d -> runCatching { DayOfWeek.valueOf(d) }.getOrNull() }
                        .toSet()
                    _uiState.value = ProfileUiState(
                        name = it.name,
                        goal = it.goal,
                        lastPeriodDate = it.lastPeriodDate,
                        cycleLength = it.cycleLength,
                        periodLength = it.periodLength,
                        selectedDays = days,
                        trainingHour = it.preferredTrainingHour,
                        notificationsEnabled = it.notificationsEnabled
                    )
                }
            }
        }
    }

    fun setName(v: String) = update { copy(name = v) }
    fun setGoal(v: String) = update { copy(goal = v) }
    fun setLastPeriodDate(v: String) = update { copy(lastPeriodDate = v) }
    fun setCycleLength(v: Int) = update { copy(cycleLength = v) }
    fun setPeriodLength(v: Int) = update { copy(periodLength = v) }
    fun setTrainingHour(v: Int) = update { copy(trainingHour = v) }
    fun setNotifications(v: Boolean) = update { copy(notificationsEnabled = v) }
    fun toggleDay(day: DayOfWeek) = update {
        val newDays = if (selectedDays.contains(day)) selectedDays - day else selectedDays + day
        copy(selectedDays = newDays)
    }

    fun save() {
        viewModelScope.launch {
            update { copy(isSaving = true) }
            val s = _uiState.value
            val profile = UserProfile(
                name = s.name,
                goal = s.goal,
                lastPeriodDate = s.lastPeriodDate,
                cycleLength = s.cycleLength,
                periodLength = s.periodLength,
                preferredTrainingDays = s.selectedDays.joinToString(",") { it.name },
                preferredTrainingHour = s.trainingHour,
                notificationsEnabled = s.notificationsEnabled
            )
            userRepository.saveProfile(profile)
            if (s.notificationsEnabled) {
                scheduleWorkoutReminder(context, s.selectedDays.toList(), s.trainingHour)
                try {
                    scheduleCycleReminder(context, LocalDate.parse(s.lastPeriodDate), s.cycleLength)
                } catch (e: Exception) { /* invalid date, skip */ }
            }
            update { copy(isSaving = false, isSaved = true) }
        }
    }

    fun clearSaved() = update { copy(isSaved = false) }
    fun clearError() = update { copy(error = null) }
    fun clearDebugSeedMessage() = update { copy(debugSeedMessage = null) }

    fun seedDebugData() {
        viewModelScope.launch {
            update { copy(isSeedingDebugData = true, debugSeedMessage = null) }
            debugDataSeeder.seedCurrentAccount()
                .onSuccess { message ->
                    update { copy(isSeedingDebugData = false, debugSeedMessage = message) }
                }
                .onFailure { error ->
                    update {
                        copy(
                            isSeedingDebugData = false,
                            debugSeedMessage = error.message ?: "Não foi possível popular os dados de teste."
                        )
                    }
                }
        }
    }

    fun logout() {
        viewModelScope.launch {
            tokenManager.clearToken()
            accountRepository.logout()
            _isLoggedOut.value = true
        }
    }

    fun clearLoggedOut() {
        _isLoggedOut.value = false
    }

    private fun update(block: ProfileUiState.() -> ProfileUiState) {
        _uiState.value = _uiState.value.block()
    }
}

