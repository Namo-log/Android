package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
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
}