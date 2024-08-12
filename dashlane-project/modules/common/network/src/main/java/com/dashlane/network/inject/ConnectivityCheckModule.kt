package com.dashlane.network.inject

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.os.Build
import com.dashlane.server.api.ConnectivityCheck
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Provider
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ConnectivityCheckModule {

    @Singleton
    @Provides
    fun getConnectivityCheck(connectivityManagerProvider: Provider<ConnectivityManager>): ConnectivityCheck {
        return object : ConnectivityCheck {

            private val connectivityManager: ConnectivityManager =
                connectivityManagerProvider.get()

            override val isOnline: Boolean
                get() = if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.O_MR1) {
                    @Suppress("DEPRECATION")
                    connectivityManager.allNetworks.any { it.hasInternet }
                } else {
                    connectivityManager.activeNetwork?.hasInternet ?: false
                }

            private val Network?.hasInternet
                get() = networkCapabilities.let { it != null && it.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) }

            private val Network?.networkCapabilities
                get() =
                    try {
                        connectivityManager.getNetworkCapabilities(this)
                    } catch (e: SecurityException) {
                        
                        
                        null
                    }
        }
    }
}