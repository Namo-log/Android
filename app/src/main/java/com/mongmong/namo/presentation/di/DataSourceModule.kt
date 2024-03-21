package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.datasource.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.dao.EventDao
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.event.EventRetrofitInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    /** 일정 */
    @Provides
    fun provideLocalScheduleDataStore(scheduleDao: EventDao): LocalScheduleDataSource = LocalScheduleDataSource(scheduleDao)
    @Provides
    fun provideRemoteScheduleDataStore(apiService: EventRetrofitInterface): RemoteScheduleDataSource = RemoteScheduleDataSource(apiService)

    /** 기록 */
    @Provides
    fun provideLocalDataSource(diaryDao: DiaryDao): LocalDiaryDataSource = LocalDiaryDataSource(diaryDao)
    @Provides
    fun provideRemoteDataSource(apiService: DiaryApiService): RemoteDiaryDataSource = RemoteDiaryDataSource (apiService)
}