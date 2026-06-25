package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.service.ExcelService
import java.io.InputStream
import javax.inject.Inject

class ImportArchivesUseCase @Inject constructor(
    private val repository: ArchiveRepository,
    private val excelService: ExcelService,
    private val storageLocationRepository: StorageLocationRepository
) {
    suspend operator fun invoke(inputStream: InputStream): DomainResult<Boolean> {
        return try {
            val archives = excelService.importFromExcel(inputStream)
            if (archives.isEmpty()) {
                return DomainResult.Error("File Excel kosong, tidak ada data untuk diimpor")
            }

            val finalArchives = mutableListOf<ArchiveDocument>()

            for ((index, archive) in archives.withIndex()) {
                val rowNum = index + 2 // Assuming header is row 1
                val metadata = archive.metadata ?: return DomainResult.Error("Metadata hilang pada baris $rowNum")
                
                val warehouse = metadata.warehouse
                val rack = metadata.rack
                val boxNumber = metadata.boxNumber

                if (warehouse.isNullOrBlank() || rack.isNullOrBlank() || boxNumber.isNullOrBlank()) {
                    return DomainResult.Error("Data lokasi (Gudang/Rak/Box) tidak lengkap pada baris $rowNum")
                }

                val locationResult = storageLocationRepository.getOrCreateLocation(
                    room = warehouse,
                    shelf = rack,
                    boxNumber = boxNumber,
                    year = archive.year.toString()
                )

                when (locationResult) {
                    is DomainResult.Success -> {
                        finalArchives.add(archive.copy(idStorageLocation = locationResult.data))
                    }
                    is DomainResult.Error -> {
                        return DomainResult.Error("Gagal memproses lokasi pada baris $rowNum: ${locationResult.message}")
                    }
                }
            }

            return repository.saveArchives(finalArchives)
        } catch (e: Exception) {
            DomainResult.Error("Gagal mengimpor Excel: ${e.message}")
        }
    }
}
