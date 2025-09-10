package com.dashlane.item.v3.itemtype.wifi

import com.dashlane.item.v3.viewmodels.ItemEditDialog

sealed class WiFiDialog : ItemEditDialog {
    data object LocationPermissionDenied : WiFiDialog()
}