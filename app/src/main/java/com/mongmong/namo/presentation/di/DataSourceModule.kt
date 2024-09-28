package com.mongmong.namo.presentation.di

import android.content.Context
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.group.GroupDiaryApiService
import com.mongmong.namo.data.remote.group.GroupScheduleApiService
import com.mongmong.namo.data.remote.ScheduleApiService
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
    fun provideRemoteScheduleDataSource(
        scheduleApiService: ScheduleApiService,
        groupScheduleApiService: GroupScheduleApiService
    ): RemoteScheduleDataSource = RemoteScheduleDataSource(scheduleApiService, groupScheduleApiService)

    /** 기록 */
    @Provides
    fun provideRemoteDiaryDataSource(
        diaryApiService: DiaryApiService,
        groupDiaryApiService: GroupDiaryApiService,
        @ApplicationContext context: Context
    )
    : RemoteDiaryDataSource = RemoteDiaryDataSource(diaryApiService, groupDiaryApiService, context)

    /** 카테고리 */
    @Provides
    fun provideRemoteCategoryDataSource(apiService: CategoryApiService): RemoteCategoryDataSource = RemoteCategoryDataSource(apiService)

    /** 그룹 */
    @Provides
    fun provideGroupDataSource(
        apiService: GroupApiService,
        @ApplicationContext context: Context
    ): GroupDataSource = GroupDataSource(apiService, context)
}