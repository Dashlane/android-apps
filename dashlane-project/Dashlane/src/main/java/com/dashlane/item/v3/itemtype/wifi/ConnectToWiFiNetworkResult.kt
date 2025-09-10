package com.dashlane.item.v3.itemtype.wifi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContract

class ConnectToWiFiNetworkResult : ActivityResultContract<WifiNetworkSuggestion, ConnectToWiFiNetworkResultCode>() {
    override fun createIntent(context: Context, network: WifiNetworkSuggestion): Intent {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val bundle = Bundle()
            bundle.putParcelableArrayList(Settings.EXTRA_WIFI_NETWORK_LIST, arrayListOf(network))
            val intent = Intent(Settings.ACTION_WIFI_ADD_NETWORKS)
            intent.putExtras(bundle)

            return intent
        } else {
            error("ConnectToWiFiNetwork is only supported on Android R and above")
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): ConnectToWiFiNetworkResultCode {
        return if (resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                val suggestions = intent?.getIntegerArrayListExtra(Settings.EXTRA_WIFI_NETWORK_RESULT_LIST)
                if (suggestions?.size != 1) {
                    error("More than one suggestion result")
                }

                val suggestion = suggestions.first()

                when (suggestion) {
                    Settings.ADD_WIFI_RESULT_SUCCESS -> ConnectToWiFiNetworkResultCode.SUCCESS
                    Settings.ADD_WIFI_RESULT_ADD_OR_UPDATE_FAILED -> ConnectToWiFiNetworkResultCode.FAILED
                    Settings.ADD_WIFI_RESULT_ALREADY_EXISTS -> ConnectToWiFiNetworkResultCode.ALREADY_EXISTS
                    else -> ConnectToWiFiNetworkResultCode.FAILED
                }
            } else {
                error("ConnectToWiFiNetwork is only supported on Android R and above")
            }
        } else {
            ConnectToWiFiNetworkResultCode.CANCELLED
        }
    }
}

enum class ConnectToWiFiNetworkResultCode {
    CANCELLED,
    SUCCESS,
    ALREADY_EXISTS,
    FAILED,
}