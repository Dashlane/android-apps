package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.PaymentPaypalSql
import com.dashlane.util.getString
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createPaymentPaypal
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object PaymentPaypalDbConverter : DbConverter.Delegate<SyncObject.PaymentPaypal> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.PaymentPaypal> {
        return createPaymentPaypal(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            login = c.getString(PaymentPaypalSql.FIELD_LOGIN),
            password = c.getString(PaymentPaypalSql.FIELD_PASSWORD)?.let { SyncObfuscatedValue(it) },
            name = c.getString(PaymentPaypalSql.FIELD_NAME)
        )
    }

    override fun syncObjectType() = SyncObjectType.PAYMENT_PAYPAL

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.PaymentPaypal>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.PaymentPaypal>): ContentValues? {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(PaymentPaypalSql.FIELD_NAME, item.name)
        cv.put(PaymentPaypalSql.FIELD_LOGIN, item.login)
        cv.put(PaymentPaypalSql.FIELD_PASSWORD, item.password?.toString())
        return cv
    }
}
