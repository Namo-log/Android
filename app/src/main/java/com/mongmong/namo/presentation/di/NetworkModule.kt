package com.mongmong.namo.presentation.di

import android.content.Context
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.presentation.config.Constants.BASE_URL
import com.mongmong.namo.presentation.config.ReissuanceTokenInterceptor
import com.mongmong.namo.presentation.config.XAccessTokenInterceptor
import com.mongmong.namo.presentation.utils.NetworkCheckerImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // 인터셉터 있는 API Retrofit
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BasicRetrofit

    // 인터셉터 없는 API Retrofit
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ReissuanceRetrofit

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideInterceptorOkHttpClient(
        interceptor: HttpLoggingInterceptor,
        @BasicRetrofit authInterceptor: XAccessTokenInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideInterceptorRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        @BasicRetrofit client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .build()

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideAuthInterceptor(apiService: AuthApiService)
    : XAccessTokenInterceptor = XAccessTokenInterceptor(apiService)

    @Provides
    @Singleton
    @ReissuanceRetrofit
    fun provideBasicRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        @ReissuanceRetrofit client: OkHttpClient
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(gsonConverterFactory)
        .build()

    @Provides
    @Singleton
    @ReissuanceRetrofit
    fun provideBasicOkHttpClient(
       interceptor: HttpLoggingInterceptor,
       @ReissuanceRetrofit authInterceptor: ReissuanceTokenInterceptor
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(interceptor)
            .addInterceptor(authInterceptor)
            .build()

    @Provides
    @Singleton
    @ReissuanceRetrofit
    fun provideReissuanceTokenInterceptor()
            : ReissuanceTokenInterceptor = ReissuanceTokenInterceptor()


    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    @Provides
    @Singleton
    fun provideConverterFactory(): GsonConverterFactory =
        GsonConverterFactory.create()

    @Provides
    @Singleton
    fun provideNetworkChecker(@ApplicationContext context: Context): NetworkChecker = NetworkCheckerImpl(context)


    @Provides
    @Singleton
    fun provideKakaoRetrofit(): Retrofit =
        Retrofit.Builder()
            .baseUrl("https://dapi.kakao.com")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
}