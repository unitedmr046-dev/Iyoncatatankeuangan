package com.finora.app.di

import com.finora.app.data.repository.FinoraRepositoryImpl
import com.finora.app.domain.repository.FinoraRepository
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
    abstract fun bindFinoraRepository(
        finoraRepositoryImpl: FinoraRepositoryImpl
    ): FinoraRepository
}
