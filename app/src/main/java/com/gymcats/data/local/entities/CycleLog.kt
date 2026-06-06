package com.gymcats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cycle_logs")
data class CycleLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long = 0,
    val date: String,
    val energyLevel: Int,
    val disposition: Int,
    val mood: String,
    val cramps: Boolean,
    val sleepQuality: Int,
    val notes: String = "",
    val cyclePhase: String,
    val workoutId: Long? = null
)
