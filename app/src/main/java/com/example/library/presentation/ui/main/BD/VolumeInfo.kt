package com.example.library.presentation.ui.main.BD


data class VolumeInfo(
    val title: String,
    val authors: List<String>?,
    val description: String?,
    val imageLinks: ImageLinks?, // ← из data.remote.model
    val industryIdentifiers: List<IndustryIdentifier>?
)