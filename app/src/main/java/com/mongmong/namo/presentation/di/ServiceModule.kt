package com.mongmong.namo.presentation.di

import com.mongmong.namo.data.remote.AnonymousApiService
import com.mongmong.namo.data.remote.CategoryApiService
import com.mongmong.namo.data.remote.DiaryApiService
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.data.remote.ReissuanceApiService
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
    /** 익명 (로그인, 토큰 재발급) */
    @Provides
    @Singleton
    fun provideAnonymousService(@NetworkModule.AnonymousRetrofit retrofit: Retrofit) : AnonymousApiService =
        retrofit.create(AnonymousApiService::class.java)

    /** 인증 (로그아웃, 회원탈퇴) */
    @Provides
    @Singleton
    fun provideAuthService(@NetworkModule.BasicRetrofit retrofit: Retrofit) : AuthApiService =
        retrofit.create(AuthApiService::class.java)

    /** 토큰 재발급 (추후 삭제 예정) */
    @Provides
    @Singleton
    fun provideReissuanceService(@NetworkModule.ReissuanceRetrofit retrofit: Retrofit) : ReissuanceApiService =
        retrofit.create(ReissuanceApiService::class.java)

    /** 약관 */
    @Provides
    @Singleton
    fun provideTermService(@NetworkModule.BasicRetrofit retrofit: Retrofit) : TermApiService =
        retrofit.create(TermApiService::class.java)

    /** 일정 */
    @Provides
    @Singleton
    fun provideScheduleService(@NetworkModule.BasicRetrofit retrofit: Retrofit) : ScheduleApiService =
        retrofit.create(ScheduleApiService::class.java)

    /** 기록 */
    @Provides
    @Singleton
    fun provideDiaryService(@NetworkModule.BasicRetrofit retrofit: Retrofit) : DiaryApiService =
        retrofit.create(DiaryApiService::class.java)

    /** 카테고리 */
    @Provides
    @Singleton
    fun provideCategoryService(@NetworkModule.BasicRetrofit retrofit: Retrofit) : CategoryApiService =
        retrofit.create(CategoryApiService::class.java)

    /** 그룹 **/
    @Provides
    @Singleton
    fun provideGroupService(@NetworkModule.BasicRetrofit retrofit: Retrofit): GroupApiService =
        retrofit.create(GroupApiService::class.java)
    // 모임 일정
    @Provides
    @Singleton
    fun provideGroupScheduleService(@NetworkModule.BasicRetrofit retrofit: Retrofit): GroupScheduleApiService =
        retrofit.create(GroupScheduleApiService::class.java)
    // 모임 기록
    @Provides
    @Singleton
    fun provideGroupDiaryService(@NetworkModule.BasicRetrofit retrofit: Retrofit): GroupDiaryApiService =
        retrofit.create(GroupDiaryApiService::class.java)


    /** 카카오 맵 **/
    @Provides
    @Singleton
    fun provideKakaoService(retrofit: Retrofit): KakaoAPI =
        retrofit.create(KakaoAPI::class.java)
}