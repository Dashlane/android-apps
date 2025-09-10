package com.dashlane.util

import android.annotation.SuppressLint
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject

class NetworkStateProvider @Inject constructor(
    private val connectivityManager: ConnectivityManager?,
) {
    fun isOn(): Boolean = isInternetAvailable(connectivityManager)

    @SuppressLint("MissingPermission")
    private fun isInternetAvailable(connectivityManager: ConnectivityManager?): Boolean {
        val network = connectivityManager?.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}