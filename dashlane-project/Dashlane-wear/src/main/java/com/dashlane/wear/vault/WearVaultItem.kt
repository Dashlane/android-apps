package com.dashlane.wear.vault

import com.dashlane.authenticator.Totp
import com.dashlane.vault.model.getUrlDisplayName
import com.dashlane.vault.model.title
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject

data class WearVaultItem(
    val id: String,
    val title: String,
    val username: String,
    val url: String,
    val urlForUi: String,
    val isPasskey: Boolean,
    val totp: Totp? = null,
)

internal fun SummaryObject.toWearVaultItem(totp: Totp? = null) = when (this) {
    is SummaryObject.Passkey -> toWearVaultItem()
    is SummaryObject.Authentifiant -> toWearVaultItem(totp)
    else -> null
}

private fun SummaryObject.Authentifiant.toWearVaultItem(totp: Totp? = null) = WearVaultItem(
    id = this.id,
    title = title ?: "",
    username = login ?: email ?: "",
    url = url ?: "",
    urlForUi = urlForUI() ?: "",
    isPasskey = false,
    totp = totp,
)

private fun SummaryObject.Passkey.toWearVaultItem() = WearVaultItem(
    id = this.id,
    title = title ?: "",
    username = userDisplayName ?: "",
    url = rpId ?: "",
    urlForUi = rpId?.getUrlDisplayName ?: "",
    isPasskey = true,
)