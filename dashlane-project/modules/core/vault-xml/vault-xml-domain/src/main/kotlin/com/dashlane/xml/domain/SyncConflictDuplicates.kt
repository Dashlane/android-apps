package com.dashlane.xml.domain

import com.dashlane.xml.domain.properties.AddressXml
import com.dashlane.xml.domain.properties.AuthCategoryXml
import com.dashlane.xml.domain.properties.AuthentifiantXml
import com.dashlane.xml.domain.properties.BankStatementXml
import com.dashlane.xml.domain.properties.CompanyXml
import com.dashlane.xml.domain.properties.DataChangeHistoryXml
import com.dashlane.xml.domain.properties.DriverLicenceXml
import com.dashlane.xml.domain.properties.EmailXml
import com.dashlane.xml.domain.properties.FiscalStatementXml
import com.dashlane.xml.domain.properties.GeneratedPasswordXml
import com.dashlane.xml.domain.properties.IdCardXml
import com.dashlane.xml.domain.properties.IdentityXml
import com.dashlane.xml.domain.properties.PassportXml
import com.dashlane.xml.domain.properties.PaymentCreditCardXml
import com.dashlane.xml.domain.properties.PersonalWebsiteXml
import com.dashlane.xml.domain.properties.PhoneXml
import com.dashlane.xml.domain.properties.SecretXml
import com.dashlane.xml.domain.properties.SecureNoteCategoryXml
import com.dashlane.xml.domain.properties.SecureNoteXml
import com.dashlane.xml.domain.properties.SocialSecurityStatementXml
import com.dashlane.xml.domain.properties.WiFiXml
import javax.annotation.processing.Generated

@Generated
object SyncConflictDuplicates {
    val address = setOf(
        AddressXml.ZIP_CODE,
        AddressXml.ADDRESS_FULL,
        AddressXml.CITY,
    )
    val authCategory = setOf(
        AuthCategoryXml.CATEGORY_NAME,
    )
    val authentifiant = setOf(
        AuthentifiantXml.OTP_URL,
        AuthentifiantXml.EMAIL,
        AuthentifiantXml.USE_FIXED_URL,
        AuthentifiantXml.OTP_SECRET,
        AuthentifiantXml.PASSWORD,
        AuthentifiantXml.SUBDOMAIN_ONLY,
        AuthentifiantXml.TITLE,
        AuthentifiantXml.LOGIN,
        AuthentifiantXml.NOTE,
    )
    val bankStatement = setOf(
        BankStatementXml.BANK_ACCOUNT_I_B_A_N,
    )
    val company = setOf(
        CompanyXml.JOB_TITLE,
        CompanyXml.NAME,
    )
    val dataChangeHistory = setOf(
        DataChangeHistoryXml.OBJECT_TITLE,
        DataChangeHistoryXml.OBJECT_TYPE,
        DataChangeHistoryXml.OBJECT_ID,
    )
    val driverLicence = setOf(
        DriverLicenceXml.NUMBER,
    )
    val email = setOf(
        EmailXml.EMAIL,
    )
    val fiscalStatement = setOf(
        FiscalStatementXml.TELEDECLARANT_NUMBER,
        FiscalStatementXml.FISCAL_NUMBER,
    )
    val generatedPassword = setOf(
        GeneratedPasswordXml.PLATFORM,
        GeneratedPasswordXml.GENERATED_DATE,
        GeneratedPasswordXml.DOMAIN,
        GeneratedPasswordXml.PASSWORD,
    )
    val idCard = setOf(
        IdCardXml.NUMBER,
    )
    val identity = setOf(
        IdentityXml.FIRST_NAME,
        IdentityXml.LAST_NAME,
    )
    val passport = setOf(
        PassportXml.NUMBER,
    )
    val paymentCreditCard = setOf(
        PaymentCreditCardXml.CARD_NUMBER,
        PaymentCreditCardXml.OWNER_NAME,
        PaymentCreditCardXml.BANK,
        PaymentCreditCardXml.EXPIRE_MONTH,
        PaymentCreditCardXml.EXPIRE_YEAR,
    )
    val personalWebsite = setOf(
        PersonalWebsiteXml.WEBSITE,
    )
    val phone = setOf(
        PhoneXml.NUMBER,
    )
    val secret = setOf(
        SecretXml.CONTENT,
        SecretXml.TITLE,
    )
    val secureNoteCategory = setOf(
        SecureNoteCategoryXml.CATEGORY_NAME,
    )
    val secureNote = setOf(
        SecureNoteXml.CONTENT,
        SecureNoteXml.TITLE,
    )
    val socialSecurityStatement = setOf(
        SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER,
    )
    val wiFi = setOf(
        WiFiXml.NOTE,
        WiFiXml.PASSPHRASE,
        WiFiXml.SSID,
        WiFiXml.NAME,
    )
}