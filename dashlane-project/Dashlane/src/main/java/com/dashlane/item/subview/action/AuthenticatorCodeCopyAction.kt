package com.dashlane.item.subview.action

import android.app.Activity
import com.dashlane.R
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject

class AuthenticatorCodeCopyAction(
    summaryObject: SummaryObject,
    vaultItemCopy: VaultItemCopyService,
    action: (Activity) -> Unit = {}
) : CopyAction(summaryObject, CopyField.OtpCode, action, null, vaultItemCopy) {

    override val icon = -1
    override val text = R.string.copy
    override val tintColorRes: Int? = null
}