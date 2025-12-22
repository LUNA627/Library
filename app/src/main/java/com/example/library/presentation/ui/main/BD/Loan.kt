package com.example.library.presentation.ui.main.BD

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "loan")
data class Loan(
    @PrimaryKey(autoGenerate = true) val loanId: Long = 0,
    val userId: Long,
    val bookId: Long,
    val issueDate: Long, // ← должно быть Long
    val dueDate: Long,   // ← должно быть Long
    val returnDate: Long? = null,
    val status: String = "active"
)