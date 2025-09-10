package com.dashlane.xml.domain

import com.dashlane.xml.domain.properties.AddressXml
import com.dashlane.xml.domain.properties.AuthCategoryXml
import com.dashlane.xml.domain.properties.AuthentifiantXml
import com.dashlane.xml.domain.properties.BankStatementXml
import com.dashlane.xml.domain.properties.CompanyXml
import com.dashlane.xml.domain.properties.DriverLicenceXml
import com.dashlane.xml.domain.properties.FiscalStatementXml
import com.dashlane.xml.domain.properties.GeneratedPasswordXml
import com.dashlane.xml.domain.properties.IdCardXml
import com.dashlane.xml.domain.properties.PassportXml
import com.dashlane.xml.domain.properties.PaymentCreditCardXml
import com.dashlane.xml.domain.properties.PersonalWebsiteXml
import com.dashlane.xml.domain.properties.PhoneXml
import com.dashlane.xml.domain.properties.SecretXml
import com.dashlane.xml.domain.properties.SecureNoteCategoryXml
import com.dashlane.xml.domain.properties.SecureNoteXml
import com.dashlane.xml.domain.properties.SocialSecurityStatementXml
import com.dashlane.xml.domain.properties.SyncObjectXml
import com.dashlane.xml.domain.properties.WiFiXml
import javax.annotation.processing.Generated

@Generated
object SyncUserContentFields {
    val syncObject = setOf(
        SyncObjectXml.ATTACHMENTS,
    )

    val address = syncObject + setOf(
        AddressXml.ZIP_CODE,
        AddressXml.ADDRESS_FULL,
        AddressXml.CITY,
        AddressXml.COUNTRY,
    )
    val authCategory = syncObject + setOf(
        AuthCategoryXml.CATEGORY_NAME,
    )
    val authentifiant = syncObject + setOf(
        AuthentifiantXml.OTP_URL,
        AuthentifiantXml.EMAIL,
        AuthentifiantXml.URL,
        AuthentifiantXml.USE_FIXED_URL,
        AuthentifiantXml.OTP_SECRET,
        AuthentifiantXml.PASSWORD,
        AuthentifiantXml.TITLE,
        AuthentifiantXml.LOGIN,
        AuthentifiantXml.NOTE,
        AuthentifiantXml.USER_SELECTED_URL,
    )
    val bankStatement = syncObject + setOf(
        BankStatementXml.BANK_ACCOUNT_B_I_C,
        BankStatementXml.BANK_ACCOUNT_I_B_A_N,
    )
    val company = syncObject + setOf(
        CompanyXml.JOB_TITLE,
        CompanyXml.NAME,
    )
    val driverLicence = syncObject + setOf(
        DriverLicenceXml.NUMBER,
    )
    val fiscalStatement = syncObject + setOf(
        FiscalStatementXml.TELEDECLARANT_NUMBER,
        FiscalStatementXml.FISCAL_NUMBER,
    )
    val generatedPassword = syncObject + setOf(
        GeneratedPasswordXml.GENERATED_DATE,
        GeneratedPasswordXml.PASSWORD,
    )
    val idCard = syncObject + setOf(
        IdCardXml.NUMBER,
    )
    val passport = syncObject + setOf(
        PassportXml.NUMBER,
    )
    val paymentCreditCard = syncObject + setOf(
        PaymentCreditCardXml.CARD_NUMBER,
        PaymentCreditCardXml.OWNER_NAME,
        PaymentCreditCardXml.BANK,
        PaymentCreditCardXml.EXPIRE_MONTH,
        PaymentCreditCardXml.START_MONTH,
        PaymentCreditCardXml.START_YEAR,
    )
    val personalWebsite = syncObject + setOf(
        PersonalWebsiteXml.WEBSITE,
    )
    val phone = syncObject + setOf(
        PhoneXml.NUMBER,
    )
    val secret = syncObject + setOf(
        SecretXml.CONTENT,
        SecretXml.TITLE,
    )
    val secureNoteCategory = syncObject + setOf(
        SecureNoteCategoryXml.CATEGORY_NAME,
    )
    val secureNote = syncObject + setOf(
        SecureNoteXml.CONTENT,
        SecureNoteXml.TITLE,
    )
    val socialSecurityStatement = syncObject + setOf(
        SocialSecurityStatementXml.SOCIAL_SECURITY_NUMBER,
    )
    val wiFi = syncObject + setOf(
        WiFiXml.NOTE,
        WiFiXml.PASSPHRASE,
        WiFiXml.SSID,
        WiFiXml.NAME,
    )
}