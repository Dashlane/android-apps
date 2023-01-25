package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.AuthCategorySql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createAuthCategory
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object AuthCategoryDbConverter : DbConverter.Delegate<SyncObject.AuthCategory> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.AuthCategory> {
        return createAuthCategory(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            name = c.getString(AuthCategorySql.FIELD_NAME)
        )
    }

    override fun syncObjectType() = SyncObjectType.AUTH_CATEGORY

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.AuthCategory>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.AuthCategory>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(AuthCategorySql.FIELD_NAME, item.categoryName ?: "")
        return cv
    }
}
