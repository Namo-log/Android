package com.mongmong.namo.di

import com.mongmong.namo.data.datasource.category.RemoteCategoryDataSource
import com.mongmong.namo.data.datasource.diary.RemoteActivityDataSource
import com.mongmong.namo.data.datasource.schedule.RemoteScheduleDataSource
import com.mongmong.namo.data.datasource.diary.RemoteDiaryDataSource
import com.mongmong.namo.data.datasource.friend.RemoteFriendDataSource
import com.mongmong.namo.data.datasource.profile.RemoteProfileDataSource
import com.mongmong.namo.data.remote.ActivityApiService
import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.MoimApiService
import com.mongmong.namo.data.remote.FriendApiService
import com.mongmong.namo.data.remote.ProfileApiService
import com.mongmong.namo.data.remote.ScheduleApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    /** 일정 */
    @Provides
    fun provideRemoteScheduleDataSource(
        scheduleApiService: ScheduleApiService,
        moimApiService: MoimApiService
    ): RemoteScheduleDataSource = RemoteScheduleDataSource(scheduleApiService, moimApiService)

    /** 기록 */
    @Provides
    fun provideRemoteDiaryDataSource(
        diaryApiService: DiaryApiService
    ): RemoteDiaryDataSource = RemoteDiaryDataSource(diaryApiService)

    /** 활동 */
    @Provides
    fun provideActivityDataSource(
        activityApiService: ActivityApiService
    ): RemoteActivityDataSource = RemoteActivityDataSource(activityApiService)

    /** 친구 */
    @Provides
    fun provideFriendDataSource(
        friendApiService: FriendApiService
    ): RemoteFriendDataSource = RemoteFriendDataSource(friendApiService)

    /** 카테고리 */
    @Provides
    fun provideRemoteCategoryDataSource(apiService: CategoryApiService): RemoteCategoryDataSource = RemoteCategoryDataSource(apiService)

    /** 프로필 */
    @Provides
    fun provideRemoteProfileDataSource(apiService: ProfileApiService): RemoteProfileDataSource = RemoteProfileDataSource(apiService)
}