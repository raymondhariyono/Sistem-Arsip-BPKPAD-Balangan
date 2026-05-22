package com.example.arsipbpkpad.domain.archive.model

data class ArchiveDocument(
    val id: String,
    val title: String,
    val description: String,
    val date: String,
    val category: String,
    val imageUrl: String? = null
)
