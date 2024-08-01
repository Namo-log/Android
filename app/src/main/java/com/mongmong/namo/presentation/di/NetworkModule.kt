package com.mongmong.namo.presentation.di

import android.content.Context
import android.util.Log
import com.mongmong.namo.BuildConfig
import com.mongmong.namo.data.remote.AnonymousApiService
import com.mongmong.namo.data.remote.AuthApiService
import com.mongmong.namo.data.remote.NetworkChecker
import com.mongmong.namo.data.remote.ReissuanceApiService
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.ReissuanceTokenInterceptor
import com.mongmong.namo.presentation.config.RemoteConfigWrapper
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
import javax.inject.Provider
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    // 403 로직이 있는 API Retrofit
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class BasicRetrofit

    // 403 로직이 없는 API Retrofit
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AnonymousRetrofit

    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ReissuanceRetrofit

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideBasicOkHttpClient(
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
    fun provideBasicRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        @BasicRetrofit client: OkHttpClient,
        remoteConfigWrapper: RemoteConfigWrapper
    ): Retrofit {
        val baseUrl = remoteConfigWrapper.fetchAndActivateConfig()
        Log.d("provideBasicRetrofit", "$baseUrl")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Provides
    @Singleton
    @BasicRetrofit
    fun provideBasicTokenInterceptor(
        apiService: ReissuanceApiService /** 추후 AnonymousRetrofit으로 변경 예정 */
    ): XAccessTokenInterceptor {
        return XAccessTokenInterceptor(apiService)
    }


    @Provides
    @Singleton
    @AnonymousRetrofit
    fun provideAnonymousRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        @AnonymousRetrofit client: OkHttpClient,
        remoteConfigWrapper: RemoteConfigWrapper
    ): Retrofit {
        val baseUrl = remoteConfigWrapper.fetchAndActivateConfig()
        Log.d("provideAnonymousRetrofit", "$baseUrl")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }

    @Provides
    @Singleton
    @AnonymousRetrofit
    fun provideAnonymousOkHttpClient(
       interceptor: HttpLoggingInterceptor,
    ): OkHttpClient =
        OkHttpClient.Builder()
            .readTimeout(5000, TimeUnit.MILLISECONDS)
            .connectTimeout(5000, TimeUnit.MILLISECONDS)
            .addInterceptor(interceptor)
            .build()


    /** 추후 ReissuanceRetrofit 삭제 예정 */
    @Provides
    @Singleton
    @ReissuanceRetrofit
    fun provideReissuanceOkHttpClient(
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
    fun provideReissuanceRetrofit(
        gsonConverterFactory: GsonConverterFactory,
        @ReissuanceRetrofit client: OkHttpClient,
        remoteConfigWrapper: RemoteConfigWrapper
    ): Retrofit {
        val baseUrl = remoteConfigWrapper.fetchAndActivateConfig()
        Log.d("provideBasicRetrofit", "$baseUrl")
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(gsonConverterFactory)
            .build()
    }
    @Provides
    @Singleton
    @ReissuanceRetrofit
    fun provideReissuanceTokenInterceptor(): ReissuanceTokenInterceptor {
        return ReissuanceTokenInterceptor()
    }


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