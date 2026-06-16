package com.example.arsipbpkpad.data.repository

import android.util.Log
import com.example.arsipbpkpad.BuildConfig
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.data.remote.dto.GroqMessage
import com.example.arsipbpkpad.data.remote.dto.GroqRequest
import com.example.arsipbpkpad.data.remote.dto.GroqResponse
import com.example.arsipbpkpad.data.remote.dto.GroqResponseFormat
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
import kotlinx.serialization.json.Json
import javax.inject.Inject

class AiParserRepositoryImpl @Inject constructor(
    private val client: HttpClient
) : AiParserRepository {

    private val apiKey = BuildConfig.GROQ_API_KEY
    private val baseUrl = "https://api.groq.com/openai/v1/chat/completions"

    override suspend fun parseMetadata(rawText: String): ResultState<ParsedMetadata> {
        return try {
            val systemPrompt = """
                You are an expert archive data extraction assistant for BPKPAD Balangan, Indonesia.
                Your task is to extract structured metadata from raw OCR text of government documents, primarily 'Surat Perintah Pencairan Dana' (SP2D).
                
                PATTERNS TO LOOK FOR:
                1. 'docNumber': Look for "Nomor:" followed by a string like "02351/SP2D/1-01.2-22.0-00.1.0.0/LS/05/2025".
                2. 'year': Look for "Tahun Anggaran :" or "Tahun :" followed by a 4-digit number (e.g., 2025).
                3. 'subject': Look for "Untuk :", "Keperluan :", "Uraian :", or "Perihal :". This is the purpose of the document.
                4. 'docType': Identify the document type from the title (e.g., "SURAT PERINTAH PENCAIRAN DANA" is "SP2D").
                5. 'nominal': Look for "uang sebesar Rp" or "Jumlah" followed by a currency value. 
                   CRITICAL: Convert to a pure numeric Double. Remove "Rp", remove dots (.) used as thousand separators, and remove decimals (e.g., "Rp 79.147.899,00" becomes 79147899.0).
                
                RESPONSE RULES:
                - Return ONLY a valid JSON object.
                - DO NOT use markdown formatting (like ```json).
                - Use 'null' for missing fields.
                - Ensure the year is an INTEGER.
                - Ensure the nominal is a DOUBLE containing ONLY digits.
                
                JSON Schema:
                {
                  "docNumber": "string or null",
                  "year": integer or null,
                  "subject": "string or null",
                  "docType": "string or null",
                  "nominal": number or null
                }
            """.trimIndent()

            Log.e("AiParser", "NETWORK: Sending request to Groq API. Raw text preview: ${rawText.take(100)}...")
            val response: HttpResponse = client.post(baseUrl) {
                header("Authorization", "Bearer $apiKey")
                contentType(ContentType.Application.Json)
                setBody(
                    GroqRequest(
                        model = "llama-3.3-70b-versatile",
                        messages = listOf(
                            GroqMessage(role = "system", content = systemPrompt),
                            GroqMessage(role = "user", content = "Extract metadata from this text:\n\n$rawText")
                        ),
                        responseFormat = GroqResponseFormat(type = "json_object")
                    )
                )
            }

            val responseBody = response.body<String>()
            Log.e("AiParser", "NETWORK: Received RAW response (Status: ${response.status}): $responseBody")
            
            if (!response.status.isSuccess()) {
                return ResultState.Error("API Error (${response.status.value}): $responseBody")
            }
            
            val groqResponse = Json { ignoreUnknownKeys = true }.decodeFromString<GroqResponse>(responseBody)
            val content = groqResponse.choices.firstOrNull()?.message?.content

            if (content != null) {
                val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<ParsedMetadata>(content)
                Log.e("AiParser", "NETWORK: Successfully parsed AI JSON to domain model: $parsed")
                ResultState.Success(parsed)
            } else {
                Log.e("AiParser", "NETWORK: choices list is empty or message content is null")
                ResultState.Error("AI returned an empty response.")
            }
        } catch (e: Exception) {
            Log.e("AiParser", "AI Parsing failed", e)
            ResultState.Error("AI Parsing failed: ${e.localizedMessage ?: "Unknown error"}")
        }
    }
}
