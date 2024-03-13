package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.diary.DiaryApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ServiceModule {
    @Provides
    @Singleton
    fun provideDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)
}