package com.example.arsipbpkpad.utils

fun handleNetworkError(message: String?): String {
    if (message == null) return "Terjadi kesalahan sistem yang tidak diketahui."
    
    val lowerMessage = message.lowercase()
    
    return when {
        lowerMessage.contains("unable to resolve host") || 
        lowerMessage.contains("no address associated with hostname") ||
        lowerMessage.contains("failed to connect to") ||
        lowerMessage.contains("connection refused") -> 
            "Gagal terhubung ke server. Silakan periksa koneksi internet Anda."
            
        lowerMessage.contains("timeout") || lowerMessage.contains("timed out") -> 
            "Koneksi lambat atau terputus. Silakan coba lagi nanti."
            
        message.contains("Failed to connect", ignoreCase = true) ->
            "Tidak dapat Mengupload dokumen. Pastikan Anda memiliki akses internet."
            
        lowerMessage.contains("http 401") || lowerMessage.contains("unauthorized") ->
            "Sesi berakhir. Silakan masuk kembali."

        else -> message
    }
}
