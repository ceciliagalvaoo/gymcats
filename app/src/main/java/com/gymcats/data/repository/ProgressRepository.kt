package com.gymcats.data.repository

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.ProgressPhotoDao
import com.gymcats.data.local.dao.ProgressPhotoWithWorkout
import com.gymcats.data.local.entities.ProgressPhoto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProgressRepository @Inject constructor(
    private val dao: ProgressPhotoDao,
    private val sessionManager: SessionManager
) {
    fun getPhotosFromDate(fromDate: String): Flow<List<ProgressPhotoWithWorkout>> =
        sessionManager.authenticatedAccountIdFlow.flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList()) else dao.getPhotosFromDate(accountId, fromDate)
        }

    suspend fun insertPhoto(photo: ProgressPhoto) {
        val accountId = sessionManager.getAuthenticatedAccountId()
            ?: throw IllegalStateException("Nenhuma conta autenticada.")
        dao.insertPhoto(photo.copy(accountId = accountId))
    }

    suspend fun deletePhoto(photo: ProgressPhoto) = dao.deletePhoto(photo)
}
