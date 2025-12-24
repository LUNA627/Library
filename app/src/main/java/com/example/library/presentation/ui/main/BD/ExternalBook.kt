package com.example.library.presentation.ui.main.BD

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(tableName = "external_book")
data class ExternalBook(
    @PrimaryKey val apiId: String,
    val title: String,
    val author: String,
    val description: String?,
    val imageUrl: String?,
    val thumbnailUrl: String?,
    val isbn: String?,
    val categoryId: Long = 1L
) : Parcelable