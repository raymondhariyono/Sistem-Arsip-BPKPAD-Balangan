package com.example.arsipbpkpad.domain.model

/**
 * Pure domain model for parsed metadata, stripped of Android and Serialization dependencies.
 */
data class ParsedMetadata(
    val docNumber: String? = null,
    val year: Int? = null,
    val subject: String? = null,
    val docType: String? = null,
    val nominal: Double? = null
)
