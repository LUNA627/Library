package com.example.library.presentation.ui.main.data

import com.example.library.R

data class TakenBook(
    val title: String,
    val coverRes: Int = R.drawable.ic_placeholder_book,
    val issueDate: String,
    val returnDate: String?,
    val actualReturnDate: String?,
    val status: String
)