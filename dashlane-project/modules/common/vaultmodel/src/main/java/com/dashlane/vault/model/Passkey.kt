package com.dashlane.vault.model

import com.dashlane.url.root
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
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
    get() = toSummary<SummaryObject.Passkey>().urlForGoToWebsite

val SummaryObject.Passkey.urlForGoToWebsite: String?
    get() {
        return rpId?.toUrlOrNull(defaultSchemeHttp = true)?.toString()
    }

val SyncObject.Passkey.urlForUsageLog: String
    get() = toSummary<SummaryObject.Passkey>().urlForUsageLog

val SummaryObject.Passkey.urlForUsageLog: String
    get() {
        return rpId?.toUrlOrNull()?.root ?: "client__not_valid_url"
    }

val SyncObject.Passkey.title: String?
    get() = toSummary<SummaryObject.Passkey>().title

val SummaryObject.Passkey.title: String?
    get() = itemName?.removeSurrounding("\"")?.takeIf { it.isNotSemanticallyNull() }
        ?: rpId?.toUrlOrNull()?.host?.takeIf { it.isNotSemanticallyNull() }

fun VaultItem<SyncObject.Passkey>.copySyncObject(builder: SyncObject.Passkey.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Passkey> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}