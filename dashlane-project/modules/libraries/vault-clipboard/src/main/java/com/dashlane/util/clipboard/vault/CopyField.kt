package com.dashlane.util.clipboard.vault

import com.dashlane.authenticator.otp
import com.dashlane.util.clipboard.vault.CopyFieldContentMapper.ContentOnlyInSyncObjectException
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.CreditCardBank
import com.dashlane.vault.model.expireDate
import com.dashlane.vault.model.fullName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

enum class CopyField(
    internal val syncObjectType: SyncObjectType,
    internal val contentMapper: CopyFieldContentMapper<out SummaryObject, out SyncObject>,
    val isSharingProtected: Boolean = false,
    val isSensitiveData: Boolean = false
) {
    Password(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.isPasswordEmpty.not() },
            hasContentInFull = { it.password?.toString().isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.password) },
        ),
        true,
        true
    ),
    Login(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.login.isNotSemanticallyNull() },
            hasContentInFull = { it.login.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.login) },
            getContentInFull = { CopyContent.Ready.StringValue(it.login) }
        )
    ),
    Email(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.email.isNotSemanticallyNull() },
            hasContentInFull = { it.email.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.email) },
            getContentInFull = { CopyContent.Ready.StringValue(it.email) }
        )
    ),
    SecondaryLogin(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { it.secondaryLogin.isNotSemanticallyNull() },
            hasContentInFull = { it.secondaryLogin.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.secondaryLogin) },
            getContentInFull = { CopyContent.Ready.StringValue(it.secondaryLogin) }
        )
    ),
    OtpCode(
        SyncObjectType.AUTHENTIFIANT,
        CopyFieldContentMapper.Authentifiant(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.otp() != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.StringValue(it.otp()?.getPin()?.code) }
        )
    ),

    PaymentsNumber(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.isCardNumberEmpty.not() },
            hasContentInFull = { it.cardNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.cardNumber) }
        ),
        isSensitiveData = true
    ),
    PaymentsSecurityCode(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.isSecurityCodeEmpty.not() },
            hasContentInFull = { it.securityCode.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.securityCode) }
        ),
        isSensitiveData = true
    ),
    PaymentsExpirationDate(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        CopyFieldContentMapper.PaymentCreditCard(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = { CopyContent.Ready.YearMonth(it.expireDate) },
            getContentInFull = { CopyContent.Ready.YearMonth(it.expireDate) }
        )
    ),

    PayPalLogin(
        SyncObjectType.PAYMENT_PAYPAL,
        CopyFieldContentMapper.PaymentPaypal(
            hasContentInSummary = { it.login.isNotSemanticallyNull() },
            hasContentInFull = { it.login.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.login) },
            getContentInFull = { CopyContent.Ready.StringValue(it.login) }
        )
    ),
    PayPalPassword(
        SyncObjectType.PAYMENT_PAYPAL,
        CopyFieldContentMapper.PaymentPaypal(
            hasContentInSummary = { it.isPasswordEmpty.not() },
            hasContentInFull = { it.password?.toString().isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.password) }
        ),
        isSensitiveData = true
    ),

    BankAccountBank(
        SyncObjectType.BANK_STATEMENT,
        CopyFieldContentMapper.BankStatement(
            hasContentInSummary = { it.bankAccountBank.isValidBank() },
            hasContentInFull = { it.bankAccountBank.isValidBank() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.bankAccountBank) },
            getContentInFull = { CopyContent.Ready.StringValue(it.bankAccountBank) }
        )
    ),
    BankAccountBicSwift(
        SyncObjectType.BANK_STATEMENT,
        CopyFieldContentMapper.BankStatement(
            hasContentInSummary = { it.isBICEmpty.not() },
            hasContentInFull = { it.bankAccountBIC.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.bankAccountBIC) }
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
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.bankAccountIBAN) }
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
            getContentInSummary = { CopyContent.Ready.StringValue(it.addressFull) },
            getContentInFull = { CopyContent.Ready.StringValue(it.addressFull) }
        )
    ),
    City(
        SyncObjectType.ADDRESS,
        CopyFieldContentMapper.Address(
            hasContentInSummary = { it.city.isNotSemanticallyNull() },
            hasContentInFull = { it.city.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.city) },
            getContentInFull = { CopyContent.Ready.StringValue(it.city) }
        )
    ),
    ZipCode(
        SyncObjectType.ADDRESS,
        CopyFieldContentMapper.Address(
            hasContentInSummary = { it.zipCode.isNotSemanticallyNull() },
            hasContentInFull = { it.zipCode.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.zipCode) },
            getContentInFull = { CopyContent.Ready.StringValue(it.zipCode) }
        )
    ),

    IdsLinkedIdentity(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            hasContentInFull = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            getContentInSummary = { it.getLinkedIdentityContent() },
            getContentInFull = { it.getLinkedIdentityContent() }
        )
    ),
    IdsNumber(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.number) },
            getContentInFull = { CopyContent.Ready.StringValue(it.number) }
        )
    ),
    IdsIssueDate(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.Date(it.deliveryDate) }
        )
    ),
    IdsExpirationDate(
        SyncObjectType.ID_CARD,
        CopyFieldContentMapper.IdCard(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = { CopyContent.Ready.Date(it.expireDate) },
            getContentInFull = { CopyContent.Ready.Date(it.expireDate) }
        )
    ),

    PassportLinkedIdentity(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            hasContentInFull = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            getContentInSummary = { it.getLinkedIdentityContent() },
            getContentInFull = { it.getLinkedIdentityContent() }
        )
    ),
    PassportNumber(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.number) },
            getContentInFull = { CopyContent.Ready.StringValue(it.number) }
        )
    ),
    PassportIssueDate(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.Date(it.deliveryDate) }
        )
    ),
    PassportExpirationDate(
        SyncObjectType.PASSPORT,
        CopyFieldContentMapper.Passport(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = { CopyContent.Ready.Date(it.expireDate) },
            getContentInFull = { CopyContent.Ready.Date(it.expireDate) }
        )
    ),

    DriverLicenseLinkedIdentity(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            hasContentInFull = { it.fullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            getContentInSummary = { it.getLinkedIdentityContent() },
            getContentInFull = { it.getLinkedIdentityContent() }
        )
    ),
    DriverLicenseNumber(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.number) },
            getContentInFull = { CopyContent.Ready.StringValue(it.number) }
        )
    ),
    DriverLicenseIssueDate(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.deliveryDate != null },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.Date(it.deliveryDate) }
        )
    ),
    DriverLicenseExpirationDate(
        SyncObjectType.DRIVER_LICENCE,
        CopyFieldContentMapper.DriverLicence(
            hasContentInSummary = { it.expireDate != null },
            hasContentInFull = { it.expireDate != null },
            getContentInSummary = { CopyContent.Ready.Date(it.expireDate) },
            getContentInFull = { CopyContent.Ready.Date(it.expireDate) }
        )
    ),

    SocialSecurityLinkedIdentity(
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        CopyFieldContentMapper.SocialSecurityStatement(
            hasContentInSummary = { it.socialSecurityFullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            hasContentInFull = { it.socialSecurityFullname.isNotSemanticallyNull() || it.linkedIdentity.isNotSemanticallyNull() },
            getContentInSummary = { it.getLinkedIdentityContent() },
            getContentInFull = { it.getLinkedIdentityContent() }
        )
    ),
    SocialSecurityNumber(
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        CopyFieldContentMapper.SocialSecurityStatement(
            hasContentInSummary = { it.isSocialSecurityNumberEmpty.not() },
            hasContentInFull = { it.socialSecurityNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.ObfuscatedValue(it.socialSecurityNumber) }
        ),
        isSensitiveData = true
    ),

    TaxNumber(
        SyncObjectType.FISCAL_STATEMENT,
        CopyFieldContentMapper.FiscalStatement(
            hasContentInSummary = { it.fiscalNumber.isNotSemanticallyNull() },
            hasContentInFull = { it.fiscalNumber.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.fiscalNumber) },
            getContentInFull = { CopyContent.Ready.StringValue(it.fiscalNumber) }
        )
    ),
    TaxOnlineNumber(
        SyncObjectType.FISCAL_STATEMENT,
        CopyFieldContentMapper.FiscalStatement(
            hasContentInSummary = { throw ContentOnlyInSyncObjectException() },
            hasContentInFull = { it.teledeclarantNumber.isNotSemanticallyNull() },
            getContentInSummary = { throw ContentOnlyInSyncObjectException() },
            getContentInFull = { CopyContent.Ready.StringValue(it.teledeclarantNumber) }
        )
    ),

    JustEmail(
        SyncObjectType.EMAIL,
        CopyFieldContentMapper.EmailContent(
            hasContentInSummary = { it.email.isNotSemanticallyNull() },
            hasContentInFull = { it.email.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.email) },
            getContentInFull = { CopyContent.Ready.StringValue(it.email) }
        )
    ),

    PhoneNumber(
        SyncObjectType.PHONE,
        CopyFieldContentMapper.Phone(
            hasContentInSummary = { it.number.isNotSemanticallyNull() },
            hasContentInFull = { it.number.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.number) },
            getContentInFull = { CopyContent.Ready.StringValue(it.number) }
        )
    ),

    PersonalWebsite(
        SyncObjectType.PERSONAL_WEBSITE,
        CopyFieldContentMapper.PersonalWebsite(
            hasContentInSummary = { it.website.isNotSemanticallyNull() },
            hasContentInFull = { it.website.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.website) },
            getContentInFull = { CopyContent.Ready.StringValue(it.website) }
        )
    ),

    FirstName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.firstName.isNotSemanticallyNull() },
            hasContentInFull = { it.firstName.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.firstName) },
            getContentInFull = { CopyContent.Ready.StringValue(it.firstName) }
        )
    ),
    LastName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.lastName.isNotSemanticallyNull() },
            hasContentInFull = { it.lastName.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.lastName) },
            getContentInFull = { CopyContent.Ready.StringValue(it.lastName) }
        )
    ),
    MiddleName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.middleName.isNotSemanticallyNull() },
            hasContentInFull = { it.middleName.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.middleName) },
            getContentInFull = { CopyContent.Ready.StringValue(it.middleName) }
        )
    ),
    IdentityLogin(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.pseudo.isNotSemanticallyNull() },
            hasContentInFull = { it.pseudo.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.pseudo) },
            getContentInFull = { CopyContent.Ready.StringValue(it.pseudo) }
        )
    ),
    FullName(
        SyncObjectType.IDENTITY,
        CopyFieldContentMapper.Identity(
            hasContentInSummary = { it.fullName.isNotSemanticallyNull() },
            hasContentInFull = { it.fullName.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.fullName) },
            getContentInFull = { CopyContent.Ready.StringValue(it.fullName) }
        )
    ),
    CompanyName(
        SyncObjectType.COMPANY,
        CopyFieldContentMapper.Company(
            hasContentInSummary = { it.name.isNotSemanticallyNull() },
            hasContentInFull = { it.name.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.name) },
            getContentInFull = { CopyContent.Ready.StringValue(it.name) }
        )
    ),
    CompanyTitle(
        SyncObjectType.COMPANY,
        CopyFieldContentMapper.Company(
            hasContentInSummary = { it.jobTitle.isNotSemanticallyNull() },
            hasContentInFull = { it.jobTitle.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.jobTitle) },
            getContentInFull = { CopyContent.Ready.StringValue(it.jobTitle) }
        )
    ),
    PasskeyDisplayName(
        SyncObjectType.PASSKEY,
        CopyFieldContentMapper.Passkey(
            hasContentInSummary = { it.userDisplayName.isNotSemanticallyNull() },
            hasContentInFull = { it.userDisplayName.isNotSemanticallyNull() },
            getContentInSummary = { CopyContent.Ready.StringValue(it.userDisplayName) },
            getContentInFull = { CopyContent.Ready.StringValue(it.userDisplayName) }
        )
    );
}

private fun SyncObject.getLinkedIdentityContent(): CopyContent {
    val (fullName, linkedIdentityId) = when (this) {
        is SyncObject.Passport -> this.fullname to this.linkedIdentity
        is SyncObject.DriverLicence -> this.fullname to this.linkedIdentity
        is SyncObject.IdCard -> this.fullname to this.linkedIdentity
        is SyncObject.SocialSecurityStatement -> this.socialSecurityFullname to this.linkedIdentity
        else -> return CopyContent.Ready.StringValue(null)
    }
    return resolveIdentityContent(fullName, linkedIdentityId)
}

private fun SummaryObject.getLinkedIdentityContent(): CopyContent {
    val (fullName, linkedIdentityId) = when (this) {
        is SummaryObject.Passport -> this.fullname to this.linkedIdentity
        is SummaryObject.DriverLicence -> this.fullname to this.linkedIdentity
        is SummaryObject.IdCard -> this.fullname to this.linkedIdentity
        is SummaryObject.SocialSecurityStatement -> this.socialSecurityFullname to this.linkedIdentity
        else -> return CopyContent.Ready.StringValue(null)
    }
    return resolveIdentityContent(fullName, linkedIdentityId)
}

private fun resolveIdentityContent(
    fullName: String?,
    linkedIdentityId: String?
): CopyContent {
    return if (fullName.isNotSemanticallyNull()) {
        CopyContent.Ready.StringValue(fullName)
    } else if (linkedIdentityId.isNotSemanticallyNull()) {
        CopyContent.FromRemoteItem(
            uid = linkedIdentityId!!,
            syncObjectType = SyncObjectType.IDENTITY,
            copyField = CopyField.FullName
        )
    } else {
        CopyContent.Ready.StringValue("")
    }
}

private fun String?.isValidBank(): Boolean =
    isNotSemanticallyNull() && this != CreditCardBank.US_NO_TYPE