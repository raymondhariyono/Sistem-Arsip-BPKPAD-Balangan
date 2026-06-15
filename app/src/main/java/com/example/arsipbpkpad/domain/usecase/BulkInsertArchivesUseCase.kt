package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BulkInsertArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val stagingRepository: StagingRepository
) {
    suspend operator fun invoke(
        sessionId: String
    ): ResultState<Unit> {
        return try {
            val stagedDocs = stagingRepository.getStagingArchivesBySession(sessionId).first()
            
            if (stagedDocs.isEmpty()) return ResultState.Error("Staging session kosong atau tidak ditemukan")

            stagedDocs.forEach { doc ->
                archiveRepository.saveArchive(doc)
            }
            
            stagingRepository.deleteStagedBox(sessionId)
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Gagal melakukan bulk insert")
        }
    }
}
