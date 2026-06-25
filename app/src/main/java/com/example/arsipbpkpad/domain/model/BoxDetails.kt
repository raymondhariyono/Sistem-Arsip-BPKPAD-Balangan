package com.example.arsipbpkpad.domain.model

data class BoxDetails(
    val id: String,
    val name: String,
    val shelfId: String,
    val shelfName: String,
    val roomId: String,
    val roomName: String,
    val itemCount: Int = 0
)
