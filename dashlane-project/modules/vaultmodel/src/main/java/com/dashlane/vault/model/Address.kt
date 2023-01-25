package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.utils.Country

fun createAddress(
    dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    name: String? = null,
    receiver: String? = null,
    full: String? = null,
    city: String = "",
    zipCode: String? = null,
    state: String? = null,
    addressCountry: Country? = null,
    streetNumber: String? = null,
    streetTitle: String? = null,
    streetName: String = "",
    stateNumber: String? = null,
    stateLevel2: String? = null,
    building: String? = null,
    floor: String? = null,
    door: String? = null,
    digitCode: String? = null,
    linkedPhone: String? = null
): VaultItem<SyncObject.Address> {
    return dataIdentifier.toVaultItem(
        SyncObject.Address {
            this.addressName = name
            this.receiver = receiver
            this.addressFull = full
            this.city = city
            this.zipCode = zipCode
            this.state = state
            this.country = addressCountry
            this.streetNumber = streetNumber
            this.streetTitle = streetTitle
            this.streetName = streetName
            this.stateNumber = stateNumber
            this.stateLevel2 = stateLevel2
            this.building = building
            this.floor = floor
            this.door = door
            this.digitCode = digitCode
            this.linkedPhone = linkedPhone

            this.setCommonDataIdentifierAttrs(dataIdentifier)
        })
}

fun VaultItem<SyncObject.Address>.copySyncObject(builder: SyncObject.Address.Builder.() -> Unit = {}):
        VaultItem<SyncObject.Address> {
    return this.copy(syncObject = this.syncObject.copy(builder))
}
