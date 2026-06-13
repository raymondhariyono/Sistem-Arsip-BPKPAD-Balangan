package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.ArchiveDocument
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import javax.inject.Inject

class BulkInsertArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val stagingRepository: StagingRepository
) {
    suspend operator fun invoke(
        warehouse: String,
        rack: String,
        box: String
    ): ResultState<Unit> {
        return try {
            val stagedDocs = mutableListOf<ArchiveDocument>()
            stagingRepository.getAllStagingArchives().collect { docs ->
                stagedDocs.addAll(docs)
            }
            
            if (stagedDocs.isEmpty()) return ResultState.Error("Staging area kosong")

            val locationId = "$warehouse-$rack-$box"
            
            stagedDocs.forEach { doc ->
                val updatedDoc = doc.copy(idStorageLocation = locationId)
                archiveRepository.saveArchive(updatedDoc)
            }
            
            stagingRepository.clearStaging()
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Gagal melakukan bulk insert")
        }
    }
}
