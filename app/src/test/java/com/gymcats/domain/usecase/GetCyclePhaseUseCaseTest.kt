package com.gymcats.domain.usecase

import com.gymcats.domain.model.CyclePhase
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class GetCyclePhaseUseCaseTest {

    private val useCase = GetCyclePhaseUseCase()

    @Test
    fun returnsMenstrualForFirstFiveDaysOfCycle() {
        val lastPeriodDate = LocalDate.now().minusDays(4)

        val result = useCase(lastPeriodDate, 28)

        assertEquals(CyclePhase.MENSTRUAL, result)
    }

    @Test
    fun returnsOvulatoriaForMidCycleWindow() {
        val lastPeriodDate = LocalDate.now().minusDays(14)

        val result = useCase(lastPeriodDate, 28)

        assertEquals(CyclePhase.OVULATORIA, result)
    }

    @Test
    fun wrapsCycleAndReturnsExpectedPhaseAfterFullCycle() {
        val lastPeriodDate = LocalDate.now().minusDays(31)

        val result = useCase(lastPeriodDate, 28)

        assertEquals(CyclePhase.MENSTRUAL, result)
    }
}
