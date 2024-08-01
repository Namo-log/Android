package com.mongmong.namo.presentation.config

import android.util.Log
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
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
            if (task.isSuccessful){
                val updated = task.result
                Log.d("TAG","Config params updated success: $updated")
            } else {
                Log.d("TAG", "Config params updated failed: $updated")
            }
        }
        return remoteConfig.getString("android_baseurl")
    }
}