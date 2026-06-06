package com.gymcats.domain.usecase

import com.gymcats.domain.model.CyclePhase
import java.time.LocalDate
import java.time.temporal.ChronoUnit
import javax.inject.Inject

class GetCyclePhaseUseCase @Inject constructor() {
    operator fun invoke(lastPeriodDate: LocalDate, cycleLength: Int, periodLength: Int): CyclePhase {
        val daysSince = ChronoUnit.DAYS.between(lastPeriodDate, LocalDate.now()).toInt()
        val dayOfCycle = (daysSince % cycleLength) + 1
        return when {
            dayOfCycle <= periodLength -> CyclePhase.MENSTRUAL
            dayOfCycle <= 13 -> CyclePhase.FOLICULAR
            dayOfCycle <= 16 -> CyclePhase.OVULATORIA
            else -> CyclePhase.LUTEA
        }
    }
}
