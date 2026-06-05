package com.gymcats.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gymcats.data.local.entities.UserProfile
import com.gymcats.data.local.entities.Workout
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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class HomeUiState(
    val profile: UserProfile? = null,
    val currentPhase: CyclePhase? = null,
    val workoutsThisMonth: Int = 0,
    val lastWorkout: Workout? = null,
    val isTodayCheckInDone: Boolean = false,
    val isLoading: Boolean = true,
    val showCycleDateWarning: Boolean = false
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val cycleRepository: CycleRepository,
    private val workoutRepository: WorkoutRepository,
    private val getCyclePhaseUseCase: GetCyclePhaseUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                userRepository.getProfile(),
                workoutRepository.countWorkoutsInMonth(DateUtils.currentMonth()),
                workoutRepository.getLastClosedWorkout(),
                cycleRepository.getAllLogs()
            ) { profile, count, last, cycleLogs ->
                val phase = profile?.let {
                    try {
                        getCyclePhaseUseCase(LocalDate.parse(it.lastPeriodDate), it.cycleLength)
                    } catch (e: Exception) { null }
                }
                val warning = profile?.let {
                    try {
                        val date = LocalDate.parse(it.lastPeriodDate)
                        date.isBefore(LocalDate.now().minusDays(60))
                    } catch (e: Exception) { false }
                } ?: false
                val todayCheckInDone = cycleLogs.any { it.date == DateUtils.today() }
                HomeUiState(
                    profile = profile,
                    currentPhase = phase,
                    workoutsThisMonth = count,
                    lastWorkout = last,
                    isTodayCheckInDone = todayCheckInDone,
                    isLoading = false,
                    showCycleDateWarning = warning
                )
            }.collect { _uiState.value = it }
        }
    }
}
