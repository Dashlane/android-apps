package com.dashlane.item.v3.itemtype.wifi.builder

import javax.inject.Inject

class WiFiQRCodeContentBuilder @Inject constructor() {
    operator fun invoke(
        ssid: String,
        passphrase: String?,
    ): String = if (passphrase == null) {
        "WIFI:S:$ssid;;"
    } else {
        "WIFI:T:WPA;S:$ssid;P:$passphrase;;"
    }
}