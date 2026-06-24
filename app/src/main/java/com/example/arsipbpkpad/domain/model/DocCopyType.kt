package com.example.arsipbpkpad.domain.model

enum class DocCopyType {
    ORIGINAL, COPY;

    fun toDisplayString(): String {
        return when (this) {
            ORIGINAL -> "Asli"
            COPY -> "Fotokopi"
        }
    }
}
