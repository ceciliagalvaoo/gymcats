package com.gymcats.data.local.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "accounts",
    indices = [
        Index(value = ["email"], unique = true),
        Index(value = ["phone"], unique = true)
    ]
)
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val email: String,
    val phone: String,
    val password: String
)
