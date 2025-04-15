package com.gradu.core.di

import android.app.Application
import com.gradu.core.utils.DataStoreManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideDataStoreManager(app: Application): DataStoreManager {
        return DataStoreManager(app.applicationContext)
    }
}
