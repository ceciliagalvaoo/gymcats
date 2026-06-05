package com.gymcats.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "progress_photos")
data class ProgressPhoto(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val accountId: Long = 0,
    val imagePath: String,
    val date: String,
    val workoutId: Long? = null,
    val notes: String = ""
)
