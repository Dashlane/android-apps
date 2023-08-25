package com.dashlane.util.clipboard.vault

import com.dashlane.authenticator.otp
import com.dashlane.util.clipboard.vault.CopyFieldContentMapper.ContentOnlyInSyncObjectException
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

enum class CopyField(
    internal val syncObjectType: SyncObjectType,
    internal val contentMapper: CopyFieldContentMapper<out SummaryObject, out SyncObject, out Any>,
    val isSharingProtected: Boolean = false,
    val isSensitiveData: Boolean = false
) {
    Password(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.isPasswordEmpty.not() },
            hasContentInFull = { it.password?.toString().isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.Authentifiant::password
        ),
        true,
        true
    ),
    Login(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.login.isNotSemanticallyNull() },
            hasContentInFull = { it.login.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Authentifiant::login,
            getContentInFull = SyncObject.Authentifiant::login
        )
    ),
    Email(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.email.isNotSemanticallyNull() },
            hasContentInFull = { it.email.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Authentifiant::email,
            getContentInFull = SyncObject.Authentifiant::email
        )
    ),
    SecondaryLogin(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.secondaryLogin.isNotSemanticallyNull() },
            hasContentInFull = { it.secondaryLogin.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Authentifiant::secondaryLogin,
            getContentInFull = SyncObject.Authentifiant::secondaryLogin
        )
    ),
    OtpCode(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.otp() != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { it.otp()?.getPin()?.code }
        )
    ),

    PaymentsNumber(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.isCardNumberEmpty.not() },
            hasContentInFull = { it.cardNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.PaymentCreditCard::cardNumber
        ),
        isSensitiveData = true
    ),
    PaymentsSecurityCode(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.isSecurityCodeEmpty.not() },
            hasContentInFull = { it.securityCode.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.PaymentCreditCard::securityCode
        ),
        isSensitiveData = true
    ),
    PaymentsExpirationDate(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = SummaryObject.PaymentCreditCard::expireDate,
            getContentInFull = SyncObject.PaymentCreditCard::expireDate
        )
    ),

    PayPalLogin(
        SyncObjectType.PAYMENT_PAYPAL,
        CopyFieldContentMapper.PaymentPaypal(
            hasContentInSummary = { it.login.isNotSemanticallyNull() },
            hasContentInFull = { it.login.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.PaymentPaypal::login,
            getContentInFull = SyncObject.PaymentPaypal::login
        )
    ),
    PayPalPassword(
        SyncObjectType.PAYMENT_PAYPAL,
        CopyFieldContentMapper.PaymentPaypal(
            hasContentInSummary = { it.isPasswordEmpty.not() },
            hasContentInFull = { it.password?.toString().isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.PaymentPaypal::password
        ),
        isSensitiveData = true
    ),

    BankAccountBank(
        SyncObjectType.BANK_STATEMENT,
        CopyFieldContentMapper.BankStatement(
            hasContentInSummary = { it.bankAccountBank.isValidBank() },
            hasContentInFull = { it.bankAccountBank.isValidBank() },
            getContentInSummary = SummaryObject.BankStatement::bankAccountBank,
            getContentInFull = SyncObject.BankStatement::bankAccountBank
        )
    ),
    BankAccountBicSwift(
        SyncObjectType.BANK_STATEMENT,
        CopyFieldContentMapper.BankStatement(
            hasContentInSummary = { it.isBICEmpty.not() },
            hasContentInFull = { it.bankAccountBIC.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.BankStatement::bankAccountBIC
        ),
        isSensitiveData = true
    ),
    BankAccountRoutingNumber(
        SyncObjectType.BANK_STATEMENT,
        BankAccountBicSwift.contentMapper,
        isSensitiveData = true
    ),
    BankAccountSortCode(
        SyncObjectType.BANK_STATEMENT,
        BankAccountBicSwift.contentMapper,
        isSensitiveData = true
    ),
    BankAccountIban(
        SyncObjectType.BANK_STATEMENT,
        CopyFieldContentMapper.BankStatement(
            hasContentInSummary = { it.isIBANEmpty.not() },
            hasContentInFull = { it.bankAccountIBAN.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.BankStatement::bankAccountIBAN
        ),
        isSensitiveData = true
    ),
    BankAccountAccountNumber(
        SyncObjectType.BANK_STATEMENT,
        BankAccountIban.contentMapper,
        isSensitiveData = true
    ),
    BankAccountClabe(
        SyncObjectType.BANK_STATEMENT,
        BankAccountIban.contentMapper
    ),

    Address(
        SyncObjectType.ADDRESS,
        CopyFieldContentMapper.Address(
            hasContentInSummary = { it.addressFull.isNotSemanticallyNull() },
            hasContentInFull = { it.addressFull.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Address::addressFull,
            getContentInFull = SyncObject.Address::addressFull
        )
    ),
    City(
        SyncObjectType.ADDRESS,
        CopyFieldContentMapper.Address(
            hasContentInSummary = { it.city.isNotSemanticallyNull() },
            hasContentInFull = { it.city.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Address::city,
            getContentInFull = SyncObject.Address::city
        )
    ),
    ZipCode(
        SyncObjectType.ADDRESS,
        CopyFieldContentMapper.Address(
            hasContentInSummary = { it.zipCode.isNotSemanticallyNull() },
            hasContentInFull = { it.zipCode.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Address::zipCode,
            getContentInFull = SyncObject.Address::zipCode
        )
    ),

    IdsNumber(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.IdCard::number,
            getContentInFull = SyncObject.IdCard::number
        )
    ),
    IdsIssueDate(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.IdCard::deliveryDate
        )
    ),
    IdsExpirationDate(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = SummaryObject.IdCard::expireDate,
            getContentInFull = SyncObject.IdCard::expireDate
        )
    ),

    PassportNumber(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Passport::number,
            getContentInFull = SyncObject.Passport::number
        )
    ),
    PassportIssueDate(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.Passport::deliveryDate
        )
    ),
    PassportExpirationDate(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = SummaryObject.Passport::expireDate,
            getContentInFull = SyncObject.Passport::expireDate
        )
    ),

    DriverLicenseNumber(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.DriverLicence::number,
            getContentInFull = SyncObject.DriverLicence::number
        )
    ),
    DriverLicenseIssueDate(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.DriverLicence::deliveryDate
        )
    ),
    DriverLicenseExpirationDate(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = SummaryObject.DriverLicence::expireDate,
            getContentInFull = SyncObject.DriverLicence::expireDate
        )
    ),

    SocialSecurityNumber(
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        CopyFieldContentMapper.SocialSecurityStatement(
            hasContentInSummary = { it.isSocialSecurityNumberEmpty.not() },
            hasContentInFull = { it.socialSecurityNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.SocialSecurityStatement::socialSecurityNumber
        ),
        isSensitiveData = true
    ),

    TaxNumber(
        SyncObjectType.FISCAL_STATEMENT,
        CopyFieldContentMapper.FiscalStatement(
            hasContentInSummary = { it.fiscalNumber.isNotSemanticallyNull() },
            hasContentInFull = { it.fiscalNumber.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.FiscalStatement::fiscalNumber,
            getContentInFull = SyncObject.FiscalStatement::fiscalNumber
        )
    ),
    TaxOnlineNumber(
        SyncObjectType.FISCAL_STATEMENT,
        CopyFieldContentMapper.FiscalStatement(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.teledeclarantNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = SyncObject.FiscalStatement::teledeclarantNumber
        )
    ),

    JustEmail(
        SyncObjectType.EMAIL,
        CopyFieldContentMapper.EmailContent(
            hasContentInSummary = { it.email.isNotSemanticallyNull() },
            hasContentInFull = { it.email.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Email::email,
            getContentInFull = SyncObject.Email::email
        )
    ),

    PhoneNumber(
        SyncObjectType.PHONE,
        CopyFieldContentMapper.Phone(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Phone::number,
            getContentInFull = SyncObject.Phone::number
        )
    ),

    PersonalWebsite(
        SyncObjectType.PERSONAL_WEBSITE,
        CopyFieldContentMapper.PersonalWebsite(
            hasContentInSummary = { it.website.isNotSemanticallyNull() },
            hasContentInFull = { it.website.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.PersonalWebsite::website,
            getContentInFull = SyncObject.PersonalWebsite::website
        )
    ),

    FirstName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.firstName.isNotSemanticallyNull() },
            hasContentInFull = { it.firstName.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Identity::firstName,
            getContentInFull = SyncObject.Identity::firstName
        )
    ),
    LastName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.lastName.isNotSemanticallyNull() },
            hasContentInFull = { it.lastName.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Identity::lastName,
            getContentInFull = SyncObject.Identity::lastName
        )
    ),
    MiddleName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.middleName.isNotSemanticallyNull() },
            hasContentInFull = { it.middleName.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Identity::middleName,
            getContentInFull = SyncObject.Identity::middleName
        )
    ),
    IdentityLogin(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.pseudo.isNotSemanticallyNull() },
            hasContentInFull = { it.pseudo.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Identity::pseudo,
            getContentInFull = SyncObject.Identity::pseudo
        )
    ),

    CompanyName(
        SyncObjectType.COMPANY,
        CopyFieldContentMapper.Company(
            hasContentInSummary = { it.name.isNotSemanticallyNull() },
            hasContentInFull = { it.name.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Company::name,
            getContentInFull = SyncObject.Company::name
        )
    ),
    CompanyTitle(
        SyncObjectType.COMPANY,
        CopyFieldContentMapper.Company(
            hasContentInSummary = { it.jobTitle.isNotSemanticallyNull() },
            hasContentInFull = { it.jobTitle.isNotSemanticallyNull() },
            getContentInSummary = SummaryObject.Company::jobTitle,
            getContentInFull = SyncObject.Company::jobTitle
        )
    );
}

private fun String?.isValidBank(): Boolean = isNotSemanticallyNull() && this != CreditCardBank.US_NO_TYPE