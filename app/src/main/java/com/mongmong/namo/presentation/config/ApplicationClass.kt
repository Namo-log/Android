package com.mongmong.namo.presentation.config

import android.app.Application
import android.content.SharedPreferences
import com.mongmong.namo.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import com.mongmong.namo.R
import com.mongmong.namo.presentation.di.NetworkModule
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltAndroidApp
class ApplicationClass: Application() {
    // 테스트 서버 주소
    val API_URL = BuildConfig.BASE_URL

    @Inject
    @NetworkModule.InterceptorRetrofit
    lateinit var interceptorRetrofit: Retrofit

    @Inject
    @NetworkModule.BasicRetrofit
    lateinit var basicRetrofit: Retrofit


    // 실 서버 주소
//    val API_URL = " "

    init {
        instance = this
    }

    // 코틀린의 전역변수 문법
    companion object {
        lateinit var instance: ApplicationClass
            private set

        // 만들어져있는 SharedPreferences 를 사용해야합니다. 재생성하지 않도록 유념해주세요
        val sSharedPreferences: SharedPreferences
            get() = instance.getSharedPreferences("NAMO", MODE_PRIVATE)

        // 버전
        const val VERSION = "1.0.2"

        // JWT Token Header 키 값
        const val X_ACCESS_TOKEN = "X_ACCESS_TOKEN"
        const val X_REFRESH_TOKEN = "X_REFRESH_TOKEN"

        const val SDK_PLATFORM = "SDK_PLATFORM"
        const val SDK_ACCESS_TOKEN = "SDK_ACCESS_TOKEN"

        // Retrofit 인스턴스, 앱 실행시 한번만 생성하여 사용합니다.
        val sRetrofit: Retrofit
            get() = instance.interceptorRetrofit

        val bRetrofit: Retrofit
            get() = instance.basicRetrofit
    }

    // 앱이 처음 생성되는 순간, SP를 새로 만들어주고, 레트로핏 인스턴스를 생성합니다.
    override fun onCreate() {
        super.onCreate()

        // SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_CLIENT_ID, BuildConfig.NAVER_CLIENT_SECRET, getString(R.string.app_name))

    }
}