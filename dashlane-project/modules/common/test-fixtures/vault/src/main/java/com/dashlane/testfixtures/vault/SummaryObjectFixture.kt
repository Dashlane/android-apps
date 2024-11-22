package com.dashlane.testfixtures.vault

import com.dashlane.vault.summary.CommonSummary
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

object SummaryObjectFixture {
    private fun commonSummary(
        syncObjectType: SyncObjectType,
        id: String = "id",
    ): CommonSummary =
        CommonSummary(
            id = id,
            syncObjectType = syncObjectType,
            isShared = false,
            locallyUsedCount = 0,
            attachments = null,
            spaceId = null,
            creationDatetime = null,
            userModificationDatetime = null,
            locallyViewedDate = null,
            sharingPermission = null,
            syncState = null,
            isFavorite = false
        )

    val securityBreach: SummaryObject.SecurityBreach = SummaryObject.SecurityBreach(
        commonSummary = commonSummary(
            id = "id-security-breach",
            syncObjectType = SyncObjectType.SECURITY_BREACH
        ),
        breachId = null,
        content = null,
        contentRevision = null,
        status = null,
    )

    val secureNoteCategory: SummaryObject.SecureNoteCategory = SummaryObject.SecureNoteCategory(
        commonSummary = commonSummary(
            id = "id-secure-note-category",
            syncObjectType = SyncObjectType.SECURE_NOTE_CATEGORY
        ),
        categoryName = null,
    )

    val secureFileInfo: SummaryObject.SecureFileInfo = SummaryObject.SecureFileInfo(
        commonSummary = commonSummary(
            id = "id-secure-file-info",
            syncObjectType = SyncObjectType.SECURE_FILE_INFO
        ),
    )

    val generatedPassword: SummaryObject.GeneratedPassword = SummaryObject.GeneratedPassword(
        commonSummary = commonSummary(
            id = "id-generated-password",
            syncObjectType = SyncObjectType.GENERATED_PASSWORD
        ),
        authId = null,
        domain = null,
        generatedDate = null,
    )

    val dataChangeHistory: SummaryObject.DataChangeHistory = SummaryObject.DataChangeHistory(
        commonSummary = commonSummary(
            id = "id-data-change-history",
            syncObjectType = SyncObjectType.DATA_CHANGE_HISTORY
        ),
        objectId = null,
    )

    val collection: SummaryObject.Collection = SummaryObject.Collection(
        commonSummary = commonSummary(
            id = "id-collection",
            syncObjectType = SyncObjectType.COLLECTION
        ),
        name = null,
        vaultItems = null,
    )

    val authCategory: SummaryObject.AuthCategory = SummaryObject.AuthCategory(
        commonSummary = commonSummary(
            id = "id-auth-category",
            syncObjectType = SyncObjectType.AUTH_CATEGORY
        ),
        categoryName = null,
    )

    val socialSecurityStatement: SummaryObject.SocialSecurityStatement = SummaryObject.SocialSecurityStatement(
        commonSummary = commonSummary(
            id = "id-social-security-statement",
            syncObjectType = SyncObjectType.SOCIAL_SECURITY_STATEMENT
        ),
        linkedIdentity = null,
        dateOfBirth = null,
        isSocialSecurityNumberEmpty = true,
        socialSecurityFullname = null,
    )

    val secureNote: SummaryObject.SecureNote = SummaryObject.SecureNote(
        commonSummary = commonSummary(
            id = "id-secure-note",
            syncObjectType = SyncObjectType.SECURE_NOTE
        ),
        title = null,
        type = null,
        category = null,
        content = null,
        secured = null,
    )

    val phone: SummaryObject.Phone = SummaryObject.Phone(
        commonSummary = commonSummary(
            id = "id-phone",
            syncObjectType = SyncObjectType.PHONE
        ),
        phoneName = null,
        number = null,
    )

    val personalWebsite: SummaryObject.PersonalWebsite = SummaryObject.PersonalWebsite(
        commonSummary = commonSummary(
            id = "id-personal-website",
            syncObjectType = SyncObjectType.PERSONAL_WEBSITE
        ),
        name = null,
        website = null,
    )

    val paymentPaypal: SummaryObject.PaymentPaypal = SummaryObject.PaymentPaypal(
        commonSummary = commonSummary(
            id = "id-payment-paypal",
            syncObjectType = SyncObjectType.PAYMENT_PAYPAL
        ),
        login = null,
        name = null,
        isPasswordEmpty = true,
    )

    val paymentCreditCard: SummaryObject.PaymentCreditCard = SummaryObject.PaymentCreditCard(
        commonSummary = commonSummary(
            id = "id-payment-credit-card",
            syncObjectType = SyncObjectType.PAYMENT_CREDIT_CARD
        ),
        name = null,
        isCardNumberEmpty = true,
        isSecurityCodeEmpty = true,
        expireMonth = null,
        expireYear = null,
        linkedBillingAddress = null,
        color = null,
        ownerName = null,
        bank = null,
        cardNumberObfuscate = null,
        cardNumberLastFourDigits = null,
        creditCardTypeName = null,
    )

    val passport: SummaryObject.Passport = SummaryObject.Passport(
        commonSummary = commonSummary(
            id = "id-passport",
            syncObjectType = SyncObjectType.PASSPORT
        ),
        number = null,
        fullname = null,
        expireDate = null,
        linkedIdentity = null,
    )

    val passkey: SummaryObject.Passkey = SummaryObject.Passkey(
        commonSummary = commonSummary(
            id = "id-passkey",
            syncObjectType = SyncObjectType.PASSKEY
        ),
        rpId = null,
        credentialId = null,
        itemName = null,
        rpName = null,
        userDisplayName = null,
        note = null,
    )

    val identity: SummaryObject.Identity = SummaryObject.Identity(
        commonSummary = commonSummary(
            id = "id-identity",
            syncObjectType = SyncObjectType.IDENTITY
        ),
        firstName = null,
        lastName = null,
        middleName = null,
        pseudo = null,
        fullName = "John Doe",
        birthDate = null,
    )

    val idCard: SummaryObject.IdCard = SummaryObject.IdCard(
        commonSummary = commonSummary(
            id = "id-id-card",
            syncObjectType = SyncObjectType.ID_CARD
        ),
        number = null,
        fullname = null,
        expireDate = null,
        linkedIdentity = null,
    )

    val fiscalStatement: SummaryObject.FiscalStatement = SummaryObject.FiscalStatement(
        commonSummary = commonSummary(
            id = "id-fiscal-statement",
            syncObjectType = SyncObjectType.FISCAL_STATEMENT
        ),
        fiscalNumber = null,
        fullname = null,
        linkedIdentity = null,
    )

    val email: SummaryObject.Email = SummaryObject.Email(
        commonSummary = commonSummary(
            id = "id-email",
            syncObjectType = SyncObjectType.EMAIL
        ),
        email = null,
        emailName = null,
    )

    val driverLicence: SummaryObject.DriverLicence = SummaryObject.DriverLicence(
        commonSummary = commonSummary(
            id = "id-driver-licence",
            syncObjectType = SyncObjectType.DRIVER_LICENCE
        ),
        number = null,
        fullname = null,
        expireDate = null,
        linkedIdentity = null,
        state = null,
    )

    val address: SummaryObject.Address = SummaryObject.Address(
        commonSummary = commonSummary(
            id = "id-address",
            syncObjectType = SyncObjectType.ADDRESS
        ),
        addressFull = null,
        addressName = null,
        city = null,
        country = null,
        state = null,
        zipCode = null,
        streetName = null,
        building = null,
        floor = null,
        door = null,
    )

    val authentifiant: SummaryObject.Authentifiant = SummaryObject.Authentifiant(
        commonSummary = commonSummary(
            id = "id-authentifiant",
            syncObjectType = SyncObjectType.AUTHENTIFIANT
        ),
        login = null,
        secondaryLogin = null,
        email = null,
        useFixedUrl = null,
        userSelectedUrl = null,
        url = null,
        title = null,
        category = null,
        appMetaData = null,
        modificationDatetime = null,
        checked = null,
        note = null,
        isPasswordEmpty = false,
        linkedServices = null,
        hasOtpUrl = false,
    )

    val bankStatement: SummaryObject.BankStatement = SummaryObject.BankStatement(
        commonSummary = commonSummary(
            id = "id-bank-statement",
            syncObjectType = SyncObjectType.BANK_STATEMENT
        ),
        bankAccountName = null,
        bankAccountOwner = null,
        isIBANEmpty = false,
        isBICEmpty = false,
        bankAccountBank = null,
        bankAccountCountry = null
    )

    val company: SummaryObject.Company = SummaryObject.Company(
        commonSummary = commonSummary(
            id = "id-company",
            syncObjectType = SyncObjectType.COMPANY
        ),
        name = null,
        jobTitle = null,
        sirenNumber = null,
        siretNumber = null,
    )

    val secret: SummaryObject.Secret = SummaryObject.Secret(
        commonSummary = commonSummary(
            id = "id-secret",
            syncObjectType = SyncObjectType.SECRET
        ),
        title = null,
        content = null,
        secured = null,
    )
}