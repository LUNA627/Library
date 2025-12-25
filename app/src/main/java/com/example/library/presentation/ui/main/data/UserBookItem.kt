package com.example.library.presentation.ui.main.data

data class UserBookItem(
    val title: String,
    val returnInfo: String,
    val loanId: Long,
    val canExtend: Boolean // ← можно ли продлить
)