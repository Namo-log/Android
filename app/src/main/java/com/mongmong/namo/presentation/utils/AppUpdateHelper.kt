package com.mongmong.namo.presentation.utils

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mongmong.namo.BuildConfig

class AppUpdateHelper(private val context: Context) {

    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(context)
    private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()

    init {
        val configSettings = FirebaseRemoteConfigSettings.Builder()
            .setMinimumFetchIntervalInSeconds(3600)
            .build()
        remoteConfig.setConfigSettingsAsync(configSettings)
    }

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            popupCompleteUpdate()
        }
    }

    fun checkForUpdate(updateActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>, autoLogin: () -> Unit) {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val minVersionCode = remoteConfig.getLong("android_version").toInt()
                Log.d("checkForUpdate", "Remote config latest version code: $minVersionCode")
                val currentVersionCode = BuildConfig.VERSION_CODE

                if (currentVersionCode < minVersionCode) {
                    appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                        if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
                            info.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {
                            appUpdateManager.startUpdateFlowForResult(
                                info,
                                updateActivityResultLauncher,
                                AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                            )
                            return@addOnSuccessListener
                        } else {
                            autoLogin()
                            Log.d("checkForUpdate", "즉시 업데이트 불가능")
                        }
                    }.addOnFailureListener { exception ->
                        autoLogin()
                        Log.e("checkForUpdate", "업데이트 체크 실패", exception)
                    }
                } else {
                    autoLogin()
                    Log.d("checkForUpdate", "업데이트 필요 없음")
                }
            } else {
                autoLogin()
                Log.e("checkForUpdate", "Failed to fetch remote config", task.exception)
            }
        }
    }

    fun registerListener() {
        appUpdateManager.registerListener(installStateUpdatedListener)
    }

    fun unregisterListener() {
        appUpdateManager.unregisterListener(installStateUpdatedListener)
    }

    fun onResumeCheck(updateActivityResultLauncher: ActivityResultLauncher<IntentSenderRequest>, autoLogin: () -> Unit) {
        appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
            if (info.installStatus() == InstallStatus.DOWNLOADED) {
                popupCompleteUpdate()
            } else if (info.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // 업데이트가 진행 중인 경우 다시 업데이트를 시작
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateActivityResultLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.IMMEDIATE).build()
                )
            } else {
                autoLogin()  // 업데이트가 진행 중이지 않을 때만 autoLogin 호출
            }
        }.addOnFailureListener {
            autoLogin()  // 실패한 경우에도 autoLogin 호출
        }
    }

    private fun popupCompleteUpdate() {
        Toast.makeText(context, "업데이트가 다운로드되었습니다. 다시 시작하여 업데이트를 적용하세요.", Toast.LENGTH_LONG).show()
        appUpdateManager.completeUpdate()
    }
}
