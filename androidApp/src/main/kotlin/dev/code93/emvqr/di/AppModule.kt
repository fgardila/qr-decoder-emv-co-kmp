package dev.code93.emvqr.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.code93.emvqr.data.repository.EmvDecoderRepositoryImpl
import dev.code93.emvqr.data.repository.KScanQrImageRepository
import dev.code93.emvqr.domain.repository.EmvDecoderRepository
import dev.code93.emvqr.domain.repository.QrImageRepository
import dev.code93.qrscanner.core.QrImageScanner
import dev.code93.qrscanner.core.QrImageScanning
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindEmvDecoderRepository(impl: EmvDecoderRepositoryImpl): EmvDecoderRepository

    @Binds
    @Singleton
    abstract fun bindQrImageRepository(impl: KScanQrImageRepository): QrImageRepository

    companion object {
        @Provides
        @Singleton
        fun provideQrImageScanning(): QrImageScanning = QrImageScanner()
    }
}
