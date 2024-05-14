package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject
import java.time.LocalDate

fun createIdCard(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    deliveryDate: LocalDate? = null,
    expireDate: LocalDate? = null,
    fullname: String? = null,
    sex: SyncObject.Gender? = null,
    dateOfBirth: LocalDate? = null,
    number: String? = null,
    linkedIdentity: String? = null
): VaultItem<SyncObject.IdCard> {
    return dataIdentifier.toVaultItem(
        SyncObject.IdCard {
            this.deliveryDate = deliveryDate
            this.expireDate = expireDate
            this.fullname = fullname
            this.sex = sex
            this.dateOfBirth = dateOfBirth
            this.number = number
            this.linkedIdentity = linkedIdentity

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        }
    )
}

fun VaultItem<SyncObject.IdCard>.copySyncObject(builder: SyncObject.IdCard.Builder.() -> Unit = {}):
        VaultItem<SyncObject.IdCard> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
