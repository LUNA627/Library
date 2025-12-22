package com.example.library.presentation.ui.main.data

data class Book(
    val title: String,
    val author: String,
    val category: String,
    val isElectronic: Boolean,
    val imageUrl: String? = null
)