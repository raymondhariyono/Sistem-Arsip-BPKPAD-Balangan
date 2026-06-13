package com.example.arsipbpkpad.domain.model

data class StagedBox(
    val sessionId: String,
    val warehouse: String,
    val rack: String,
    val box: String,
    val year: String,
    val itemCount: Int = 0
)
