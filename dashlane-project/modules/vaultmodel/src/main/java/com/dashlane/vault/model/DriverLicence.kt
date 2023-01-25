package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

fun createDriverLicence(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    deliveryDate: LocalDate? = null,
    expireDate: LocalDate? = null,
    dateOfBirth: LocalDate? = null,
    fullName: String? = null,
    number: String? = null,
    sex: SyncObject.Gender? = null,
    state: String? = null,
    linkedIdentity: String? = null
): VaultItem<SyncObject.DriverLicence> {
    return dataIdentifier.toVaultItem(
        SyncObject.DriverLicence {
            this.deliveryDate = deliveryDate
            this.expireDate = expireDate
            this.dateOfBirth = dateOfBirth
            this.fullname = fullName
            this.number = number
            this.sex = sex
            this.state = state
            this.linkedIdentity = linkedIdentity

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.DriverLicence>.copySyncObject(builder: SyncObject.DriverLicence.Builder.() -> Unit = {}):
        VaultItem<SyncObject.DriverLicence> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
