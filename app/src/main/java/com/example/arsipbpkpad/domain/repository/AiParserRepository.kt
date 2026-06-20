package com.example.arsipbpkpad.domain.repository

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.ParsedMetadata

/**
 * Interface for AI-based metadata parsing.
 */
interface AiParserRepository {
    suspend fun parseMetadata(rawText: String): DomainResult<ParsedMetadata>
}
