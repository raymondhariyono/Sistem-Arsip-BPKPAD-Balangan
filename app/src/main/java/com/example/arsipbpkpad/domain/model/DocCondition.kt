package com.example.arsipbpkpad.domain.model

enum class DocCondition {
    GOOD, DAMAGED, LOST;

    fun toDisplayString(): String {
        return when (this) {
            GOOD -> "Baik"
            DAMAGED -> "Rusak"
            LOST -> "Hilang"
        }
    }
}
