package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.service.ExcelService
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import javax.inject.Inject

class ExportArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository,
    private val excelService: ExcelService
) {
    suspend operator fun invoke(outputStream: OutputStream, years: List<Int> = emptyList()) {
        val archives = repository.getArchivesFlow(years = years).first()
        excelService.exportToExcel(archives, outputStream)
    }
}
