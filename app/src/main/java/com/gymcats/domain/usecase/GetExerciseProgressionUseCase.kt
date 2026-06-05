package com.gymcats.domain.usecase

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.ExerciseLogDao
import com.gymcats.data.local.dao.ExerciseProgression
import java.time.LocalDate
import javax.inject.Inject

class GetExerciseProgressionUseCase @Inject constructor(
    private val exerciseLogDao: ExerciseLogDao,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        exerciseName: String,
        periodMonths: Int
    ): List<ExerciseProgression> {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return emptyList()
        val fromDate = LocalDate.now().minusMonths(periodMonths.toLong()).toString()
        return exerciseLogDao.getProgressionForExercise(accountId, exerciseName, fromDate)
    }
}
