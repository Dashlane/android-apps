package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createFiscalStatement(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    fiscalNumber: String? = null,
    teleDeclarantNumber: String? = null,
    fullname: String? = null,
    linkedIdentity: String? = null
): VaultItem<SyncObject.FiscalStatement> {
    return dataIdentifier.toVaultItem(
        SyncObject.FiscalStatement {
            this.fiscalNumber = fiscalNumber
            this.teledeclarantNumber = teleDeclarantNumber
            this.fullname = fullname
            this.linkedIdentity = linkedIdentity

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.FiscalStatement>.copySyncObject(builder: SyncObject.FiscalStatement.Builder.() -> Unit = {}):
        VaultItem<SyncObject.FiscalStatement> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
