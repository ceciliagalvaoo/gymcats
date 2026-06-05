package com.gymcats.data.repository

import com.gymcats.data.remote.api.GymCatsApi
import com.gymcats.data.remote.models.ExerciseResponse
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExerciseRepository @Inject constructor(private val api: GymCatsApi) {
    suspend fun searchByName(name: String): Result<List<ExerciseResponse>> =
        runCatching { api.searchByName(name) }

    suspend fun getByBodyPart(bodyPart: String): Result<List<ExerciseResponse>> =
        runCatching { api.getByBodyPart(bodyPart) }

    suspend fun getByTarget(target: String): Result<List<ExerciseResponse>> =
        runCatching { api.getByTarget(target) }

    suspend fun getByEquipment(equipment: String): Result<List<ExerciseResponse>> =
        runCatching { api.getByEquipment(equipment) }

    suspend fun getDetail(id: String): Result<ExerciseResponse> =
        runCatching { api.getExerciseDetail(id) }

    suspend fun getBodyParts(): Result<List<String>> = runCatching { api.getBodyParts() }
    suspend fun getTargets(): Result<List<String>> = runCatching { api.getTargets() }
    suspend fun getEquipment(): Result<List<String>> = runCatching { api.getEquipment() }
}
