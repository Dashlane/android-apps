package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.IdCardSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createIdCard
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object IdCardDbConverter : DbConverter.Delegate<SyncObject.IdCard> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.IdCard> {
        val sex = c.getString(IdCardSql.FIELD_SEX)?.let {
            when (it.toIntOrNull()) {
                0 -> SyncObject.Gender.MALE
                1 -> SyncObject.Gender.FEMALE
                else -> null
            }
        }
        return createIdCard(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            fullname = c.getString(IdCardSql.FIELD_FULLNAME),
            dateOfBirth = TimedDocumentDbConverter.getLocalDate(c, IdCardSql.FIELD_DATE_OF_BIRTH),
            sex = sex,
            number = c.getString(IdCardSql.FIELD_NUMBER),
            linkedIdentity = c.getString(IdCardSql.FIELD_LINKED_IDENTITY),
            expireDate = c.getExpireDateField(),
            deliveryDate = c.getDeliveryDateField()
        )
    }

    override fun syncObjectType() = SyncObjectType.ID_CARD

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.IdCard>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.IdCard>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        val genderValue = when (item.sex) {
            SyncObject.Gender.MALE -> "0"
            SyncObject.Gender.FEMALE -> "1"
            else -> null
        }
        TimedDocumentDbConverter.putDeliveryDate(cv, item.deliveryDate)
        TimedDocumentDbConverter.putExpireDate(cv, item.expireDate)
        cv.put(IdCardSql.FIELD_FULLNAME, item.fullname)
        cv.put(IdCardSql.FIELD_SEX, genderValue)
        TimedDocumentDbConverter.putLocalDate(cv, IdCardSql.FIELD_DATE_OF_BIRTH, item.dateOfBirth)
        cv.put(IdCardSql.FIELD_NUMBER, item.number)
        cv.put(IdCardSql.FIELD_LINKED_IDENTITY, item.linkedIdentity)
        return cv
    }
}
