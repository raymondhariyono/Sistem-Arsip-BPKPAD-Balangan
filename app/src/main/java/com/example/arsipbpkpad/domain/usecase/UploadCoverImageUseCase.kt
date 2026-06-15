package com.example.arsipbpkpad.domain.usecase

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import javax.inject.Inject

class UploadCoverImageUseCase @Inject constructor(
    private val repository: ArchiveRepository
) {
    suspend operator fun invoke(id: String, imageByteArray: ByteArray): ResultState<String> {
        return repository.uploadImage(id, imageByteArray)
    }
}
