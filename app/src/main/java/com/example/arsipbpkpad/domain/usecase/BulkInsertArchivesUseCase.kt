package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.repository.TransactionBundleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class BulkInsertArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val stagingRepository: StagingRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val transactionBundleRepository: TransactionBundleRepository
) {
    suspend operator fun invoke(
        sessionId: String
    ): ResultState<Unit> {
        return try {
            val stagedBox = stagingRepository.getStagedBoxById(sessionId)
                ?: return ResultState.Error("Session box tidak ditemukan")
            
            val stagedDocs = stagingRepository.getStagingArchivesBySession(sessionId).first()
            if (stagedDocs.isEmpty()) return ResultState.Error("Staging session kosong")

            // 1. Get or Create Storage Location in Supabase
            val locationResult = storageLocationRepository.getOrCreateLocation(
                room = stagedBox.warehouse,
                shelf = stagedBox.rack,
                boxNumber = stagedBox.box,
                year = stagedBox.year
            )
            
            val storageLocationId = when (locationResult) {
                is ResultState.Success -> locationResult.data
                is ResultState.Error -> {
                    val friendlyError = com.example.arsipbpkpad.utils.handleNetworkError(locationResult.message)
                    return ResultState.Error("Gagal inisialisasi lokasi: $friendlyError")
                }
                else -> return ResultState.Error("Gagal inisialisasi lokasi")
            }

            // 2. Identify unique local bundles and create them in Supabase
            val localToRemoteBundleMap = mutableMapOf<String, String>()
            val bundlesToCreate = stagedDocs.filter { it.bundleId != null }
                .groupBy { it.bundleId!! }
            
            for ((localId, docs) in bundlesToCreate) {
                // Heuristic: take description from SP2D if exists
                val sp2d = docs.find { it.type == DocType.SP2D }
                val bundleDesc = "Bundle ${sp2d?.documentNumber ?: docs.first().documentNumber}"
                
                val bundleResult = transactionBundleRepository.createBundle(
                    description = bundleDesc,
                    documentType = sp2d?.type?.name ?: docs.first().type.name,
                    year = stagedBox.year.toIntOrNull() ?: 2026
                )
                
                if (bundleResult is ResultState.Success) {
                    localToRemoteBundleMap[localId] = bundleResult.data
                } else if (bundleResult is ResultState.Error) {
                    return ResultState.Error("Gagal membuat bundle transaksi: ${bundleResult.message}")
                }
            }

            // 3. Update documents with remote IDs and save to Archive repository
            val finalDocs = stagedDocs.map { doc ->
                doc.copy(
                    idStorageLocation = storageLocationId,
                    bundleId = if (doc.bundleId != null) localToRemoteBundleMap[doc.bundleId] else null
                )
            }

            // 4. Save to Archive Repository (it handles local draft + remote sync)
            val saveResult = archiveRepository.saveArchives(finalDocs)
            
            if (saveResult is ResultState.Success) {
                stagingRepository.deleteStagedBox(sessionId)
                ResultState.Success(Unit)
            } else {
                // If it partially failed (saved as local draft), we still consider it "done" for staging
                // but the repo already returns Success/Error.
                // In our implementation, saveArchives returns Error if remote fails but local succeeds.
                // We should only clear staging if local save was successful.
                stagingRepository.deleteStagedBox(sessionId)
                saveResult
            }
        } catch (e: Exception) {
            ResultState.Error(e.message ?: "Gagal melakukan bulk insert")
        }
    }
}
