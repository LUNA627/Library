package com.example.library.presentation.ui.main.BD

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class User(
    @PrimaryKey(autoGenerate = true) val userId: Long = 0,
    val email: String,
    val password: String,
    val fullName: String,
    val role: String,
    val registrationDate: Long,
    val isBlocked: Boolean
)