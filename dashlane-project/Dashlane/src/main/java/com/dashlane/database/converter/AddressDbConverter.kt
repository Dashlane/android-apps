package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.AddressSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAddress
import com.dashlane.vault.util.getCountry
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country

object AddressDbConverter : DbConverter.Delegate<SyncObject.Address> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Address> {
        return createAddress(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            name = c.getString(AddressSql.FIELD_NAME),
            receiver = c.getString(AddressSql.FIELD_RECEIVER),
            full = c.getString(AddressSql.FIELD_FULL),
            city = c.getString(AddressSql.FIELD_CITY) ?: "",
            zipCode = c.getString(AddressSql.FIELD_ZIPCODE),
            state = c.getString(AddressSql.FIELD_STATE),
            addressCountry = c.getString(AddressSql.FIELD_COUNTRY)?.let(Country::forIsoCodeOrNull),
            streetNumber = c.getString(AddressSql.FIELD_STREET_NUMBER),
            streetTitle = c.getString(AddressSql.FIELD_STREET_TITLE),
            streetName = c.getString(AddressSql.FIELD_STREET_NAME) ?: "",
            stateNumber = c.getString(AddressSql.FIELD_STATE_NUMBER),
            stateLevel2 = c.getString(AddressSql.FIELD_STATE_LEVEL_2),
            building = c.getString(AddressSql.FIELD_BUILDING),
            floor = c.getString(AddressSql.FIELD_FLOOR),
            door = c.getString(AddressSql.FIELD_DOOR),
            digitCode = c.getString(AddressSql.FIELD_DIGIT_CODE),
            linkedPhone = c.getString(AddressSql.FIELD_LINKED_PHONE)
        )
    }

    override fun syncObjectType() = SyncObjectType.ADDRESS

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Address>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Address>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(AddressSql.FIELD_NAME, item.addressName)
        cv.put(AddressSql.FIELD_RECEIVER, item.receiver)
        cv.put(AddressSql.FIELD_FULL, item.addressFull)
        cv.put(AddressSql.FIELD_CITY, item.city)
        cv.put(AddressSql.FIELD_ZIPCODE, item.zipCode)
        cv.put(AddressSql.FIELD_STATE, item.state)
        cv.put(AddressSql.FIELD_COUNTRY, item.getCountry().isoCode)
        cv.put(AddressSql.FIELD_STREET_NUMBER, item.streetNumber)
        cv.put(AddressSql.FIELD_STREET_TITLE, item.streetNumber)
        cv.put(AddressSql.FIELD_STREET_NAME, item.streetName)
        cv.put(AddressSql.FIELD_STATE_NUMBER, item.stateNumber)
        cv.put(AddressSql.FIELD_STATE_LEVEL_2, item.stateLevel2)
        cv.put(AddressSql.FIELD_BUILDING, item.building)
        cv.put(AddressSql.FIELD_FLOOR, item.floor)
        cv.put(AddressSql.FIELD_DOOR, item.door)
        cv.put(AddressSql.FIELD_DIGIT_CODE, item.digitCode)
        cv.put(AddressSql.FIELD_LINKED_PHONE, item.linkedPhone)

        return cv
    }
}
