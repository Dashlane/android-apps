package com.dashlane.database.adapter

import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.squareup.moshi.adapters.PolymorphicJsonAdapterFactory
import kotlin.jvm.java

object SummaryObjectAdapterFactory {
    private const val TYPE_LABEL_KEY = "syncObjectType"

    val factory: PolymorphicJsonAdapterFactory<SummaryObject> = PolymorphicJsonAdapterFactory.of(SummaryObject::class.java, TYPE_LABEL_KEY)
        .withSubtype(SummaryObject.AuthCategory::class.java, SyncObjectType.AUTH_CATEGORY.transactionType)
        .withSubtype(SummaryObject.SecureNoteCategory::class.java, SyncObjectType.SECURE_NOTE_CATEGORY.transactionType)
        .withSubtype(SummaryObject.Authentifiant::class.java, SyncObjectType.AUTHENTIFIANT.transactionType)
        .withSubtype(SummaryObject.SecureNote::class.java, SyncObjectType.SECURE_NOTE.transactionType)
        .withSubtype(SummaryObject.Address::class.java, SyncObjectType.ADDRESS.transactionType)
        .withSubtype(SummaryObject.Email::class.java, SyncObjectType.EMAIL.transactionType)
        .withSubtype(SummaryObject.Phone::class.java, SyncObjectType.PHONE.transactionType)
        .withSubtype(SummaryObject.PersonalWebsite::class.java, SyncObjectType.PERSONAL_WEBSITE.transactionType)
        .withSubtype(SummaryObject.PaymentCreditCard::class.java, SyncObjectType.PAYMENT_CREDIT_CARD.transactionType)
        .withSubtype(SummaryObject.Passkey::class.java, SyncObjectType.PASSKEY.transactionType)
        .withSubtype(SummaryObject.Passport::class.java, SyncObjectType.PASSPORT.transactionType)
        .withSubtype(SummaryObject.SecureFileInfo::class.java, SyncObjectType.SECURE_FILE_INFO.transactionType)
        .withSubtype(SummaryObject.SecurityBreach::class.java, SyncObjectType.SECURITY_BREACH.transactionType)
        .withSubtype(SummaryObject.BankStatement::class.java, SyncObjectType.BANK_STATEMENT.transactionType)
        .withSubtype(SummaryObject.Company::class.java, SyncObjectType.COMPANY.transactionType)
        .withSubtype(SummaryObject.Collection::class.java, SyncObjectType.COLLECTION.transactionType)
        .withSubtype(SummaryObject.DriverLicence::class.java, SyncObjectType.DRIVER_LICENCE.transactionType)
        .withSubtype(SummaryObject.Identity::class.java, SyncObjectType.IDENTITY.transactionType)
        .withSubtype(SummaryObject.FiscalStatement::class.java, SyncObjectType.FISCAL_STATEMENT.transactionType)
        .withSubtype(SummaryObject.IdCard::class.java, SyncObjectType.ID_CARD.transactionType)
        .withSubtype(SummaryObject.SocialSecurityStatement::class.java, SyncObjectType.SOCIAL_SECURITY_STATEMENT.transactionType)
        .withSubtype(SummaryObject.GeneratedPassword::class.java, SyncObjectType.GENERATED_PASSWORD.transactionType)
        .withSubtype(SummaryObject.Secret::class.java, SyncObjectType.SECRET.transactionType)
        .withSubtype(SummaryObject.WiFi::class.java, SyncObjectType.WI_FI.transactionType)
}