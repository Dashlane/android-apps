package com.dashlane.vault.model

import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

fun createGeneratedPassword(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    authDomain: String? = null,
    generatedDate: String? = null,
    password: String? = null,
    authId: String? = null,
    platform: SyncObject.Platform? = null
): VaultItem<SyncObject.GeneratedPassword> {
    return dataIdentifier.toVaultItem(
        SyncObject.GeneratedPassword {
            this.domain = authDomain
            this.generatedDate = generatedDate?.toLongOrNull()
            this.password = password.toSyncObfuscatedValue()
            this.authId = authId
            this.platform = platform

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.GeneratedPassword>.addAuthId(authId: String): VaultItem<SyncObject.GeneratedPassword> {
    return this.copy(syncObject = this.syncObject.copy {
        this.authId = authId
    })
}
