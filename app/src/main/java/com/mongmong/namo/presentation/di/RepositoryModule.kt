package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.remote.diary.NetworkChecker
import com.mongmong.namo.data.repositoriyImpl.DiaryRepositoryImpl
import com.mongmong.namo.data.repositoriyImpl.ScheduleRepositoryImpl
import com.mongmong.namo.domain.repositories.DiaryRepository
import com.mongmong.namo.domain.repositories.ScheduleRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {
    /** 일정 */
    @Provides
    fun provideScheduleRepository(
        localScheduleDataSource: LocalScheduleDataSource,
        remoteScheduleDataSource: RemoteScheduleDataSource,
        networkChecker: NetworkChecker
    ): ScheduleRepository = ScheduleRepositoryImpl(localScheduleDataSource, remoteScheduleDataSource, networkChecker)

    /** 기록 */
    @Provides
    fun provideDiaryRepository(
        localDiaryDataSource: LocalDiaryDataSource,
        remoteDiaryDataSource: RemoteDiaryDataSource,
        networkChecker: NetworkChecker
    ): DiaryRepository = DiaryRepositoryImpl(localDiaryDataSource, remoteDiaryDataSource, networkChecker)
}