package com.example.arsipbpkpad.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

/**
 * Domain model for parsed metadata.
 */
@Parcelize
@Serializable
data class ParsedMetadata(
    val docNumber: String? = null,
    val year: Int? = null,
    val subject: String? = null,
    val docType: String? = null,
    val nominal: Double? = null,
    val isArchiveDocument: Boolean = true
) : Parcelable

