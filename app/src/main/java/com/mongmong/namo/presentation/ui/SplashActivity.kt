package com.mongmong.namo.presentation.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
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

    private lateinit var appUpdateManager: AppUpdateManager

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

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupCompleteUpdate()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            !isDataLoaded.value
        }

        initObserve()

        appUpdateManager = AppUpdateManagerFactory.create(this)
        checkForUpdate()

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun checkForUpdate() {
        val appUpdateInfoTask = appUpdateManager.appUpdateInfo
        Log.d("checkForUpdate", "checkForUpdate")

        appUpdateInfoTask.addOnSuccessListener { info ->
            if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE) {
                when (info.updatePriority()) {
                    HIGH -> {
                        if (info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            appUpdateManager.startUpdateFlowForResult(
                                info,
                                updateActivityResultLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build())
                        }
                    }
                    MEDIUM -> {
                        autoLogin()
                        if (info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {
                            appUpdateManager.startUpdateFlowForResult(
                                info,
                                updateActivityResultLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build())
                        }
                        appUpdateManager.registerListener(installStateUpdatedListener)
                    }
                    LOW -> {
                        autoLogin()
                    }
                }
            } else {
                Log.d("checkForUpdate", "No updateAvailability")
                autoLogin()
            }
        }.addOnFailureListener { exception ->
            Log.e("checkForUpdate", "Update check failed", exception)
        }
    }

    private fun popupCompleteUpdate() {
        Toast.makeText(
            this,
            "다운로드 완료. 앱을 재시작 해주세요.",
            Toast.LENGTH_LONG
        ).show()

        appUpdateManager.completeUpdate()
    }
    override fun onResume() {
        super.onResume()
        appUpdateManager.appUpdateInfo.addOnSuccessListener { appUpdateInfo ->
            if (appUpdateInfo.installStatus() == InstallStatus.DOWNLOADED) {
                popupCompleteUpdate()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        appUpdateManager.unregisterListener(installStateUpdatedListener)
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
        const val HIGH = 3
        const val MEDIUM = 2
        const val LOW = 1
    }
}
