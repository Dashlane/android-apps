package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.database.sql.PaymentCreditCardSql
import com.dashlane.util.getString
import com.dashlane.util.obfuscated.toSyncObfuscatedValue
import com.dashlane.util.time.toMonthOrNull
import com.dashlane.util.time.toYearOrNull
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createPaymentCreditCard
import com.dashlane.xml.SyncObjectEnum
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object PaymentCreditCardDbConverter : DbConverter.Delegate<SyncObject.PaymentCreditCard> {
    @JvmStatic
    fun getItemFromCursor(c: Cursor) = cursorToItem(c)

    override fun cursorToItem(c: Cursor): VaultItem<SyncObject.PaymentCreditCard> {
        return createPaymentCreditCard(
            dataIdentifier = DataIdentifierDbConverter.loadDataIdentifier(c, syncObjectType()),
            billingAddress = c.getString(PaymentCreditCardSql.FIELD_BILLING_ADDRESS),
            cardNumber = c.getString(PaymentCreditCardSql.FIELD_CARD_NUMBER)?.toSyncObfuscatedValue(),
            name = c.getString(PaymentCreditCardSql.FIELD_NAME),
            bank = CreditCardBank(c.getString(PaymentCreditCardSql.FIELD_BANK)),
            color = c.getString(PaymentCreditCardSql.FIELD_COLOR)?.let { SyncObjectEnum.getEnumForValue(it) }
                ?: SyncObject.PaymentCreditCard.Color.NO_TYPE,
            expireMonth = c.getString(PaymentCreditCardSql.FIELD_EXP_MONTH)?.toMonthOrNull(),
            expireYear = c.getString(PaymentCreditCardSql.FIELD_EXP_YEAR)?.toYearOrNull(),
            startMonth = c.getString(PaymentCreditCardSql.FIELD_START_MONTH)?.toMonthOrNull(),
            startYear = c.getString(PaymentCreditCardSql.FIELD_START_YEAR)?.toYearOrNull(),
            owner = c.getString(PaymentCreditCardSql.FIELD_OWNER),
            securityCode = c.getString(PaymentCreditCardSql.FIELD_SEC_CODE),
            issueNumber = c.getString(PaymentCreditCardSql.FIELD_ISSUE_NUM),
            cardNote = c.getString(PaymentCreditCardSql.FIELD_NOTE)
        )
    }

    override fun syncObjectType() = SyncObjectType.PAYMENT_CREDIT_CARD

    @JvmStatic
    fun getContentValues(item: VaultItem<SyncObject.PaymentCreditCard>) = toContentValues(item)

    override fun toContentValues(vaultItem: VaultItem<SyncObject.PaymentCreditCard>): ContentValues {
        val item = vaultItem.syncObject
        val cv = DataIdentifierDbConverter.getContentValues(vaultItem)

        cv.put(PaymentCreditCardSql.FIELD_CARD_NUMBER, item.cardNumber?.toString())
        cv.put(PaymentCreditCardSql.FIELD_OWNER, item.ownerName)
        cv.put(PaymentCreditCardSql.FIELD_BANK, item.bank ?: "")
        cv.put(PaymentCreditCardSql.FIELD_SEC_CODE, item.securityCode?.toString())
        cv.put(PaymentCreditCardSql.FIELD_EXP_MONTH, item.expireMonth?.value?.toString())
        cv.put(PaymentCreditCardSql.FIELD_EXP_YEAR, item.expireYear?.value?.toString())
        cv.put(PaymentCreditCardSql.FIELD_START_MONTH, item.startMonth?.value?.toString())
        cv.put(PaymentCreditCardSql.FIELD_START_YEAR, item.startYear?.value?.toString())
        cv.put(PaymentCreditCardSql.FIELD_NAME, item.name)
        cv.put(PaymentCreditCardSql.FIELD_NOTE, item.cCNote?.toString())
        cv.put(PaymentCreditCardSql.FIELD_ISSUE_NUM, item.issueNumber)
        cv.put(PaymentCreditCardSql.FIELD_BILLING_ADDRESS, item.linkedBillingAddress)
        cv.put(PaymentCreditCardSql.FIELD_COLOR, item.color?.value)
        return cv
    }
}
