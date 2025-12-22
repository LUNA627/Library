package com.example.library.presentation.ui.main.BD

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "library_book")
data class LibraryBook(
    @PrimaryKey(autoGenerate = true) val bookId: Long = 0,
    val externalBookId: String,
    val categoryId: Long,
    val isElectronic: Boolean,
    val copiesTotal: Int,
    val copiesAvailable: Int
) : Parcelable