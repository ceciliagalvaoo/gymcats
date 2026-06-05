package com.gymcats.data.repository

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.CycleLogDao
import com.gymcats.data.local.dao.CycleLogDao.PhaseMetric
import com.gymcats.data.local.entities.CycleLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CycleRepository @Inject constructor(
    private val dao: CycleLogDao,
    private val sessionManager: SessionManager
) {
    fun getAllLogs(): Flow<List<CycleLog>> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(emptyList()) else dao.getAllLogs(accountId)
    }

    suspend fun insertLog(log: CycleLog) {
        val accountId = sessionManager.getAuthenticatedAccountId()
            ?: throw IllegalStateException("Nenhuma conta autenticada.")
        dao.insertLog(log.copy(accountId = accountId))
    }

    suspend fun avgEnergyForPhase(phase: String, fromDate: String): Float? {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return null
        return dao.avgEnergyForPhase(accountId, phase, fromDate)
    }

    suspend fun avgDisposition(fromDate: String): Float? {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return null
        return dao.avgDisposition(accountId, fromDate)
    }

    suspend fun avgSleepQuality(fromDate: String): Float? {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return null
        return dao.avgSleepQuality(accountId, fromDate)
    }

    suspend fun avgCrampsByPhase(fromDate: String): List<PhaseMetric> {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return emptyList()
        return dao.avgCrampsByPhase(accountId, fromDate)
    }

    suspend fun getLogsFromDate(fromDate: String): List<CycleLog> {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return emptyList()
        return dao.getLogsFromDate(accountId, fromDate)
    }

    suspend fun getLogForDate(date: String): CycleLog? {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return null
        return dao.getLogForDate(accountId, date)
    }
}
