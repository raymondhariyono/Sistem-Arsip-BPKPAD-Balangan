package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.service.ExcelService
import java.io.InputStream
import javax.inject.Inject

class ImportArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository,
    private val excelService: ExcelService
) {
    suspend operator fun invoke(inputStream: InputStream): DomainResult<Unit> {
        return try {
            val archives = excelService.importFromExcel(inputStream)
            repository.saveArchives(archives)
        } catch (e: Exception) {
            DomainResult.Error("Failed to import Excel: ${e.message}")
        }
    }
}
