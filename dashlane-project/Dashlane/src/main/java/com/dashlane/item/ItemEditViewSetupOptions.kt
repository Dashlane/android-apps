package com.dashlane.item

import android.content.Intent
import android.os.Bundle
import com.dashlane.authenticator.Otp
import com.dashlane.util.isSemanticallyNull
import com.dashlane.xml.domain.SyncObjectType

data class ItemEditViewSetupOptions(
    val dataType: SyncObjectType,
    val uid: String?,
    val websiteUrl: String?,
    val toolbarCollapsed: Boolean,
    val forceEdit: Boolean,
    val successIntent: Intent?,
    val savedScreenConfiguration: Bundle?,
    val savedAdditionalData: Bundle?,
    val scannedOtp: Otp?
) {
    val editMode: Boolean = forceEdit || uid.isSemanticallyNull() || dataType == SyncObjectType.SECURE_NOTE
}