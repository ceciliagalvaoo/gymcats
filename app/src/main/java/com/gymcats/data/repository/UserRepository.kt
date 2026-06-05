package com.gymcats.data.repository

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.UserProfileDao
import com.gymcats.data.local.entities.UserProfile
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepository @Inject constructor(
    private val dao: UserProfileDao,
    private val sessionManager: SessionManager
) {
    fun getProfile(): Flow<UserProfile?> = sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else dao.getProfile(accountId)
    }

    suspend fun saveProfile(profile: UserProfile) {
        val accountId = sessionManager.getAuthenticatedAccountId()
            ?: throw IllegalStateException("Nenhuma conta autenticada.")
        dao.saveProfile(profile.copy(accountId = accountId))
    }

    suspend fun hasProfile(): Boolean {
        val accountId = sessionManager.getAuthenticatedAccountId() ?: return false
        return dao.getProfileNow(accountId) != null
    }
}
