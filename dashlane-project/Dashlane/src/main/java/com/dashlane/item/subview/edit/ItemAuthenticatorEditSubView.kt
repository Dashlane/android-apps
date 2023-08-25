package com.dashlane.item.subview.edit

import com.dashlane.R
import com.dashlane.authenticator.Otp
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject

class ItemAuthenticatorEditSubView(
    val credentialName: String,
    val itemId: String,
    val topDomain: String?,
    val linkedServices: SummaryObject.LinkedServices?,
    val professional: Boolean,
    override var value: Otp?,
    valueUpdate: (VaultItem<*>, Otp?) -> VaultItem<*>?
) : ItemEditValueSubView<Otp?>(valueUpdate) {
    val title: Int
        get() = if (value == null) R.string.authenticator_item_edit_activate_title else R.string.authenticator_item_edit_activated_title
}