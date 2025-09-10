package com.dashlane.item.v3.itemtype.wifi

import com.dashlane.item.v3.data.FormData

data class WiFiFormData(
    val ssid: String = "",
    val passphrase: String? = null,
    val isPassphraseEmpty: Boolean = true,
    val note: String = "",
    val isQRCodeBottomSheetVisible: Boolean = false,
    val qrCodeContent: String? = null,
    val suggestedNetworks: List<String> = emptyList(),
    val isAlreadyConnectedInfoBoxVisible: Boolean = false,
    val isConnectToNetworkButtonVisible: Boolean = false,
) : FormData