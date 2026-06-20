package com.example.arsipbpkpad.domain.service

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import java.io.InputStream
import java.io.OutputStream

interface ExcelService {
    /**
     * Exports a list of [ArchiveDocument] to an Excel file format.
     * @param archives The list of documents to export.
     * @param outputStream The stream to write the Excel data to.
     */
    suspend fun exportToExcel(archives: List<ArchiveDocument>, outputStream: OutputStream)

    /**
     * Imports a list of [ArchiveDocument] from an Excel file stream.
     * @param inputStream The stream containing the Excel data.
     * @return A list of parsed documents.
     */
    suspend fun importFromExcel(inputStream: InputStream): List<ArchiveDocument>
}
