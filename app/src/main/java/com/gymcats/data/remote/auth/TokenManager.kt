package com.gymcats.data.remote.auth

import android.content.Context
import android.provider.Settings
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.google.gson.Gson
import com.gymcats.BuildConfig
import com.gymcats.data.remote.models.TokenRequest
import com.gymcats.data.remote.models.TokenResponse
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    @Named("base") private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {
    private val TOKEN_KEY = stringPreferencesKey("jwt_token")
    private val gson = Gson()
    private val baseUrl = BuildConfig.API_BASE_URL.removeSuffix("/")

    suspend fun getOrCreateToken(): String {
        return try {
            val stored = dataStore.data.first()[TOKEN_KEY]
            if (!stored.isNullOrBlank()) return stored

            val deviceId = Settings.Secure.getString(
                context.contentResolver,
                Settings.Secure.ANDROID_ID
            )
            val body = gson.toJson(TokenRequest(deviceId))
                .toRequestBody("application/json".toMediaType())
            val request = Request.Builder()
                .url("$baseUrl/auth/token")
                .post(body)
                .build()
            val response = okHttpClient.newCall(request).execute()
            val tokenResponse = gson.fromJson(response.body?.string(), TokenResponse::class.java)
            dataStore.edit { it[TOKEN_KEY] = tokenResponse.access_token }
            tokenResponse.access_token
        } catch (e: Exception) {
            ""
        }
    }

    suspend fun clearToken() = dataStore.edit { it.remove(TOKEN_KEY) }
}
