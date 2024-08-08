package com.mongmong.namo.presentation.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import com.mongmong.namo.BuildConfig
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RemoteConfigWrapper @Inject constructor() {
    private val remoteConfig: FirebaseRemoteConfig by lazy{
        FirebaseRemoteConfig.getInstance().apply {
            val configSettings = FirebaseRemoteConfigSettings.Builder()
                .build()
            setConfigSettingsAsync(configSettings)
        }
    }

    fun fetchAndActivateConfig(): String {
        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            val updated = task.result
            if (task.isSuccessful) {
                Log.d("fetchAndActivateConfig", "Config params updated success: $updated")
            } else {
                Log.d("fetchAndActivateConfig", "Config params updated failed: $updated")
            }
        }

        val baseUrl = remoteConfig.getString("android_baseurl")
        return if (baseUrl.startsWith("http")) {
            Log.d("fetchAndActivateConfig", "baseurl: $baseUrl")
            baseUrl
        } else {
            Log.d("fetchAndActivateConfig", "baseurl is not start http")
            BuildConfig.BASE_URL
        }
        // return BuildConfig.BASE_URL
    }
}
