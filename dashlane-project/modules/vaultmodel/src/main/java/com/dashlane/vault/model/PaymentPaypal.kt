package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

fun createPaymentPaypal(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    login: String? = null,
    password: SyncObfuscatedValue? = null
): VaultItem<SyncObject.PaymentPaypal> {
    return dataIdentifier.toVaultItem(
        SyncObject.PaymentPaypal {
            this.name = name
            this.login = login
            this.password = password

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

val SyncObject.PaymentPaypal.Companion.PAYPAL
    get() = "PayPal"

fun VaultItem<SyncObject.PaymentPaypal>.copySyncObject(builder: SyncObject.PaymentPaypal.Builder.() -> Unit = {}):
        VaultItem<SyncObject.PaymentPaypal> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
