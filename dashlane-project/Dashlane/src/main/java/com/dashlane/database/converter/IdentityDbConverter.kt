package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.IdentitySql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createIdentity
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object IdentityDbConverter : DbConverter.Delegate<SyncObject.Identity> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Identity> {
        return createIdentity(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            title = c.getString(IdentitySql.FIELD_TITLE)
                ?.let { title -> enumValues<SyncObject.Identity.Title>().firstOrNull { it.value == title } },
            type = c.getString(IdentitySql.FIELD_TYPE),
            firstname = c.getString(IdentitySql.FIELD_FIRSTNAME),
            lastname = c.getString(IdentitySql.FIELD_LASTNAME),
            middlename = c.getString(IdentitySql.FIELD_MIDDLENAME),
            pseudo = c.getString(IdentitySql.FIELD_PSEUDO),
            dateOfBirth = TimedDocumentDbConverter.getLocalDate(c, IdentitySql.FIELD_DATE_OF_BIRTH),
            placeOfBirth = c.getString(IdentitySql.FIELD_PLACE_OF_BIRTH)
        )
    }

    override fun syncObjectType() = SyncObjectType.IDENTITY

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Identity>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Identity>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(IdentitySql.FIELD_TYPE, item.type)
        cv.put(IdentitySql.FIELD_TITLE, item.title?.value)
        cv.put(IdentitySql.FIELD_FIRSTNAME, item.firstName)
        cv.put(IdentitySql.FIELD_LASTNAME, item.lastName)
        cv.put(IdentitySql.FIELD_MIDDLENAME, item.middleName)
        cv.put(IdentitySql.FIELD_PSEUDO, item.pseudo)
        TimedDocumentDbConverter.putLocalDate(cv, IdentitySql.FIELD_DATE_OF_BIRTH, item.birthDate)
        cv.put(IdentitySql.FIELD_PLACE_OF_BIRTH, item.birthPlace)

        return cv
    }
}
