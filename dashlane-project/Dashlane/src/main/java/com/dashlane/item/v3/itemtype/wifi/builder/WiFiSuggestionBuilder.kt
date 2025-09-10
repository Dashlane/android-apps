package com.dashlane.item.v3.itemtype.wifi.builder

import android.net.wifi.WifiNetworkSuggestion
import javax.inject.Inject

class WiFiSuggestionBuilder @Inject constructor() {
    fun buildSuggestion(
        ssid: String,
        passphrase: String?,
    ): WifiNetworkSuggestion =
        WifiNetworkSuggestion.Builder()
            .setSsid(ssid)
            .apply {
                if (passphrase != null) {
                    setWpa2Passphrase(passphrase)
                }
            }
            .build()
}