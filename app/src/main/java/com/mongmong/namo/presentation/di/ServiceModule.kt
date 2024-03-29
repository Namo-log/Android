package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import com.mongmong.namo.domain.repositories.GroupRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.create
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    /** 일정 */
    @Provides
    @Singleton
    fun provideScheduleService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : ScheduleRetrofitInterface = retrofit.create(ScheduleRetrofitInterface::class.java)

    /** 기록 */
    @Provides
    fun provideDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)

    @Provides
    fun provideGroupService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit): GroupApiService =
        retrofit.create(GroupApiService::class.java)
}