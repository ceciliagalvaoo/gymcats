package com.gymcats.data.remote.models

data class ExerciseResponse(
    val id: String,
    val name: String,
    val bodyPart: String,
    val equipment: String,
    val target: String,
    val secondaryMuscles: List<String> = emptyList(),
    val instructions: List<String> = emptyList(),
    val description: String = "",
    val difficulty: String = "",
    val gifUrl: String? = null
)

data class TokenRequest(val device_id: String)
data class TokenResponse(val access_token: String, val token_type: String)
data class HealthResponse(val status: String)
