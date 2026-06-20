package com.example.arsipbpkpad.di

import com.example.arsipbpkpad.data.repository.AiParserRepositoryImpl
import com.example.arsipbpkpad.data.repository.ArchiveRepositoryImpl
import com.example.arsipbpkpad.data.repository.StagingRepositoryImpl
import com.example.arsipbpkpad.data.repository.StorageLocationRepositoryImpl
import com.example.arsipbpkpad.data.repository.TransactionBundleRepositoryImpl
import com.example.arsipbpkpad.domain.repository.AiParserRepository
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
import com.example.arsipbpkpad.domain.repository.StorageLocationRepository
import com.example.arsipbpkpad.domain.repository.TransactionBundleRepository
import com.example.arsipbpkpad.data.repository.OcrRepositoryImpl
import com.example.arsipbpkpad.domain.repository.OcrRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindArchiveRepository(
        archiveRepositoryImpl: ArchiveRepositoryImpl
    ): ArchiveRepository

    @Binds
    @Singleton
    abstract fun bindStagingRepository(
        stagingRepositoryImpl: StagingRepositoryImpl
    ): StagingRepository

    @Binds
    @Singleton
    abstract fun bindAiParserRepository(
        aiParserRepositoryImpl: AiParserRepositoryImpl
    ): AiParserRepository

    @Binds
    @Singleton
    abstract fun bindStorageLocationRepository(
        storageLocationRepositoryImpl: StorageLocationRepositoryImpl
    ): StorageLocationRepository

    @Binds
    @Singleton
    abstract fun bindTransactionBundleRepository(
        transactionBundleRepositoryImpl: TransactionBundleRepositoryImpl
    ): TransactionBundleRepository

    @Binds
    @Singleton
    abstract fun bindOcrRepository(
        ocrRepositoryImpl: OcrRepositoryImpl
    ): OcrRepository
}
