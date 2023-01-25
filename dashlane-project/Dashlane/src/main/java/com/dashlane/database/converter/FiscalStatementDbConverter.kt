package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.FiscalStatementSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createFiscalStatement
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object FiscalStatementDbConverter : DbConverter.Delegate<SyncObject.FiscalStatement> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.FiscalStatement> {
        return createFiscalStatement(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            fiscalNumber = c.getString(FiscalStatementSql.FIELD_FISCAL_NUMBER),
            teleDeclarantNumber = c.getString(FiscalStatementSql.FIELD_TELEDECLARANT_NUMBER),
            linkedIdentity = c.getString(FiscalStatementSql.FIELD_LINKED_IDENTITY),
            fullname = c.getString(FiscalStatementSql.FIELD_FULLNAME),
        )
    }

    override fun syncObjectType() = SyncObjectType.FISCAL_STATEMENT

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.FiscalStatement>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.FiscalStatement>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(FiscalStatementSql.FIELD_FISCAL_NUMBER, item.fiscalNumber)
        cv.put(FiscalStatementSql.FIELD_TELEDECLARANT_NUMBER, item.teledeclarantNumber)
        cv.put(FiscalStatementSql.FIELD_LINKED_IDENTITY, item.linkedIdentity)
        cv.put(FiscalStatementSql.FIELD_FULLNAME, item.fullname)
        return cv
    }
}
