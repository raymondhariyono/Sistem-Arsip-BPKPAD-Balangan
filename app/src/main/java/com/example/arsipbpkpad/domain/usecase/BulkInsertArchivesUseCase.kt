package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.domain.model.DocType
import com.example.arsipbpkpad.domain.model.DomainConstants
import com.example.arsipbpkpad.domain.model.DomainResult
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.repository.TransactionBundleRepository
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * UseCase for bulk inserting documents from a staging session into the archive.
 * Follows SRP and uses pure domain abstractions.
 */
class BulkInsertArchivesUseCase @Inject constructor(
    private val archiveRepository: ArchiveRepository,
    private val stagingRepository: StagingRepository,
    private val storageLocationRepository: StorageLocationRepository,
    private val transactionBundleRepository: TransactionBundleRepository
) {
    suspend operator fun invoke(sessionId: String): DomainResult<Boolean> {
        return try {
            val stagedBox = stagingRepository.getStagedBoxById(sessionId)
                ?: return DomainResult.Error(DomainConstants.ERROR_SESSION_NOT_FOUND)
            
            val stagedDocs = stagingRepository.getStagingArchivesBySession(sessionId).first()
            if (stagedDocs.isEmpty()) return DomainResult.Error(DomainConstants.ERROR_STAGING_EMPTY)

            // 1. Get or Create Storage Location
            val locationResult = storageLocationRepository.getOrCreateLocation(
                room = stagedBox.warehouse,
                shelf = stagedBox.rack,
                boxNumber = stagedBox.box,
                year = stagedBox.year
            )
            
            val storageLocationId = when (locationResult) {
                is DomainResult.Success -> locationResult.data
                is DomainResult.Error -> return DomainResult.Error("${DomainConstants.ERROR_LOCATION_INIT_FAILED}: ${locationResult.message}")
            }

            // 2. Identify and create transaction bundles
            val localToRemoteBundleMap = mutableMapOf<String, String>()
            val bundlesToCreate = stagedDocs.filter { it.bundleId != null }
                .groupBy { it.bundleId!! }
            
            for ((localId, docs) in bundlesToCreate) {
                val sp2d = docs.find { it.type == DocType.SP2D }
                val bundleName = sp2d?.documentNumber ?: docs.first().documentNumber ?: "Bundle"
                val bundleDesc = "Bundle otomatis untuk transaksi $bundleName"
                
                val bundleResult = transactionBundleRepository.createBundle(
                    name = bundleName,
                    description = bundleDesc,
                    year = stagedBox.year.toIntOrNull() ?: DomainConstants.DEFAULT_YEAR
                )
                
                if (bundleResult is DomainResult.Success) {
                    localToRemoteBundleMap[localId] = bundleResult.data
                } else if (bundleResult is DomainResult.Error) {
                    return DomainResult.Error("${DomainConstants.ERROR_BUNDLE_CREATION_FAILED}: ${bundleResult.message}")
                }
            }

            // 3. Map documents to final state
            val finalDocs = stagedDocs.map { doc ->
                doc.copy(
                    status = com.example.arsipbpkpad.domain.model.DocStatus.AVAILABLE,
                    idStorageLocation = storageLocationId,
                    bundleId = if (doc.bundleId != null) localToRemoteBundleMap[doc.bundleId] else null
                )
            }

            // 4. Save and cleanup
            val saveResult = archiveRepository.saveArchives(finalDocs)
            
            when (saveResult) {
                is DomainResult.Success -> {
                    stagingRepository.deleteStagedBox(sessionId)
                    DomainResult.Success(saveResult.data)
                }
                is DomainResult.Error -> {
                    // Keep staging if it failed
                    DomainResult.Error(saveResult.message)
                }
            }
        } catch (e: Exception) {
            DomainResult.Error(e.message ?: DomainConstants.ERROR_BULK_INSERT_FAILED)
        }
    }
}
