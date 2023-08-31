package com.feduss.telegramwear.hilt

import com.feduss.telegramwear.business.ClientInteractor
import com.feduss.telegramwear.business.ClientInteractorImpl
import com.feduss.telegramwear.data.ClientRepository
import com.feduss.telegramwear.data.ClientRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

//Binds the interface with its implementation
@Module
@InstallIn(SingletonComponent::class)
abstract class DataModule {

    @Singleton
    @Binds
    abstract fun bindClientRepository(
        clientRepositoryImpl: ClientRepositoryImpl
    ): ClientRepository

    @Binds
    abstract fun bindClientInteractor(
        clientInteractorImpl: ClientInteractorImpl
    ): ClientInteractor
}