package com.example.arsipbpkpad.utils

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import java.text.NumberFormat
import java.util.Locale

class CurrencyVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) {
            return TransformedText(text, OffsetMapping.Identity)
        }

        val formattedText = try {
            val number = originalText.toLong()
            val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
            format.maximumFractionDigits = 0
            format.format(number)
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (originalText.isEmpty()) return 0
                // This is a simplification. For a precise mapping, we'd need to track
                // where digits are in the formatted string.
                // For "Rp 1.000", if original is "1000":
                // 0 -> 3 (R p space 1)
                // 1 -> 4
                // 2 -> 6 (dot)
                // 3 -> 7
                // 4 -> 8
                
                // Heuristic: just return the end of the formatted string for simplicity
                // in this specific "Shopping Cart" rapid entry use case.
                return formattedText.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                return originalText.length
            }
        }

        return TransformedText(AnnotatedString(formattedText), offsetMapping)
    }
}
