package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.CompanySql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createCompany
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object CompanyDbConverter : DbConverter.Delegate<SyncObject.Company> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.Company> {
        return createCompany(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            jobtitle = c.getString(CompanySql.FIELD_JOBTITLE),
            nafcode = c.getString(CompanySql.FIELD_NAFCODE),
            name = c.getString(CompanySql.FIELD_NAME),
            siren = c.getString(CompanySql.FIELD_SIREN),
            siret = c.getString(CompanySql.FIELD_SIRET),
            tvaNumber = c.getString(CompanySql.FIELD_TVANUMBER)
        )
    }

    override fun syncObjectType() = SyncObjectType.COMPANY

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.Company>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.Company>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(CompanySql.FIELD_NAME, item.name)
        cv.put(CompanySql.FIELD_JOBTITLE, item.jobTitle)
        cv.put(CompanySql.FIELD_SIRET, item.siretNumber)
        cv.put(CompanySql.FIELD_SIREN, item.sirenNumber)
        cv.put(CompanySql.FIELD_TVANUMBER, item.tvaNumber)
        cv.put(CompanySql.FIELD_NAFCODE, item.nafCode)
        return cv
    }
}
