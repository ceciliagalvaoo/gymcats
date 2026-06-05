package com.gymcats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "workouts")
data class Workout(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long = 0,
    val name: String,
    val date: String,
    val durationMinutes: Int,
    val cyclePhase: String,
    val notes: String = "",
    val isOpen: Boolean = false
)
