package com.dashlane.vault.util

import com.dashlane.vault.model.DataIdentifierId
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.summary.toSummary
import com.dashlane.vault.model.VaultItem
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlin.reflect.KClass

val KClass<out SyncObject>.syncObjectType: SyncObjectType
    get() = when (this) {
        SyncObject.Address::class -> SyncObjectType.ADDRESS
        SyncObject.AuthCategory::class -> SyncObjectType.AUTH_CATEGORY
        SyncObject.Authentifiant::class -> SyncObjectType.AUTHENTIFIANT
        SyncObject.BankStatement::class -> SyncObjectType.BANK_STATEMENT
        SyncObject.Company::class -> SyncObjectType.COMPANY
        SyncObject.DataChangeHistory::class -> SyncObjectType.DATA_CHANGE_HISTORY
        SyncObject.DriverLicence::class -> SyncObjectType.DRIVER_LICENCE
        SyncObject.Email::class -> SyncObjectType.EMAIL
        SyncObject.FiscalStatement::class -> SyncObjectType.FISCAL_STATEMENT
        SyncObject.GeneratedPassword::class -> SyncObjectType.GENERATED_PASSWORD
        SyncObject.IdCard::class -> SyncObjectType.ID_CARD
        SyncObject.Identity::class -> SyncObjectType.IDENTITY
        SyncObject.Passkey::class -> SyncObjectType.PASSKEY
        SyncObject.Passport::class -> SyncObjectType.PASSPORT
        SyncObject.PaymentCreditCard::class -> SyncObjectType.PAYMENT_CREDIT_CARD
        SyncObject.PaymentPaypal::class -> SyncObjectType.PAYMENT_PAYPAL
        SyncObject.PersonalWebsite::class -> SyncObjectType.PERSONAL_WEBSITE
        SyncObject.Phone::class -> SyncObjectType.PHONE
        SyncObject.SecureFileInfo::class -> SyncObjectType.SECURE_FILE_INFO
        SyncObject.SecureNote::class -> SyncObjectType.SECURE_NOTE
        SyncObject.SecureNoteCategory::class -> SyncObjectType.SECURE_NOTE_CATEGORY
        SyncObject.SecurityBreach::class -> SyncObjectType.SECURITY_BREACH
        SyncObject.SocialSecurityStatement::class -> SyncObjectType.SOCIAL_SECURITY_STATEMENT
        else ->
            error("${java.simpleName} doesn't have a corresponding ${SyncObjectType::class.java.simpleName} enum value")
    }

val SyncObjectType.desktopId: Int
    get() = when (this) {
        SyncObjectType.ADDRESS -> DataIdentifierId.ADDRESS
        SyncObjectType.AUTH_CATEGORY -> DataIdentifierId.AUTH_CATEGORY
        SyncObjectType.AUTHENTIFIANT -> DataIdentifierId.AUTHENTIFIANT
        SyncObjectType.COMPANY -> DataIdentifierId.COMPANY
        SyncObjectType.DRIVER_LICENCE -> DataIdentifierId.DRIVER_LICENCE
        SyncObjectType.EMAIL -> DataIdentifierId.EMAIL
        SyncObjectType.FISCAL_STATEMENT -> DataIdentifierId.FISCAL_STATEMENT
        SyncObjectType.GENERATED_PASSWORD -> DataIdentifierId.GENERATED_PASSWORD
        SyncObjectType.ID_CARD -> DataIdentifierId.ID_CARD
        SyncObjectType.IDENTITY -> DataIdentifierId.IDENTITY
        SyncObjectType.PASSPORT -> DataIdentifierId.PASSPORT
        SyncObjectType.PAYMENT_PAYPAL -> DataIdentifierId.PAYMENT_PAYPAL
        SyncObjectType.PAYMENT_CREDIT_CARD -> DataIdentifierId.PAYMENT_CREDIT_CARD
        SyncObjectType.PERSONAL_WEBSITE -> DataIdentifierId.PERSONAL_WEBSITE
        SyncObjectType.PHONE -> DataIdentifierId.PHONE
        SyncObjectType.SOCIAL_SECURITY_STATEMENT -> DataIdentifierId.SOCIAL_SECURITY_STATEMENT
        SyncObjectType.SECURE_NOTE -> DataIdentifierId.SECURE_NOTE
        SyncObjectType.SECURE_NOTE_CATEGORY -> DataIdentifierId.SECURE_NOTE_CATEGORY
        SyncObjectType.BANK_STATEMENT -> DataIdentifierId.BANK_STATEMENT
        SyncObjectType.DATA_CHANGE_HISTORY -> DataIdentifierId.DATA_CHANGE_HISTORY
        SyncObjectType.SECURE_FILE_INFO -> DataIdentifierId.SECURE_FILE_INFO
        SyncObjectType.SECURITY_BREACH -> DataIdentifierId.SECURITY_BREACH
        else -> -1
    }

val SyncObject.desktopId: Int
    get() = supportedSyncObjectType.desktopId

operator fun <T : SyncObject> SyncObjectType.Companion.get(vaultItem: VaultItem<T>): SyncObjectType? {
    return vaultItem.syncObject.supportedSyncObjectType
}

operator fun SyncObjectType.Companion.get(summaryObject: SummaryObject): SyncObjectType? {
    return summaryObject.syncObjectType
}

val SyncObjectType.isSpaceSupported: Boolean
    get() {
        return this in SyncObjectTypeUtils.WITH_TEAMSPACES
    }

fun <T : SyncObject> VaultItem<T>?.valueOfFromDataIdentifier() = this?.toSummary<SummaryObject>()?.valueOfFromDataIdentifier()
fun SummaryObject?.valueOfFromDataIdentifier() = this?.let { SyncObjectType[it] }
