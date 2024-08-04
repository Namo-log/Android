package com.mongmong.namo.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.mongmong.namo.databinding.ActivitySplashBinding
import com.mongmong.namo.presentation.config.ApplicationClass
import com.mongmong.namo.presentation.config.ApplicationClass.Companion.dsManager
import com.mongmong.namo.presentation.ui.login.AuthViewModel
import com.mongmong.namo.presentation.ui.onBoarding.OnBoardingActivity
import com.mongmong.namo.presentation.utils.AppUpdateHelper
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.runBlocking

@AndroidEntryPoint
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    private lateinit var splashScreen: SplashScreen
    private val isDataLoaded = MutableStateFlow(false)
    private val viewModel: AuthViewModel by viewModels()

    private lateinit var appUpdateHelper: AppUpdateHelper

    private val updateActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            // 업데이트 성공
            autoLogin()
        } else {
            // 업데이트 실패
            Toast.makeText(this, "업데이트 실패", Toast.LENGTH_SHORT).show()
        }
    }

    private var autoLoginCalled = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !isDataLoaded.value
        }

        initObserve()

        appUpdateHelper = AppUpdateHelper(this)
        appUpdateHelper.registerListener() // 리스너 등록
        appUpdateHelper.checkForUpdate(updateActivityResultLauncher) {
            autoLogin()
        }

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()
        // 백그라운드에서 포그라운드로 전환될 때 업데이트 상태 확인
        appUpdateHelper.onResumeCheck(updateActivityResultLauncher) {
            autoLogin()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateHelper.unregisterListener() // 리스너 해제
    }

    private fun autoLogin() {
        if (!autoLoginCalled) {
            autoLoginCalled = true
            viewModel.tryRefreshToken()
        }
    }

    private fun initObserve() {
        viewModel.refreshResponse.observe(this) { response ->
            if (response.code == OK_CODE) {
                runBlocking {
                    dsManager.saveAccessToken(response.result.accessToken)
                    dsManager.saveRefreshToken(response.result.refreshToken)
                }

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
