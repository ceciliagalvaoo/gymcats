package com.gymcats.data.remote.api

import com.gymcats.data.remote.models.ExerciseResponse
import com.gymcats.data.remote.models.HealthResponse
import com.gymcats.data.remote.models.TokenRequest
import com.gymcats.data.remote.models.TokenResponse
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GymCatsApi {
    @GET("health")
    suspend fun health(): HealthResponse

    @POST("auth/token")
    suspend fun getToken(@Body body: TokenRequest): TokenResponse

    @GET("exercise/search/{name}")
    suspend fun searchByName(@Path("name") name: String): List<ExerciseResponse>

    @GET("exercise/bodypart/{bodyPart}")
    suspend fun getByBodyPart(@Path("bodyPart") bodyPart: String): List<ExerciseResponse>

    @GET("exercise/target/{target}")
    suspend fun getByTarget(@Path("target") target: String): List<ExerciseResponse>

    @GET("exercise/equipment/{equipment}")
    suspend fun getByEquipment(@Path("equipment") equipment: String): List<ExerciseResponse>

    @GET("exercise/detail/{id}")
    suspend fun getExerciseDetail(@Path("id") id: String): ExerciseResponse

    @GET("bodyparts")
    suspend fun getBodyParts(): List<String>

    @GET("targets")
    suspend fun getTargets(): List<String>

    @GET("equipment")
    suspend fun getEquipment(): List<String>

    @GET("tips")
    suspend fun getTips(): List<String>
}
