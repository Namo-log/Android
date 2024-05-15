package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.data.remote.group.GroupApiService
import com.mongmong.namo.data.remote.group.GroupDiaryApiService
import com.mongmong.namo.data.remote.group.GroupScheduleApiService
import com.mongmong.namo.data.remote.ScheduleApiService
import com.mongmong.namo.data.remote.TermApiService
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
    /** 인증 */
    @Provides
    @Singleton
    fun provideLoginService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : AuthApiService =
        retrofit.create(AuthApiService::class.java)

    /** 약관 */
    @Provides
    @Singleton
    fun provideTermService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : TermApiService =
        retrofit.create(TermApiService::class.java)

    /** 일정 */
    @Provides
    @Singleton
    fun provideScheduleService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : ScheduleApiService =
        retrofit.create(ScheduleApiService::class.java)

    /** 기록 */
    @Provides
    @Singleton
    fun provideDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)

    /** 카테고리 */
    @Provides
    @Singleton
    fun provideCategoryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit) : CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    /** 그룹 **/
    @Provides
    @Singleton
    fun provideGroupService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit): GroupApiService =
        retrofit.create(GroupApiService::class.java)
    // 모임 일정
    @Provides
    @Singleton
    fun provideGroupScheduleService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit): GroupScheduleApiService =
        retrofit.create(GroupScheduleApiService::class.java)
    // 모임 기록
    @Provides
    @Singleton
    fun provideGroupDiaryService(@NetworkModule.InterceptorRetrofit retrofit: Retrofit): GroupDiaryApiService =
        retrofit.create(GroupDiaryApiService::class.java)


    /** 카카오 맵 **/
    @Provides
    @Singleton
    fun provideKakaoService(retrofit: Retrofit): KakaoAPI =
        retrofit.create(KakaoAPI::class.java)
}