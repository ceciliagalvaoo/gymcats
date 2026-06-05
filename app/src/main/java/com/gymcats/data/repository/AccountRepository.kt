package com.gymcats.data.repository

import com.gymcats.data.auth.SessionManager
import com.gymcats.data.local.dao.AccountDao
import com.gymcats.data.local.dao.CycleLogDao
import com.gymcats.data.local.dao.ProgressPhotoDao
import com.gymcats.data.local.dao.UserProfileDao
import com.gymcats.data.local.dao.WorkoutDao
import com.gymcats.data.local.entities.Account
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AccountRepository @Inject constructor(
    private val dao: AccountDao,
    private val sessionManager: SessionManager,
    private val userProfileDao: UserProfileDao,
    private val workoutDao: WorkoutDao,
    private val cycleLogDao: CycleLogDao,
    private val progressPhotoDao: ProgressPhotoDao
) {
    val biometricAccountFlow: Flow<Account?> = sessionManager.biometricAccountIdFlow.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else dao.getAccount(accountId)
    }

    suspend fun hasAnyAccount(): Boolean = dao.countAccounts() > 0

    suspend fun isAuthenticated(): Boolean = sessionManager.getAuthenticatedAccountId() != null

    suspend fun register(email: String, phone: String, password: String): Result<Account> = runCatching {
        val isFirstAccount = dao.countAccounts() == 0
        val normalizedEmail = email.trim().lowercase()
        val normalizedPhone = phone.filter { it.isDigit() }

        require(normalizedEmail.isNotBlank()) { "Informe seu email." }
        require(normalizedPhone.isNotBlank()) { "Informe seu numero." }
        require(password.length >= 6) { "A senha precisa ter ao menos 6 caracteres." }
        require(dao.getByEmail(normalizedEmail) == null) { "Esse email ja esta cadastrado." }
        require(dao.getByPhone(normalizedPhone) == null) { "Esse numero ja esta cadastrado." }

        val accountId = dao.insertAccount(
            Account(
                email = normalizedEmail,
                phone = normalizedPhone,
                password = password
            )
        )
        if (isFirstAccount) {
            userProfileDao.claimOrphanedProfile(accountId)
            workoutDao.claimOrphanedWorkouts(accountId)
            cycleLogDao.claimOrphanedLogs(accountId)
            progressPhotoDao.claimOrphanedPhotos(accountId)
        }
        sessionManager.onManualLogin(accountId)
        dao.getAccountNow(accountId)!!
    }

    suspend fun login(identifier: String, password: String): Result<Account> = runCatching {
        val raw = identifier.trim()
        val normalizedEmail = raw.lowercase()
        val normalizedPhone = raw.filter { it.isDigit() }
        val account = (if ("@" in raw) {
            dao.getByEmail(normalizedEmail)
        } else {
            dao.getByPhone(normalizedPhone)
        }) ?: throw IllegalArgumentException("Conta não encontrada.")

        require(account.password == password) { "Senha incorreta." }
        sessionManager.onManualLogin(account.id)
        account
    }

    suspend fun loginWithBiometric(): Result<Account> = runCatching {
        val accountId = sessionManager.getBiometricAccountId()
            ?: throw IllegalStateException("Nenhuma conta biométrica disponível.")
        val account = dao.getAccountNow(accountId)
            ?: throw IllegalStateException("Conta biométrica não encontrada.")
        sessionManager.onBiometricLogin(account.id)
        account
    }

    suspend fun getBiometricAccountNow(): Account? {
        val biometricId = sessionManager.getBiometricAccountId() ?: return null
        return dao.getAccountNow(biometricId)
    }

    suspend fun logout() {
        sessionManager.logout()
    }
}

