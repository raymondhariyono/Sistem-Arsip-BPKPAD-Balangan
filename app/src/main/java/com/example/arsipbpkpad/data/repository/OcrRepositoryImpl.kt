package com.example.arsipbpkpad.data.repository

import android.content.Context
import android.net.Uri
import com.example.arsipbpkpad.data.util.safeApiCall
import com.example.arsipbpkpad.domain.model.DomainConstants
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.OcrRepository
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Named

/**
 * Implementation of OcrRepository using Google ML Kit.
 * Handles the Android-specific context and Uri dependencies, providing a pure domain result.
 */
class OcrRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    @Named("ioDispatcher") private val ioDispatcher: CoroutineDispatcher
) : OcrRepository {

    override suspend fun extractText(imageIdentifier: String): DomainResult<String> {
        return safeApiCall(ioDispatcher) {
            val imageUri = Uri.parse(imageIdentifier)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            
            if (result.text.isBlank()) {
                throw java.lang.Exception(DomainConstants.ERROR_NO_TEXT_DETECTED)
            }
            result.text
        }
    }
}
