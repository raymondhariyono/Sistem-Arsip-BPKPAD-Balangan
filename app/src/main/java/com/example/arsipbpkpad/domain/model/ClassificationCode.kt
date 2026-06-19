package com.example.arsipbpkpad.domain.model

data class ClassificationCode(
    val code: String,
    val name: String,
    val parentCode: String?,
    val level: Int,
    val isActive: Boolean
)
