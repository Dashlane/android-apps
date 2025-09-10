package com.dashlane.xml.domain

import com.dashlane.xml.XmlData
import com.dashlane.xml.domain.SyncObject.Address
import com.dashlane.xml.domain.SyncObject.AuthCategory
import com.dashlane.xml.domain.SyncObject.Authentifiant
import com.dashlane.xml.domain.SyncObject.BankStatement
import com.dashlane.xml.domain.SyncObject.Collection
import com.dashlane.xml.domain.SyncObject.Company
import com.dashlane.xml.domain.SyncObject.DataChangeHistory
import com.dashlane.xml.domain.SyncObject.DriverLicence
import com.dashlane.xml.domain.SyncObject.Email
import com.dashlane.xml.domain.SyncObject.FiscalStatement
import com.dashlane.xml.domain.SyncObject.GeneratedPassword
import com.dashlane.xml.domain.SyncObject.IdCard
import com.dashlane.xml.domain.SyncObject.Identity
import com.dashlane.xml.domain.SyncObject.Passkey
import com.dashlane.xml.domain.SyncObject.Passport
import com.dashlane.xml.domain.SyncObject.PaymentCreditCard
import com.dashlane.xml.domain.SyncObject.PersonalWebsite
import com.dashlane.xml.domain.SyncObject.Phone
import com.dashlane.xml.domain.SyncObject.Secret
import com.dashlane.xml.domain.SyncObject.SecureFileInfo
import com.dashlane.xml.domain.SyncObject.SecureNote
import com.dashlane.xml.domain.SyncObject.SecureNoteCategory
import com.dashlane.xml.domain.SyncObject.SecurityBreach
import com.dashlane.xml.domain.SyncObject.Settings
import com.dashlane.xml.domain.SyncObject.SocialSecurityStatement
import com.dashlane.xml.domain.SyncObject.WiFi
import javax.annotation.processing.Generated
import kotlin.reflect.KClass

@Generated
enum class SyncObjectType(
    val transactionType: String,
    val xmlObjectName: String,
    val kClass: KClass<out SyncObject>,
    val conflictingFields: Set<String> = emptySet(),
    val sensitiveFields: Set<String> = emptySet(),
    val hasDeduplication: Boolean = false,
) {
    ADDRESS(
        SyncTransactionName.ADDRESS,
        SyncObjectXmlName.ADDRESS,
        Address::class,
        conflictingFields = SyncConflictDuplicates.address,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Address(data)
    },
    AUTH_CATEGORY(
        SyncTransactionName.AUTH_CATEGORY,
        SyncObjectXmlName.AUTH_CATEGORY,
        AuthCategory::class,
        conflictingFields = SyncConflictDuplicates.authCategory,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            AuthCategory(data)
    },
    AUTHENTIFIANT(
        SyncTransactionName.AUTHENTIFIANT,
        SyncObjectXmlName.AUTHENTIFIANT,
        Authentifiant::class,
        conflictingFields = SyncConflictDuplicates.authentifiant,
        sensitiveFields = SyncSensitiveFields.authentifiant,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Authentifiant(data)
    },
    BANK_STATEMENT(
        SyncTransactionName.BANK_STATEMENT,
        SyncObjectXmlName.BANK_STATEMENT,
        BankStatement::class,
        conflictingFields = SyncConflictDuplicates.bankStatement,
        sensitiveFields = SyncSensitiveFields.bankStatement,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            BankStatement(data)
    },
    COLLECTION(
        SyncTransactionName.COLLECTION,
        SyncObjectXmlName.COLLECTION,
        Collection::class,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Collection(data)
    },
    COMPANY(
        SyncTransactionName.COMPANY,
        SyncObjectXmlName.COMPANY,
        Company::class,
        conflictingFields = SyncConflictDuplicates.company,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Company(data)
    },
    DATA_CHANGE_HISTORY(
        SyncTransactionName.DATA_CHANGE_HISTORY,
        SyncObjectXmlName.DATA_CHANGE_HISTORY,
        DataChangeHistory::class,
        conflictingFields = SyncConflictDuplicates.dataChangeHistory,
    ) {
        override fun create(data: Map<String, XmlData>) =
            DataChangeHistory(data)
    },
    DRIVER_LICENCE(
        SyncTransactionName.DRIVER_LICENCE,
        SyncObjectXmlName.DRIVER_LICENCE,
        DriverLicence::class,
        conflictingFields = SyncConflictDuplicates.driverLicence,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            DriverLicence(data)
    },
    EMAIL(
        SyncTransactionName.EMAIL,
        SyncObjectXmlName.EMAIL,
        Email::class,
        conflictingFields = SyncConflictDuplicates.email,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Email(data)
    },
    FISCAL_STATEMENT(
        SyncTransactionName.FISCAL_STATEMENT,
        SyncObjectXmlName.FISCAL_STATEMENT,
        FiscalStatement::class,
        conflictingFields = SyncConflictDuplicates.fiscalStatement,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            FiscalStatement(data)
    },
    GENERATED_PASSWORD(
        SyncTransactionName.GENERATED_PASSWORD,
        SyncObjectXmlName.GENERATED_PASSWORD,
        GeneratedPassword::class,
        conflictingFields = SyncConflictDuplicates.generatedPassword,
        sensitiveFields = SyncSensitiveFields.generatedPassword,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            GeneratedPassword(data)
    },
    ID_CARD(
        SyncTransactionName.ID_CARD,
        SyncObjectXmlName.ID_CARD,
        IdCard::class,
        conflictingFields = SyncConflictDuplicates.idCard,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            IdCard(data)
    },
    IDENTITY(
        SyncTransactionName.IDENTITY,
        SyncObjectXmlName.IDENTITY,
        Identity::class,
        conflictingFields = SyncConflictDuplicates.identity,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Identity(data)
    },
    PASSKEY(
        SyncTransactionName.PASSKEY,
        SyncObjectXmlName.PASSKEY,
        Passkey::class,
        sensitiveFields = SyncSensitiveFields.passkey,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Passkey(data)
    },
    PASSPORT(
        SyncTransactionName.PASSPORT,
        SyncObjectXmlName.PASSPORT,
        Passport::class,
        conflictingFields = SyncConflictDuplicates.passport,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Passport(data)
    },
    PAYMENT_CREDIT_CARD(
        SyncTransactionName.PAYMENT_CREDIT_CARD,
        SyncObjectXmlName.PAYMENT_CREDIT_CARD,
        PaymentCreditCard::class,
        conflictingFields = SyncConflictDuplicates.paymentCreditCard,
        sensitiveFields = SyncSensitiveFields.paymentCreditCard,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            PaymentCreditCard(data)
    },
    PERSONAL_WEBSITE(
        SyncTransactionName.PERSONAL_WEBSITE,
        SyncObjectXmlName.PERSONAL_WEBSITE,
        PersonalWebsite::class,
        conflictingFields = SyncConflictDuplicates.personalWebsite,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            PersonalWebsite(data)
    },
    PHONE(
        SyncTransactionName.PHONE,
        SyncObjectXmlName.PHONE,
        Phone::class,
        conflictingFields = SyncConflictDuplicates.phone,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Phone(data)
    },
    SECRET(
        SyncTransactionName.SECRET,
        SyncObjectXmlName.SECRET,
        Secret::class,
        conflictingFields = SyncConflictDuplicates.secret,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Secret(data)
    },
    SECURE_FILE_INFO(
        SyncTransactionName.SECURE_FILE_INFO,
        SyncObjectXmlName.SECURE_FILE_INFO,
        SecureFileInfo::class,
    ) {
        override fun create(data: Map<String, XmlData>) =
            SecureFileInfo(data)
    },
    SECURE_NOTE(
        SyncTransactionName.SECURE_NOTE,
        SyncObjectXmlName.SECURE_NOTE,
        SecureNote::class,
        conflictingFields = SyncConflictDuplicates.secureNote,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            SecureNote(data)
    },
    SECURE_NOTE_CATEGORY(
        SyncTransactionName.SECURE_NOTE_CATEGORY,
        SyncObjectXmlName.SECURE_NOTE_CATEGORY,
        SecureNoteCategory::class,
        conflictingFields = SyncConflictDuplicates.secureNoteCategory,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            SecureNoteCategory(data)
    },
    SECURITY_BREACH(
        SyncTransactionName.SECURITY_BREACH,
        SyncObjectXmlName.SECURITY_BREACH,
        SecurityBreach::class,
        sensitiveFields = SyncSensitiveFields.securityBreach,
    ) {
        override fun create(data: Map<String, XmlData>) =
            SecurityBreach(data)
    },
    SETTINGS(
        SyncTransactionName.SETTINGS,
        SyncObjectXmlName.SETTINGS,
        Settings::class,
    ) {
        override fun create(data: Map<String, XmlData>) =
            Settings(data)
    },
    SOCIAL_SECURITY_STATEMENT(
        SyncTransactionName.SOCIAL_SECURITY_STATEMENT,
        SyncObjectXmlName.SOCIAL_SECURITY_STATEMENT,
        SocialSecurityStatement::class,
        conflictingFields = SyncConflictDuplicates.socialSecurityStatement,
        sensitiveFields = SyncSensitiveFields.socialSecurityStatement,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            SocialSecurityStatement(data)
    },
    WI_FI(
        SyncTransactionName.WI_FI,
        SyncObjectXmlName.WI_FI,
        WiFi::class,
        conflictingFields = SyncConflictDuplicates.wiFi,
        sensitiveFields = SyncSensitiveFields.wiFi,
        hasDeduplication = true,
    ) {
        override fun create(data: Map<String, XmlData>) =
            WiFi(data)
    }, ;

    abstract fun create(data: Map<String, XmlData> = mapOf()): SyncObject

    companion object {
        private val byTransactionType: Map<String, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::transactionType).let {
                it + it.mapKeys { (key, _) -> key.replace("_", "") }
            }
        private val byXmlName: Map<String, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::xmlObjectName)
        private val byClass: Map<KClass<out SyncObject>, SyncObjectType> =
            enumValues<SyncObjectType>().associateBy(SyncObjectType::kClass)

        fun forTransactionType(transactionType: String): SyncObjectType =
            forTransactionTypeOrNull(transactionType)
                ?: throw IllegalArgumentException("SyncObjectType(transactionType = $transactionType) not found.")

        fun forTransactionTypeOrNull(transactionType: String): SyncObjectType? =
            byTransactionType[transactionType]
                ?: byTransactionType[transactionType.replace("_", "")]

        fun forXmlName(xmlObjectName: String): SyncObjectType =
            forXmlNameOrNull(xmlObjectName)
                ?: throw IllegalArgumentException("SyncObjectType(xmlObjectName = $xmlObjectName) not found.")

        fun forXmlNameOrNull(xmlObjectName: String): SyncObjectType? =
            byXmlName[xmlObjectName]

        fun forObject(syncObject: SyncObject): SyncObjectType =
            forClass(syncObject::class)

        fun forClass(kClass: KClass<out SyncObject>): SyncObjectType =
            byClass.getValue(kClass)
    }
}