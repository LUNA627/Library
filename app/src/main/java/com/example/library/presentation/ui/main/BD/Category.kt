package com.example.library.presentation.ui.main.BD

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
    @PrimaryKey(autoGenerate = true) val categoryId: Long = 0,
    val name: String
)