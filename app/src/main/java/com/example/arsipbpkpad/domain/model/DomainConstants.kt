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

    // Repository Errors
    const val ERROR_ARCHIVE_NOT_FOUND = "Arsip tidak ditemukan"
    const val ERROR_API_GENERAL = "Terjadi kesalahan pada layanan AI"
    const val ERROR_DB_GENERAL = "Terjadi kesalahan pada database"
    const val ERROR_AI_EMPTY_RESPONSE = "AI tidak memberikan respon, silakan coba lagi"
    const val ERROR_NOT_A_DOCUMENT = "Teks terdeteksi, namun tidak menyerupai dokumen arsip yang valid"
    const val ERROR_NO_TEXT_DETECTED = "Tidak ada teks yang terdeteksi pada gambar"

    // Validation Messages
    const val VAL_DUPLICATE_DOC = "Nomor dokumen ini sudah ada dengan status yang sama."
}
