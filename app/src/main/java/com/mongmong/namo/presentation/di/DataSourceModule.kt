package com.mongmong.namo.presentation.di

import android.content.Context
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.dao.ScheduleDao
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    /** 일정 */
    @Provides
    fun provideLocalScheduleDataStore(scheduleDao: ScheduleDao): LocalScheduleDataSource = LocalScheduleDataSource(scheduleDao)
    @Provides
    fun provideRemoteScheduleDataStore(apiService: ScheduleRetrofitInterface): RemoteScheduleDataSource = RemoteScheduleDataSource(apiService)

    /** 기록 */
    @Provides
    fun provideLocalDiaryDataSource(diaryDao: DiaryDao): LocalDiaryDataSource = LocalDiaryDataSource(diaryDao)
    @Provides
    fun provideRemoteDiaryDataSource(
        apiService: DiaryApiService,
        @ApplicationContext context: Context
    )
    : RemoteDiaryDataSource = RemoteDiaryDataSource(apiService, context)

    @Provides
    fun provideGroupDataSource(
        apiService: GroupApiService,
        @ApplicationContext context: Context
    ): GroupDataSource = GroupDataSource(apiService, context)
}