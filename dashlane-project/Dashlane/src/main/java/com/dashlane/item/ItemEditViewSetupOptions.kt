package com.dashlane.item

import android.content.Intent
import android.os.Bundle
import com.dashlane.authenticator.Otp
import com.dashlane.useractivity.log.usage.UsageLogCode57
import com.dashlane.util.isSemanticallyNull
import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.util.desktopId
import com.dashlane.xml.domain.SyncObjectType



data class ItemEditViewSetupOptions(
    val dataType: SyncObjectType,
    val uid: String?,
    val websiteUrl: String?,
    val toolbarCollapsed: Boolean,
    val forceEdit: Boolean,
    val sender: UsageLogCode57.Sender?,
    val successIntent: Intent?,
    val savedScreenConfiguration: Bundle?,
    val savedAdditionalData: Bundle?,
    val scannedOtp: Otp?
) {
    val editMode: Boolean = forceEdit || uid.isSemanticallyNull() || dataType.desktopId == DataIdentifierId.SECURE_NOTE
}