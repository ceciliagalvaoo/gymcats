package com.gymcats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val accountId: Long = 0,
    val name: String,
    val goal: String,
    val lastPeriodDate: String,
    val cycleLength: Int,
    val periodLength: Int,
    val preferredTrainingDays: String,
    val preferredTrainingHour: Int,
    val notificationsEnabled: Boolean = true
)
