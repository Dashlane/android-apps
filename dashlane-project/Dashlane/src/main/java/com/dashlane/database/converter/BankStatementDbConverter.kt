package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.BankStatementSql
import com.dashlane.database.sql.DataIdentifierSql
import com.dashlane.util.getString
import com.dashlane.util.tryOrNull
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createBankStatement
import com.dashlane.vault.model.signature
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object BankStatementDbConverter : DbConverter.Delegate<SyncObject.BankStatement> {

    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.BankStatement> {
        val bank: CreditCardBank? = tryOrNull {
            CreditCardBank(c.getString(BankStatementSql.FIELD_BANK))
        }
        return createBankStatement(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            owner = c.getString(BankStatementSql.FIELD_OWNER),
            name = c.getString(BankStatementSql.FIELD_NAME)?.trim(),
            bic = c.getString(BankStatementSql.FIELD_BIC),
            iban = c.getString(BankStatementSql.FIELD_IBAN),
            bank = bank
        )
    }

    override fun syncObjectType() = SyncObjectType.BANK_STATEMENT

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.BankStatement>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.BankStatement>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(BankStatementSql.FIELD_NAME, item.bankAccountName?.trim())
        cv.put(BankStatementSql.FIELD_OWNER, item.bankAccountOwner)
        cv.put(BankStatementSql.FIELD_BIC, item.bankAccountBIC?.toString())
        cv.put(BankStatementSql.FIELD_IBAN, item.bankAccountIBAN?.toString())
        cv.put(BankStatementSql.FIELD_BANK, item.bankAccountBank ?: "")
        cv.put(DataIdentifierSql.FIELD_LOCALE_LANG, item.localeFormat.signature)
        return cv
    }
}
