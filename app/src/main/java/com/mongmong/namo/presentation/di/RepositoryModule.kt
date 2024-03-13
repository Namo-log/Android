package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.RemoteDiaryDataSource
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.data.repositoriyImpl.DiaryRepositoryImpl
import com.mongmong.namo.domain.repositories.DiaryRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    @Provides
    fun provideDiaryRepository(
        localDiaryDataSource: LocalDiaryDataSource,
        remoteDiaryDataSource: RemoteDiaryDataSource,
        networkChecker: NetworkChecker
    ): DiaryRepository = DiaryRepositoryImpl(localDiaryDataSource, remoteDiaryDataSource, networkChecker)
}