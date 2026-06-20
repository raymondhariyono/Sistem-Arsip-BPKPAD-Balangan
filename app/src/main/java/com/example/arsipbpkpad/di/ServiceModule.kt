package com.example.arsipbpkpad.di

import com.example.arsipbpkpad.data.service.ExcelServiceImpl
import com.example.arsipbpkpad.domain.service.ExcelService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class ServiceModule {
    @Binds
    @Singleton
    abstract fun bindExcelService(
        excelServiceImpl: ExcelServiceImpl
    ): ExcelService
}
