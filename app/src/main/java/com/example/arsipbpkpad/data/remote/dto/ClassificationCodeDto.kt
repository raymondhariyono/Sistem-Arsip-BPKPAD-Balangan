package com.example.arsipbpkpad.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClassificationCodeDto(
    @SerialName("code") val code: String,
    @SerialName("name") val name: String,
    @SerialName("parent_code") val parentCode: String? = null,
    @SerialName("level") val level: Int = 1,
    @SerialName("is_active") val isActive: Boolean = true
)
