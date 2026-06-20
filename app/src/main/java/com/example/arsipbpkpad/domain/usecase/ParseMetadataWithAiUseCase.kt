package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.ParsedMetadata
import com.example.arsipbpkpad.domain.repository.AiParserRepository
import javax.inject.Inject

/**
 * UseCase for parsing raw text into structured metadata using AI.
 */
class ParseMetadataWithAiUseCase @Inject constructor(
    private val aiParserRepository: AiParserRepository
) {
    suspend operator fun invoke(rawText: String): DomainResult<ParsedMetadata> {
        return aiParserRepository.parseMetadata(rawText)
    }
}
