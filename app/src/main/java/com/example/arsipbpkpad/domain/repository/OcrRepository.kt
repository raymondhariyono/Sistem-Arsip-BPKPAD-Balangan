package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.DomainResult

/**
 * Interface for OCR operations.
 */
interface OcrRepository {
    /**
     * Extracts text from an image. 
     * Takes an image identifier (e.g. URI string) and returns the extracted text.
     */
    suspend fun extractText(imageIdentifier: String): DomainResult<String>
}
