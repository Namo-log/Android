package com.mongmong.namo.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import com.mongmong.namo.data.remote.diary.NetworkChecker

class NetworkCheckerImpl(private val context: Context) : NetworkChecker {
    override fun isOnline(): Boolean {
        val connectivityManager : ConnectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val network : Network = connectivityManager.activeNetwork ?: return false
        val actNetwork : NetworkCapabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

        return when {
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true // 데이터
            actNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true // 와이파이
            else -> false
        }
    }
}