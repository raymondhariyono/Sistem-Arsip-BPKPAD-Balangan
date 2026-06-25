package com.example.arsipbpkpad.data.repository

import com.example.arsipbpkpad.BuildConfig
import com.example.arsipbpkpad.data.remote.dto.GroqMessage
import com.example.arsipbpkpad.data.remote.dto.GroqRequest
import com.example.arsipbpkpad.data.remote.dto.GroqResponse
import com.example.arsipbpkpad.data.remote.dto.GroqResponseFormat
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.DomainConstants
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.model.ParsedMetadata
import com.example.arsipbpkpad.domain.repository.AiParserRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of AiParserRepository using Ktor and Groq API.
 * Uses safeApiCall and DomainResult.
 */
class AiParserRepositoryImpl @Inject constructor(
    private val client: HttpClient,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : AiParserRepository {

    private val apiKey = BuildConfig.GROQ_API_KEY
    private val baseUrl = "https://api.groq.com/openai/v1/chat/completions"

    private val json = Json { ignoreUnknownKeys = true }

    override suspend fun parseMetadata(rawText: String): DomainResult<ParsedMetadata> {
        if (rawText.isBlank()) {
            return DomainResult.Error("No text provided for parsing")
        }

        return safeApiCall(ioDispatcher) {
            val systemPrompt = """
                You are an expert archive data extraction assistant for BPKPAD Balangan, Indonesia.
                Your task is to extract structured metadata from raw OCR text of government documents, primarily 'Surat Perintah Pencairan Dana' (SP2D).
                
                You MUST return the results in JSON format with the following structure:
                {
                  "docNumber": "string or null",
                  "year": integer or null,
                  "subject": "string or null",
                  "docType": "string or null",
                  "nominal": number or null,
                  "isArchiveDocument": boolean
                }
                
                Set "isArchiveDocument" to true only if the text belongs to an SPP, SPM, SP2D, or SPJ document from BPKPAD Balangan.
                If the text is just random words, or other type of document, set "isArchiveDocument" to false.
                Ensure "docType" is one of: SPP, SPM, SP2D, SPJ.
                Only return the JSON object.
            """.trimIndent()

            val response: HttpResponse = client.post(baseUrl) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    GroqRequest(
                        model = "llama-3.3-70b-versatile",
                        messages = listOf(
                            GroqMessage(role = "system", content = systemPrompt),
                            GroqMessage(role = "user", content = "Extract JSON metadata from this text:\n\n$rawText")
                        ),
                        responseFormat = GroqResponseFormat(type = "json_object")
                    )
                )
            }

            if (!response.status.isSuccess()) {
                throw Exception("API Error (${response.status.value})")
            }

            val responseBody = response.body<String>()
            val groqResponse = json.decodeFromString<GroqResponse>(responseBody)
            val content = groqResponse.choices.firstOrNull()?.message?.content
                ?: throw Exception(DomainConstants.ERROR_AI_EMPTY_RESPONSE)

            val parsed = json.decodeFromString<ParsedMetadata>(content)
            if (!parsed.isArchiveDocument) {
                throw Exception(DomainConstants.ERROR_NOT_A_DOCUMENT)
            }
            parsed
        }
    }
}
