package com.example.arsipbpkpad.di

import com.example.arsipbpkpad.data.repository.ArchiveRepositoryImpl
import com.example.arsipbpkpad.data.repository.StagingRepositoryImpl
import com.example.arsipbpkpad.domain.repository.ArchiveRepository
import com.example.arsipbpkpad.domain.repository.StagingRepository
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
}
