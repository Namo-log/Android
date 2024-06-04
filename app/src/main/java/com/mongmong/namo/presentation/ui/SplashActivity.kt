package com.mongmong.namo.presentation.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mongmong.namo.databinding.ActivitySplashBinding
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.ui.login.AuthViewModel
import com.mongmong.namo.presentation.ui.onBoarding.OnBoardingActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding : ActivitySplashBinding
    private lateinit var splashScreen: SplashScreen
    private val isDataLoaded = MutableStateFlow(false)
    private val viewModel : AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !isDataLoaded.value
        }

        autoLogin()
        initObserve()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun autoLogin() {
        viewModel.tryRefreshToken()
    }
    private fun initObserve() {
        viewModel.refreshResponse.observe(this) { response ->
            if (response.code == OK_CODE) {
                ApplicationClass.sSharedPreferences.edit()
                    .putString(ApplicationClass.X_REFRESH_TOKEN, response.result.refreshToken)
                    .putString(ApplicationClass.X_ACCESS_TOKEN, response.result.accessToken)
                    .apply()

                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                finish()
                isDataLoaded.value = true
            } else {
                startActivity(Intent(this@SplashActivity, OnBoardingActivity::class.java))
                finish()
                overridePendingTransition(0, 0)
                isDataLoaded.value = true
            }
        }
    }

    companion object {
        const val OK_CODE = 200
    }
}
