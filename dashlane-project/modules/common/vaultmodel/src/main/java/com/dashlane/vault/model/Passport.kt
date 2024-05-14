package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

fun createPassport(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    deliveryDate: LocalDate? = null,
    expireDate: LocalDate? = null,
    number: String? = null,
    fullname: String? = null,
    sex: SyncObject.Gender? = null,
    dateOfBirth: LocalDate? = null,
    deliveryPlace: String? = null,
    linkedIdentity: String? = null
): VaultItem<SyncObject.Passport> {
    return dataIdentifier.toVaultItem(
        SyncObject.Passport {
            this.deliveryDate = deliveryDate
            this.expireDate = expireDate
            this.number = number
            this.fullname = fullname
            this.sex = sex
            this.dateOfBirth = dateOfBirth
            this.deliveryPlace = deliveryPlace
            this.linkedIdentity = linkedIdentity

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

fun VaultItem<SyncObject.Passport>.copySyncObject(builder: SyncObject.Passport.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Passport> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
