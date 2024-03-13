package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.RemoteDiaryDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.remote.diary.DiaryApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    fun provideLocalDataSource(diaryDao: DiaryDao): LocalDiaryDataSource = LocalDiaryDataSource(diaryDao)
    @Provides
    fun provideRemoteDataSource(apiService: DiaryApiService): RemoteDiaryDataSource = RemoteDiaryDataSource (apiService)
}