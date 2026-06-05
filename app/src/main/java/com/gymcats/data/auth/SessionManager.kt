package com.gymcats.data.auth

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val AUTHENTICATED_ACCOUNT_ID = longPreferencesKey("authenticated_account_id")
    private val LAST_ACCOUNT_ID = longPreferencesKey("last_account_id")
    private val BIOMETRIC_ACCOUNT_ID = longPreferencesKey("biometric_account_id")

    val authenticatedAccountIdFlow: Flow<Long?> = dataStore.data.map { it[AUTHENTICATED_ACCOUNT_ID] }
    val lastAccountIdFlow: Flow<Long?> = dataStore.data.map { it[LAST_ACCOUNT_ID] }
    val biometricAccountIdFlow: Flow<Long?> = dataStore.data.map { it[BIOMETRIC_ACCOUNT_ID] }

    suspend fun getAuthenticatedAccountId(): Long? = dataStore.data.first()[AUTHENTICATED_ACCOUNT_ID]
    suspend fun getLastAccountId(): Long? = dataStore.data.first()[LAST_ACCOUNT_ID]
    suspend fun getBiometricAccountId(): Long? = dataStore.data.first()[BIOMETRIC_ACCOUNT_ID]

    suspend fun onManualLogin(accountId: Long) {
        dataStore.edit {
            it[AUTHENTICATED_ACCOUNT_ID] = accountId
            it[LAST_ACCOUNT_ID] = accountId
            it[BIOMETRIC_ACCOUNT_ID] = accountId
        }
    }

    suspend fun onBiometricLogin(accountId: Long) {
        dataStore.edit {
            it[AUTHENTICATED_ACCOUNT_ID] = accountId
            it[LAST_ACCOUNT_ID] = accountId
        }
    }

    suspend fun logout() {
        dataStore.edit { it.remove(AUTHENTICATED_ACCOUNT_ID) }
    }
}
