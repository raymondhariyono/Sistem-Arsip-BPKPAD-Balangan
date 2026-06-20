package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.OcrRepository
import javax.inject.Inject

/**
 * UseCase for extracting text from a document image.
 * Pure logic that delegates to the OCR repository.
 */
class ExtractTextUseCase @Inject constructor(
    private val ocrRepository: OcrRepository
) {
    suspend operator fun invoke(imageIdentifier: String): DomainResult<String> {
        return ocrRepository.extractText(imageIdentifier)
    }
}
