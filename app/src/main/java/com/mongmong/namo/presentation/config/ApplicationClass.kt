package com.mongmong.namo.presentation.config

import android.app.Application
import android.content.SharedPreferences
import android.provider.ContactsContract.Data
import com.google.firebase.FirebaseApp
import com.mongmong.namo.BuildConfig
import com.kakao.sdk.common.KakaoSdk
import com.kakao.vectormap.KakaoMapSdk
import com.mongmong.namo.R
import com.mongmong.namo.presentation.di.NetworkModule
import com.mongmong.namo.presentation.utils.DataStoreManager
import com.navercorp.nid.NaverIdLoginSDK
import dagger.hilt.android.HiltAndroidApp
import retrofit2.Retrofit
import javax.inject.Inject

@HiltAndroidApp
class ApplicationClass: Application() {
    @Inject
    @NetworkModule.BasicRetrofit
    lateinit var basicRetrofit: Retrofit

    @Inject
    @NetworkModule.AnonymousRetrofit
    lateinit var anonymousRetrofit: Retrofit

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

        lateinit var dsManager: DataStoreManager

        // 버전
        const val VERSION = "1.0.5"

        // JWT Token Header 키 값
        const val X_ACCESS_TOKEN = "X_ACCESS_TOKEN"
        const val X_REFRESH_TOKEN = "X_REFRESH_TOKEN"

        const val SDK_PLATFORM = "SDK_PLATFORM"
    }

    // 앱이 처음 생성되는 순간, SP를 새로 만들어주고, 레트로핏 인스턴스를 생성합니다.
    override fun onCreate() {
        super.onCreate()

        dsManager = DataStoreManager(applicationContext)

        FirebaseApp.initializeApp(this)
        // SDK 초기화
        KakaoSdk.init(this, BuildConfig.KAKAO_API_KEY)
        KakaoMapSdk.init(this, BuildConfig.KAKAO_API_KEY)
        NaverIdLoginSDK.initialize(this, BuildConfig.NAVER_CLIENT_ID, BuildConfig.NAVER_CLIENT_SECRET, getString(R.string.app_name))
    }
}