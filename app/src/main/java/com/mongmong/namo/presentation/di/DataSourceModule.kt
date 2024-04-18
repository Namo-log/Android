package com.mongmong.namo.presentation.di

import android.content.Context
import com.mongmong.namo.data.datasource.category.LocalCategoryDataSource
import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.datasource.schedule.LocalScheduleDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.datasource.diary.LocalDiaryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.group.GroupDataSource
import com.mongmong.namo.data.local.dao.CategoryDao
import com.mongmong.namo.data.local.dao.DiaryDao
import com.mongmong.namo.data.local.dao.ScheduleDao
import com.mongmong.namo.data.remote.category.CategoryApiService
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.schedule.ScheduleApiService
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
    fun provideLocalScheduleDataSource(scheduleDao: ScheduleDao): LocalScheduleDataSource = LocalScheduleDataSource(scheduleDao)
    @Provides
    fun provideRemoteScheduleDataSource(
        apiService: ScheduleApiService, // 개인 쪽
        groupApiService: GroupApiService // 그룹 쪽
    ): RemoteScheduleDataSource = RemoteScheduleDataSource(apiService, groupApiService)

    /** 기록 */
    @Provides
    fun provideLocalDiaryDataSource(diaryDao: DiaryDao): LocalDiaryDataSource = LocalDiaryDataSource(diaryDao)
    @Provides
    fun provideRemoteDiaryDataSource(
        apiService: DiaryApiService,
        @ApplicationContext context: Context
    )
    : RemoteDiaryDataSource = RemoteDiaryDataSource(apiService, context)

    /** 카테고리 */
    @Provides
    fun provideLocalCategoryDataSource(categoryDao: CategoryDao): LocalCategoryDataSource = LocalCategoryDataSource(categoryDao)
    @Provides
    fun provideRemoteCategoryDataSource(apiService: CategoryApiService): RemoteCategoryDataSource = RemoteCategoryDataSource(apiService)

    /** 그룹 */
    @Provides
    fun provideGroupDataSource(
        apiService: GroupApiService,
        @ApplicationContext context: Context
    ): GroupDataSource = GroupDataSource(apiService, context)
}