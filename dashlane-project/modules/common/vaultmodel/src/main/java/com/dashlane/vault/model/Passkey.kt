package com.dashlane.vault.model

import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject

fun createPasskey(
    itemId: String = generateUniqueIdentifier(),
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(uid = itemId),
    counter: Long = 0,
    privateKey: SyncObject.Passkey.PrivateKey? = null,
    credentialId: String? = null,
    rpName: String? = null,
    rpId: String? = null,
    itemName: String? = null,
    userHandle: String? = null,
    userDisplayName: String? = null,
    keyAlgorithm: Long = 0
): VaultItem<SyncObject.Passkey> {
    return dataIdentifier.toVaultItem(
        SyncObject.Passkey {
            this.counter = counter
            this.privateKey = privateKey
            this.credentialId = credentialId
            this.rpName = rpName
            this.rpId = rpId
            this.itemName = itemName
            this.keyAlgorithm = keyAlgorithm
            this.userHandle = userHandle
            this.userDisplayName = userDisplayName
            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

val SyncObject.Passkey.urlForGoToWebsite: String?
    get() = urlForGoToWebsite(rpId)

val SummaryObject.Passkey.urlForGoToWebsite: String?
    get() = urlForGoToWebsite(rpId)

private fun urlForGoToWebsite(rpId: String?): String? =
    rpId?.toUrlOrNull(defaultSchemeHttp = true)?.toString()

val SyncObject.Passkey.title: String?
    get() = getTitle(itemName = itemName, rpId = rpId)

val SummaryObject.Passkey.title: String?
    get() = getTitle(itemName = itemName, rpId = rpId)

private fun getTitle(itemName: String?, rpId: String?): String? =
    itemName?.removeSurrounding("\"")?.takeIf { it.isNotSemanticallyNull() }
        ?: rpId?.toUrlOrNull()?.host?.takeIf { it.isNotSemanticallyNull() }

fun VaultItem<SyncObject.Passkey>.copySyncObject(builder: SyncObject.Passkey.Builder.() -> Unit = {}):
    VaultItem<SyncObject.Passkey> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}