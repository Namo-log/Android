package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.event.EventRetrofitInterface
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
    fun provideScheduleService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : EventRetrofitInterface = retrofit.create(EventRetrofitInterface::class.java)

    /** 기록 */
    @Provides
    @Singleton
    fun provideDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)
}