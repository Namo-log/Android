package com.mongmong.namo.presentation.ui.splash

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mongmong.namo.data.remote.login.RefreshService
import com.mongmong.namo.data.remote.login.SplashView
import com.mongmong.namo.databinding.ActivitySplashBinding
import com.mongmong.namo.domain.model.LoginResponse
import com.mongmong.namo.domain.model.TokenBody
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.ui.MainActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class SplashActivity : AppCompatActivity(), SplashView {

    private lateinit var binding : ActivitySplashBinding
    private lateinit var splashScreen: SplashScreen
    private val isDataLoaded = MutableStateFlow(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()

        splashScreen.setKeepOnScreenCondition {
            !isDataLoaded.value
        }

        performNetworkRequest()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

    }

    private fun performNetworkRequest() {
        // 네트워크 요청을 수행하고, 완료시 콜백 실행
        CoroutineScope(Dispatchers.IO).launch {
            val splashService = RefreshService()
            splashService.setSplashView(this@SplashActivity)

            val accessToken = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_ACCESS_TOKEN, null)
            val refreshToken = ApplicationClass.sSharedPreferences.getString(ApplicationClass.X_REFRESH_TOKEN, null)
            splashService.splashTokenRefresh(TokenBody(accessToken.toString(), refreshToken.toString()))
        }
    }

    override fun onVerifyTokenSuccess(response: LoginResponse) {
        Log.d("SplashFragment", "onVerifyTokenSuccess")
        // 로그인 성공
        // 토큰 업데이트
        ApplicationClass.sSharedPreferences.edit()
            .putString(ApplicationClass.X_REFRESH_TOKEN, response.result.refreshToken)
            .putString(ApplicationClass.X_ACCESS_TOKEN, response.result.accessToken)
            .apply()

        isDataLoaded.value = true

        startActivity(Intent(this, MainActivity::class.java))
        finish()


    }

    override fun onVerifyTokenFailure(message: String) {
        Log.d("SplashFragment", "onVerifyTokenFailure")
        isDataLoaded.value = true


        startActivity(Intent(this, OnBoardingActivity::class.java))
        finish()


    }
}