package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject

fun createCompany(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    jobtitle: String? = null,
    siret: String? = null,
    siren: String? = null,
    tvaNumber: String? = null,
    nafcode: String? = null
): VaultItem<SyncObject.Company> {
    return dataIdentifier.toVaultItem(
        SyncObject.Company {
            this.name = name
            this.jobTitle = jobtitle
            this.siretNumber = siret
            this.sirenNumber = siren
            this.tvaNumber = tvaNumber
            this.nafCode = nafcode

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

fun VaultItem<SyncObject.Company>.copySyncObject(builder: SyncObject.Company.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Company> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
