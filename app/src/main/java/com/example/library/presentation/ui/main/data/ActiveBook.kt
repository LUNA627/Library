package com.example.library.presentation.ui.main.data

data class ActiveBook(
    val title: String,
    val issueDate: String,
    val returnDate: String, // пустая строка — если электронная
    val isElectronic: Boolean,
    val status: String // 🟢 / 🔵
)