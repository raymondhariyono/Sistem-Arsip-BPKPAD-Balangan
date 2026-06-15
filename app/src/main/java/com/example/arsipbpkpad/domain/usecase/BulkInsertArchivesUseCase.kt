package com.example.arsipbpkpad.domain.usecase

import android.content.Context
import androidx.core.net.toUri
import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BulkInsertArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val stagingRepository: StagingRepository,
    @ApplicationContext private val context: Context
) {
    suspend operator fun invoke(
        sessionId: String
    ): ResultState<Unit> {
        return try {
            val stagedDocs = stagingRepository.getStagingArchivesBySession(sessionId).first()
            
            if (stagedDocs.isEmpty()) return ResultState.Error("Staging session kosong atau tidak ditemukan")

            stagedDocs.forEach { doc ->
                var finalDoc = doc
                
                // If there's a local image URI, upload it first
                doc.imageUrl?.let { uriString ->
                    if (uriString.startsWith("content://") || uriString.startsWith("file://")) {
                        try {
                            val uri = uriString.toUri()
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bytes = inputStream?.readBytes()
                            if (bytes != null) {
                                val uploadResult = archiveRepository.uploadImage(doc.id, bytes)
                                if (uploadResult is ResultState.Success) {
                                    finalDoc = doc.copy(imageUrl = uploadResult.data)
                                }
                            }
                            inputStream?.close()
                        } catch (e: Exception) {
                            // Log error but continue with saving doc without image if upload fails
                        }
                    }
                }
                
                archiveRepository.saveArchive(finalDoc)
            }
            
            stagingRepository.deleteStagedBox(sessionId)
            ResultState.Success(Unit)
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Gagal melakukan bulk insert")
        }
    }
}
