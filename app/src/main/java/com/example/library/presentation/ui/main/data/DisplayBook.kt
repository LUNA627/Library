package com.example.library.presentation.ui.main.data

data class DisplayBook(
    val title: String,
    val author: String,
    val category: String,
    val isElectronic: Boolean,
    var isAdded: Boolean = false,
    val imageUrl: String? = null,
    val copiesAvailable: Int = 0
)