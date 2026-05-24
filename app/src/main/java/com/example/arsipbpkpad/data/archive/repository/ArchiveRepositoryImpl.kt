package com.example.arsipbpkpad.data.archive.repository

import com.example.arsipbpkpad.core.common.ResultState
import com.example.arsipbpkpad.domain.archive.model.ArchiveDocument
import com.example.arsipbpkpad.domain.archive.repository.ArchiveRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject


class ArchiveRepositoryImpl @Inject constructor() : ArchiveRepository {
    override fun getArchives(): Flow<ResultState<List<ArchiveDocument>>> = flow {
        emit(ResultState.Loading)
        // Simulate data fetching
        val mockData = listOf(
            ArchiveDocument("1", "SP2D-1029", "Mock description", "2024-05-10", "Keuangan"),
            ArchiveDocument("2", "SP2D-1030", "Mock description", "2024-05-11", "Keuangan")
        )
        emit(ResultState.Success(mockData))
    }

    override suspend fun saveArchive(archive: ArchiveDocument): ResultState<Unit> {
        return ResultState.Success(Unit)
    }

    override fun getArchiveDetail(id: String): Flow<ResultState<ArchiveDocument>> = flow {
        emit(ResultState.Loading)
        // Simulate detail fetching
        val mockDetail = ArchiveDocument(id, "SP2D-2023-11-0045", "Surat Perintah Pencairan Dana (Dinas Pendidikan)", "2023-11-12", "Dinas Pendidikan")
        emit(ResultState.Success(mockDetail))
    }
}
