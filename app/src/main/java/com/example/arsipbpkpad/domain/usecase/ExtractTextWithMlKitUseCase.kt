package com.example.arsipbpkpad.domain.usecase

import android.content.Context
import android.net.Uri
import com.example.arsipbpkpad.core.common.ResultState
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class ExtractTextWithMlKitUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(imageUri: Uri): ResultState<String> {
        return try {
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            val image = InputImage.fromFilePath(context, imageUri)
            val result = recognizer.process(image).await()
            
            if (result.text.isBlank()) {
                ResultState.Error("No text detected in the image.")
            } else {
                ResultState.Success(result.text)
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Failed to extract text from image.")
        }
    }
}
