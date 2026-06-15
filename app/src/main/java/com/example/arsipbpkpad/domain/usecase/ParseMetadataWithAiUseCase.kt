package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.DocType
import java.util.regex.Pattern
import javax.inject.Inject

data class ParsedMetadata(
    val docNumber: String? = null,
    val year: Int? = null,
    val docType: DocType = DocType.SP2D,
    val subject: String? = null
)

class ParseMetadataWithAiUseCase @Inject constructor() {
    operator fun invoke(rawText: String): ResultState<ParsedMetadata> {
        return try {
            // Very basic regex-based "AI" for now
            val docNumberPattern = Pattern.compile("\\d{5}/SP2D/\\d{4}|SP2D-\\d{4}")
            val yearPattern = Pattern.compile("20\\d{2}")
            
            val matcherDoc = docNumberPattern.matcher(rawText)
            val docNumber = if (matcherDoc.find()) matcherDoc.group() else null
            
            val matcherYear = yearPattern.matcher(rawText)
            val year = if (matcherYear.find()) matcherYear.group().toInt() else null
            
            val parsed = ParsedMetadata(
                docNumber = docNumber,
                year = year,
                subject = extractSubject(rawText)
            )
            
            if (parsed.docNumber == null && parsed.year == null) {
                ResultState.Error("Failed to parse meaningful data from text.")
            } else {
                ResultState.Success(parsed)
            }
        } catch (e: Exception) {
            ResultState.Error("AI Processing failed: ${e.message}")
        }
    }
    
    private fun extractSubject(text: String): String? {
        // Try to find lines that look like "Perihal:" or similar
        val lines = text.lines()
        val subjectLine = lines.find { it.contains("Perihal", ignoreCase = true) || it.contains("Tentang", ignoreCase = true) }
        return subjectLine?.substringAfter(":")?.trim()?.substringAfter("Tentang")?.trim()
    }
}
