package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.category.CategoryRetrofitInterface
import com.mongmong.namo.data.remote.diary.DiaryApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.schedule.ScheduleRetrofitInterface
import com.mongmong.namo.presentation.ui.home.schedule.map.data.KakaoAPI
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
    @Singleton
    fun provideDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)

    /** 카테고리 */
    @Provides
    @Singleton
    fun provideCategoryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : CategoryRetrofitInterface = retrofit.create(CategoryRetrofitInterface::class.java)

    /** 그룹 **/
    @Provides
    @Singleton
    fun provideGroupService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit): GroupApiService =
        retrofit.create(GroupApiService::class.java)

    /** 카카오 맵 **/
    @Provides
    @Singleton
    fun provideKakaoService(retrofit: Retrofit): KakaoAPI =
        retrofit.create(KakaoAPI::class.java)
}