package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.PersonalWebsiteSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createPersonalWebsite
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object PersonalWebsiteDbConverter : DbConverter.Delegate<SyncObject.PersonalWebsite> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.PersonalWebsite> {
        return createPersonalWebsite(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            website = c.getString(PersonalWebsiteSql.FIELD_WEBSITE),
            name = c.getString(PersonalWebsiteSql.FIELD_NAME)
        )
    }

    override fun syncObjectType() = SyncObjectType.PERSONAL_WEBSITE

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.PersonalWebsite>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.PersonalWebsite>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(PersonalWebsiteSql.FIELD_WEBSITE, item.website ?: "")
        cv.put(PersonalWebsiteSql.FIELD_NAME, item.name ?: "")
        return cv
    }
}
