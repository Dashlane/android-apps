package com.dashlane.vault.model

import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject

fun createBankStatement(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    owner: String? = null,
    bic: String? = null,
    iban: String? = null,
    bank: CreditCardBank? = null
): VaultItem<SyncObject.BankStatement> {
    return dataIdentifier.toVaultItem(
        SyncObject.BankStatement {
            this.bankAccountName = name
            this.bankAccountOwner = owner
            this.bankAccountBIC = bic.toSyncObfuscatedValue()
            this.bankAccountIBAN = iban.toSyncObfuscatedValue()
            this.bankAccountBank = bank?.bankDescriptor

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.BankStatement>.copySyncObject(builder: SyncObject.BankStatement.Builder.() -> Unit = {}):
        VaultItem<SyncObject.BankStatement> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
