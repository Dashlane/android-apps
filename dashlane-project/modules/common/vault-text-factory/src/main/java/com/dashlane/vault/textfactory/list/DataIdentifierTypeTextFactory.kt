package com.dashlane.vault.textfactory.list

import android.annotation.SuppressLint
import com.dashlane.vault.textfactory.R
import com.dashlane.xml.domain.SyncObjectType

object DataIdentifierTypeTextFactory {
    @SuppressLint("SwitchIntDef")
    fun getStringResId(syncObjectType: SyncObjectType): Int {
        return when (syncObjectType) {
            SyncObjectType.ADDRESS -> R.string.datatype_address
            SyncObjectType.AUTH_CATEGORY -> R.string.datatype_auth_category
            SyncObjectType.AUTHENTIFIANT -> R.string.datatype_authentifiant
            SyncObjectType.COMPANY -> R.string.datatype_company
            SyncObjectType.DRIVER_LICENCE -> R.string.datatype_driver_licence
            SyncObjectType.EMAIL -> R.string.datatype_email
            SyncObjectType.FISCAL_STATEMENT -> R.string.datatype_fiscal_statement
            SyncObjectType.ID_CARD -> R.string.datatype_id_card
            SyncObjectType.IDENTITY -> R.string.datatype_identity
            SyncObjectType.PASSPORT -> R.string.datatype_passport
            SyncObjectType.PAYMENT_CREDIT_CARD -> R.string.datatype_paymentcreditcard
            SyncObjectType.PERSONAL_WEBSITE -> R.string.datatype_personal_website
            SyncObjectType.PHONE -> R.string.datatype_phone
            SyncObjectType.SOCIAL_SECURITY_STATEMENT -> R.string.datatype_social_security_statement
            SyncObjectType.SECURE_NOTE -> R.string.datatype_secure_note
            SyncObjectType.SECURE_NOTE_CATEGORY -> R.string.datatype_secure_note_category
            SyncObjectType.BANK_STATEMENT -> R.string.datatype_bank_statement
            SyncObjectType.PASSKEY -> R.string.datatype_passkey
            else -> 0
        }
    }
}