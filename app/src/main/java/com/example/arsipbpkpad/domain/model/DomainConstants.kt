package com.example.arsipbpkpad.domain.model

/**
 * Centralized constants for the domain layer to eliminate magic strings and numbers.
 */
object DomainConstants {
    // Default Values
    const val DEFAULT_CLASSIFICATION_CODE = "900.1.3.1"
    const val DEFAULT_COPY_COUNT = 1
    const val DEFAULT_YEAR = 2026

    // Error Messages
    const val ERROR_SESSION_NOT_FOUND = "Session box tidak ditemukan"
    const val ERROR_STAGING_EMPTY = "Staging session kosong"
    const val ERROR_LOCATION_INIT_FAILED = "Gagal inisialisasi lokasi"
    const val ERROR_BUNDLE_CREATION_FAILED = "Gagal membuat bundle transaksi"
    const val ERROR_BULK_INSERT_FAILED = "Gagal melakukan bulk insert"
    const val ERROR_UNKNOWN = "Terjadi kesalahan yang tidak diketahui"
}
