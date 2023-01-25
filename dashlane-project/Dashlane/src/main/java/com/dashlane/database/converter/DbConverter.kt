package com.dashlane.database.converter

import android.content.ContentValues
import android.database.Cursor
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.SyncObjectType.ADDRESS
import com.dashlane.xml.domain.SyncObjectType.AUTHENTIFIANT
import com.dashlane.xml.domain.SyncObjectType.AUTH_CATEGORY
import com.dashlane.xml.domain.SyncObjectType.BANK_STATEMENT
import com.dashlane.xml.domain.SyncObjectType.COMPANY
import com.dashlane.xml.domain.SyncObjectType.DATA_CHANGE_HISTORY
import com.dashlane.xml.domain.SyncObjectType.DRIVER_LICENCE
import com.dashlane.xml.domain.SyncObjectType.EMAIL
import com.dashlane.xml.domain.SyncObjectType.FISCAL_STATEMENT
import com.dashlane.xml.domain.SyncObjectType.GENERATED_PASSWORD
import com.dashlane.xml.domain.SyncObjectType.IDENTITY
import com.dashlane.xml.domain.SyncObjectType.ID_CARD
import com.dashlane.xml.domain.SyncObjectType.PASSPORT
import com.dashlane.xml.domain.SyncObjectType.PAYMENT_CREDIT_CARD
import com.dashlane.xml.domain.SyncObjectType.PAYMENT_PAYPAL
import com.dashlane.xml.domain.SyncObjectType.PERSONAL_WEBSITE
import com.dashlane.xml.domain.SyncObjectType.PHONE
import com.dashlane.xml.domain.SyncObjectType.SECURE_FILE_INFO
import com.dashlane.xml.domain.SyncObjectType.SECURE_NOTE
import com.dashlane.xml.domain.SyncObjectType.SECURE_NOTE_CATEGORY
import com.dashlane.xml.domain.SyncObjectType.SECURITY_BREACH
import com.dashlane.xml.domain.SyncObjectType.SETTINGS
import com.dashlane.xml.domain.SyncObjectType.SOCIAL_SECURITY_STATEMENT
import com.dashlane.xml.domain.SyncObjectType.PASSKEY
import com.dashlane.xml.domain.objectType



object DbConverter {

    

    @JvmStatic
    fun fromCursorToList(c: Cursor, type: SyncObjectType): List<VaultItem<*>> {
        val converter = getConverter<SyncObject>(type) ?: return listOf()
        val list = mutableListOf<VaultItem<*>>()
        if (c.moveToFirst()) {
            do {
                list.add(converter.cursorToItem(c))
            } while (c.moveToNext())
        }
        return list
    }

    @JvmStatic
    fun fromCursor(c: Cursor, type: SyncObjectType): VaultItem<*>? {
        
        if (c.position < 0 && !c.moveToFirst()) {
            return null
        }
        val converter = getConverter<SyncObject>(type) ?: return null
        return converter.cursorToItem(c)
    }

    

    @JvmStatic
    fun <T : SyncObject> toContentValues(item: VaultItem<T>?): ContentValues? {
        item ?: return null
        val converter = getConverter<T>(item.syncObject.objectType) ?: return null
        return converter.toContentValues(item)
    }

    private fun <T : SyncObject> getConverter(type: SyncObjectType): Delegate<T>? {
        @Suppress("UNCHECKED_CAST")
        return when (type) {
            ADDRESS -> AddressDbConverter
            AUTH_CATEGORY -> AuthCategoryDbConverter
            AUTHENTIFIANT -> AuthentifiantDbConverter
            BANK_STATEMENT -> BankStatementDbConverter
            COMPANY -> CompanyDbConverter
            DATA_CHANGE_HISTORY -> error("DATA_CHANGE_HISTORY cannot be saved to DB like this.")
            DRIVER_LICENCE -> DriverLicenceDbConverter
            EMAIL -> EmailDbConverter
            FISCAL_STATEMENT -> FiscalStatementDbConverter
            GENERATED_PASSWORD -> GeneratedPasswordDbConverter
            ID_CARD -> IdCardDbConverter
            IDENTITY -> IdentityDbConverter
            PASSPORT -> PassportDbConverter
            PAYMENT_CREDIT_CARD -> PaymentCreditCardDbConverter
            PAYMENT_PAYPAL -> PaymentPaypalDbConverter
            PERSONAL_WEBSITE -> PersonalWebsiteDbConverter
            PHONE -> PhoneDbConverter
            SECURE_NOTE -> SecureNoteDbConverter
            SECURE_NOTE_CATEGORY -> SecureNoteCategoryDbConverter
            SOCIAL_SECURITY_STATEMENT -> SocialSecurityStatementDbConverter
            SECURE_FILE_INFO -> SecureFileInfoDbConverter
            SECURITY_BREACH -> SecurityBreachDbConverter
            PASSKEY -> error("Passkeys are not supported yet")
            SETTINGS -> error("Settings cannot be saved to DB. Use settings manager instead.")
        } as Delegate<T>
    }

    interface Delegate<T : SyncObject> {
        fun cursorToItem(c: Cursor): VaultItem<T>
        fun toContentValues(vaultItem: VaultItem<T>): ContentValues?
        fun syncObjectType(): SyncObjectType
    }
}
