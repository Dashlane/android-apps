package com.dashlane.vault.summary

import com.dashlane.util.CreditCardType
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.cardNumberLastFourDigits
import com.dashlane.vault.model.cardNumberObfuscate
import com.dashlane.vault.model.fullName
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.objectType

fun List<SummaryObject>.groupBySyncObjectType() = groupBy { it.syncObjectType }

fun <T : SummaryObject> VaultItem<*>.toSummary(): T =
    toSummaryOrNull()
        ?: throw IllegalStateException("Unsupported SummaryObject type ${this.syncObject.objectType}")

@Suppress("UNCHECKED_CAST")
fun <T : SummaryObject> VaultItem<*>.toSummaryOrNull(): T? {
    val summaryObject: SummaryObject =
        when (this.syncObject) {
            is SyncObject.Authentifiant -> (this as VaultItem<SyncObject.Authentifiant>).toSummaryOrNull()
            is SyncObject.Address -> (this as VaultItem<SyncObject.Address>).toSummaryOrNull()
            is SyncObject.AuthCategory -> (this as VaultItem<SyncObject.AuthCategory>).toSummaryOrNull()
            is SyncObject.BankStatement -> (this as VaultItem<SyncObject.BankStatement>).toSummaryOrNull()
            is SyncObject.Company -> (this as VaultItem<SyncObject.Company>).toSummaryOrNull()
            is SyncObject.Collection -> (this as VaultItem<SyncObject.Collection>).toSummaryOrNull()
            is SyncObject.DriverLicence -> (this as VaultItem<SyncObject.DriverLicence>).toSummaryOrNull()
            is SyncObject.Email -> (this as VaultItem<SyncObject.Email>).toSummaryOrNull()
            is SyncObject.FiscalStatement -> (this as VaultItem<SyncObject.FiscalStatement>).toSummaryOrNull()
            is SyncObject.IdCard -> (this as VaultItem<SyncObject.IdCard>).toSummaryOrNull()
            is SyncObject.Identity -> (this as VaultItem<SyncObject.Identity>).toSummaryOrNull()
            is SyncObject.Passkey -> (this as VaultItem<SyncObject.Passkey>).toSummaryOrNull()
            is SyncObject.Passport -> (this as VaultItem<SyncObject.Passport>).toSummaryOrNull()
            is SyncObject.PaymentCreditCard -> (this as VaultItem<SyncObject.PaymentCreditCard>).toSummaryOrNull()
            is SyncObject.PersonalWebsite -> (this as VaultItem<SyncObject.PersonalWebsite>).toSummaryOrNull()
            is SyncObject.Phone -> (this as VaultItem<SyncObject.Phone>).toSummaryOrNull()
            is SyncObject.SecureFileInfo -> (this as VaultItem<SyncObject.SecureFileInfo>).toSummaryOrNull()
            is SyncObject.SecureNote -> (this as VaultItem<SyncObject.SecureNote>).toSummaryOrNull()
            is SyncObject.SecureNoteCategory -> (this as VaultItem<SyncObject.SecureNoteCategory>).toSummaryOrNull()
            is SyncObject.SecurityBreach -> (this as VaultItem<SyncObject.SecurityBreach>).toSummaryOrNull()
            is SyncObject.SocialSecurityStatement -> (this as VaultItem<SyncObject.SocialSecurityStatement>).toSummaryOrNull()
            is SyncObject.GeneratedPassword -> (this as VaultItem<SyncObject.GeneratedPassword>).toSummaryOrNull()
            is SyncObject.DataChangeHistory -> (this as VaultItem<SyncObject.DataChangeHistory>).toSummaryOrNull()
            is SyncObject.Secret -> (this as VaultItem<SyncObject.Secret>).toSummaryOrNull()
            is SyncObject.WiFi -> (this as VaultItem<SyncObject.WiFi>).toSummaryOrNull()
            else -> return null
        }
    return summaryObject as? T
}

private fun VaultItem<*>.toCommonSummary(): CommonSummary = CommonSummary(
    id = this.uid,
    syncObjectType = this.syncObject.objectType,
    spaceId = this.syncObject.spaceId,
    isShared = this.isShared(),
    attachments = this.syncObject.attachments,
    creationDatetime = this.syncObject.creationDatetime,
    userModificationDatetime = this.syncObject.userModificationDatetime,
    locallyViewedDate = this.locallyViewedDate,
    locallyUsedCount = this.locallyUsedCount,
    sharingPermission = this.sharingPermission,
    syncState = this.syncState,
    isFavorite = this.syncObject.isFavorite == true,
)

private fun VaultItem<SyncObject.DataChangeHistory>.toSummaryOrNull() =
    SummaryObject.DataChangeHistory(
        commonSummary = toCommonSummary(),
        objectId = this.syncObject.objectId,
    )

private fun VaultItem<SyncObject.Authentifiant>.toSummaryOrNull() =
    SummaryObject.Authentifiant(
        commonSummary = toCommonSummary(),
        login = this.syncObject.login,
        secondaryLogin = this.syncObject.secondaryLogin,
        email = this.syncObject.email,
        useFixedUrl = this.syncObject.useFixedUrl,
        userSelectedUrl = this.syncObject.userSelectedUrl,
        url = this.syncObject.url,
        title = this.syncObject.title,
        category = this.syncObject.category,
        appMetaData = this.syncObject.appMetaData?.toSummaryOrNull(),
        modificationDatetime = this.syncObject.modificationDatetime,
        checked = this.syncObject.checked,
        note = this.syncObject.note,
        isPasswordEmpty = this.syncObject.password.isNullOrEmpty(),
        linkedServices = this.syncObject.linkedServices?.toSummaryOrNull(),
        hasOtpUrl = this.syncObject.otpUrl.isNotSemanticallyNull() || this.syncObject.otpSecret.isNotSemanticallyNull(),
        lastUsed = this.syncObject.lastUse,
    )

private fun VaultItem<SyncObject.Address>.toSummaryOrNull() =
    SummaryObject.Address(
        commonSummary = toCommonSummary(),
        addressFull = this.syncObject.addressFull,
        addressName = this.syncObject.addressName,
        city = this.syncObject.city,
        country = this.syncObject.country,
        state = this.syncObject.state,
        zipCode = this.syncObject.zipCode,
        streetName = this.syncObject.streetName,
        building = this.syncObject.building,
        floor = this.syncObject.floor,
        door = this.syncObject.door,
    )

private fun VaultItem<SyncObject.AuthCategory>.toSummaryOrNull() =
    SummaryObject.AuthCategory(
        commonSummary = toCommonSummary(),
        categoryName = this.syncObject.categoryName,
    )

private fun VaultItem<SyncObject.SecureNoteCategory>.toSummaryOrNull() =
    SummaryObject.SecureNoteCategory(
        commonSummary = toCommonSummary(),
        categoryName = this.syncObject.categoryName,
    )

private fun VaultItem<SyncObject.SecureNote>.toSummaryOrNull() =
    SummaryObject.SecureNote(
        commonSummary = toCommonSummary(),
        title = this.syncObject.title,
        type = this.syncObject.type,
        category = this.syncObject.category,
        content = this.syncObject.content,
        secured = this.syncObject.secured,
    )

private fun VaultItem<SyncObject.BankStatement>.toSummaryOrNull() =
    SummaryObject.BankStatement(
        commonSummary = toCommonSummary(),
        bankAccountBank = this.syncObject.bankAccountBank,
        isBICEmpty = this.syncObject.bankAccountBIC.isNullOrEmpty(),
        isIBANEmpty = this.syncObject.bankAccountIBAN.isNullOrEmpty(),
        bankAccountName = this.syncObject.bankAccountName,
        bankAccountOwner = this.syncObject.bankAccountOwner,
        bankAccountCountry = this.syncObject.localeFormat,
    )

private fun VaultItem<SyncObject.Company>.toSummaryOrNull() =
    SummaryObject.Company(
        commonSummary = toCommonSummary(),
        jobTitle = this.syncObject.jobTitle,
        name = this.syncObject.name,
        sirenNumber = this.syncObject.sirenNumber,
        siretNumber = this.syncObject.siretNumber,
    )

private fun VaultItem<SyncObject.DriverLicence>.toSummaryOrNull() =
    SummaryObject.DriverLicence(
        commonSummary = toCommonSummary(),
        expireDate = this.syncObject.expireDate,
        fullname = this.syncObject.fullname,
        linkedIdentity = this.syncObject.linkedIdentity,
        number = this.syncObject.number,
        state = this.syncObject.state,
    )

private fun VaultItem<SyncObject.Email>.toSummaryOrNull() =
    SummaryObject.Email(
        commonSummary = toCommonSummary(),
        email = this.syncObject.email,
        emailName = this.syncObject.emailName,
    )

private fun VaultItem<SyncObject.FiscalStatement>.toSummaryOrNull() =
    SummaryObject.FiscalStatement(
        commonSummary = toCommonSummary(),
        fiscalNumber = this.syncObject.fiscalNumber,
        linkedIdentity = this.syncObject.linkedIdentity,
        fullname = this.syncObject.fullname,
    )

private fun VaultItem<SyncObject.IdCard>.toSummaryOrNull() =
    SummaryObject.IdCard(
        commonSummary = toCommonSummary(),
        number = this.syncObject.number,
        linkedIdentity = this.syncObject.linkedIdentity,
        fullname = this.syncObject.fullname,
        expireDate = this.syncObject.expireDate,
    )

private fun VaultItem<SyncObject.Identity>.toSummaryOrNull() =
    SummaryObject.Identity(
        commonSummary = toCommonSummary(),
        birthDate = this.syncObject.birthDate,
        firstName = this.syncObject.firstName,
        fullName = this.syncObject.fullName,
        lastName = this.syncObject.lastName,
        middleName = this.syncObject.middleName,
        pseudo = this.syncObject.pseudo,
    )

private fun VaultItem<SyncObject.Passport>.toSummaryOrNull() =
    SummaryObject.Passport(
        commonSummary = toCommonSummary(),
        expireDate = this.syncObject.expireDate,
        fullname = this.syncObject.fullname,
        linkedIdentity = this.syncObject.linkedIdentity,
        number = this.syncObject.number,
    )

private fun VaultItem<SyncObject.PaymentCreditCard>.toSummaryOrNull() =
    SummaryObject.PaymentCreditCard(
        commonSummary = toCommonSummary(),
        name = this.syncObject.name,
        isCardNumberEmpty = this.syncObject.cardNumber.isNullOrEmpty(),
        color = this.syncObject.color,
        expireMonth = this.syncObject.expireMonth,
        expireYear = this.syncObject.expireYear,
        linkedBillingAddress = this.syncObject.linkedBillingAddress,
        isSecurityCodeEmpty = this.syncObject.securityCode.isNullOrEmpty(),
        bank = this.syncObject.bank,
        ownerName = this.syncObject.ownerName,
        cardNumberObfuscate = this.syncObject.cardNumberObfuscate,
        cardNumberLastFourDigits = this.syncObject.cardNumberLastFourDigits,
        creditCardTypeName = CreditCardType.fromCardNumber(this.syncObject.cardNumber)?.typeName,
        country = this.syncObject.localeFormat,
        isNoteEmpty = this.syncObject.cCNote.isNullOrEmpty(),
    )

private fun VaultItem<SyncObject.PersonalWebsite>.toSummaryOrNull() =
    SummaryObject.PersonalWebsite(
        commonSummary = toCommonSummary(),
        name = this.syncObject.name,
        website = this.syncObject.website,
    )

private fun VaultItem<SyncObject.Phone>.toSummaryOrNull() =
    SummaryObject.Phone(
        commonSummary = toCommonSummary(),
        number = this.syncObject.number,
        phoneName = this.syncObject.phoneName,
    )

private fun VaultItem<SyncObject.SocialSecurityStatement>.toSummaryOrNull() =
    SummaryObject.SocialSecurityStatement(
        commonSummary = toCommonSummary(),
        linkedIdentity = this.syncObject.linkedIdentity,
        dateOfBirth = this.syncObject.dateOfBirth,
        socialSecurityFullname = this.syncObject.socialSecurityFullname,
        isSocialSecurityNumberEmpty = this.syncObject.socialSecurityNumber.isNullOrEmpty(),
    )

private fun VaultItem<SyncObject.SecureFileInfo>.toSummaryOrNull() =
    SummaryObject.SecureFileInfo(
        commonSummary = toCommonSummary(),
    )

private fun VaultItem<SyncObject.SecurityBreach>.toSummaryOrNull() =
    SummaryObject.SecurityBreach(
        commonSummary = toCommonSummary(),
        content = this.syncObject.content,
        breachId = this.syncObject.breachId,
        contentRevision = this.syncObject.contentRevision,
        status = this.syncObject.status?.toSummaryOrNull(),
    )

private fun VaultItem<SyncObject.GeneratedPassword>.toSummaryOrNull() =
    SummaryObject.GeneratedPassword(
        commonSummary = toCommonSummary(),
        authId = this.syncObject.authId,
        generatedDate = this.syncObject.generatedDate,
        domain = this.syncObject.domain,
    )

private fun SyncObject.Authentifiant.LinkedServices.toSummaryOrNull() =
    SummaryObject.LinkedServices(
        associatedAndroidApps = associatedAndroidApps?.map { it.toSummaryOrNull() } ?: listOf(),
        associatedDomains = associatedDomains?.map { it.toSummaryOrNull() } ?: listOf(),
    )

private fun SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.toSummaryOrNull() =
    SummaryObject.LinkedServices.AssociatedAndroidApps(
        linkSource.toSummaryOrNull(),
        packageName,
        name,
        sha256CertFingerprints ?: listOf(),
        sha512CertFingerprints ?: listOf(),
    )

private fun SyncObject.Authentifiant.LinkedServices.AssociatedDomains.toSummaryOrNull() =
    SummaryObject.LinkedServices.AssociatedDomains(domain ?: "", source.toSummaryOrNull())

private fun SyncObject.Authentifiant.AppMetaData.toSummaryOrNull() =
    SummaryObject.AppMetaData(androidLinkedApplications = androidLinkedApplications?.map { it.toSummaryOrNull() })

private fun SyncObject.Authentifiant.AppMetaData.AndroidLinkedApplications.toSummaryOrNull() =
    SummaryObject.AppMetaData.AndroidLinkedApplications(
        packageName,
        sha256CertFingerprints,
        sha512CertFingerprints,
    )

private fun SyncObject.SecurityBreach.Status.toSummaryOrNull() =
    when (this) {
        SyncObject.SecurityBreach.Status.VIEWED -> SummaryObject.SecurityBreach.Status.VIEWED
        SyncObject.SecurityBreach.Status.ACKNOWLEDGED -> SummaryObject.SecurityBreach.Status.ACKNOWLEDGED
        SyncObject.SecurityBreach.Status.SOLVED -> SummaryObject.SecurityBreach.Status.SOLVED
        SyncObject.SecurityBreach.Status.PENDING -> SummaryObject.SecurityBreach.Status.PENDING
    }

private fun SyncObject.Authentifiant.LinkedServices.AssociatedDomains.Source?.toSummaryOrNull() =
    when (this) {
        SyncObject.Authentifiant.LinkedServices.AssociatedDomains.Source.REMEMBER ->
            SummaryObject.LinkedServices.AssociatedDomainsSource.REMEMBER

        else -> SummaryObject.LinkedServices.AssociatedDomainsSource.MANUAL
    }

private fun SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource?.toSummaryOrNull() =
    when (this) {
        SyncObject.Authentifiant.LinkedServices.AssociatedAndroidApps.LinkSource.DASHLANE ->
            SummaryObject.LinkedServices.AssociatedAppsSource.DASHLANE
        else -> SummaryObject.LinkedServices.AssociatedAppsSource.USER
    }

private fun VaultItem<SyncObject.Collection>.toSummaryOrNull() =
    SummaryObject.Collection(
        commonSummary = toCommonSummary(),
        name = this.syncObject.name,
        vaultItems = this.syncObject.vaultItems?.map { it.toSummaryOrNull() },
    )

private fun SyncObject.Collection.VaultItems.toSummaryOrNull() = CollectionVaultItems(type, id)

private fun VaultItem<SyncObject.Passkey>.toSummaryOrNull() =
    SummaryObject.Passkey(
        commonSummary = toCommonSummary(),
        itemName = this.syncObject.itemName,
        credentialId = this.syncObject.credentialId,
        rpId = this.syncObject.rpId,
        rpName = this.syncObject.rpName,
        userDisplayName = this.syncObject.userDisplayName,
        keyAlgorithm = this.syncObject.keyAlgorithm,
        note = this.syncObject.note,
    )

private fun VaultItem<SyncObject.Secret>.toSummaryOrNull() =
    SummaryObject.Secret(
        commonSummary = toCommonSummary(),
        title = this.syncObject.title,
        content = this.syncObject.content,
        secured = this.syncObject.secured,
    )

private fun VaultItem<SyncObject.WiFi>.toSummaryOrNull() =
    SummaryObject.WiFi(
        commonSummary = toCommonSummary(),
        ssid = this.syncObject.ssid,
        name = this.syncObject.name,
        note = this.syncObject.note,
        hidden = this.syncObject.hidden,
        isPassphraseEmpty = this.syncObject.passphrase.isNullOrEmpty(),
        encryptionType = when (this.syncObject.encryptionType) {
            SyncObject.WiFiEncryptionType.WEP -> SummaryObject.WiFi.EncryptionType.WEP
            SyncObject.WiFiEncryptionType.WPA_PERSONAL -> SummaryObject.WiFi.EncryptionType.WPA_PERSONAL
            SyncObject.WiFiEncryptionType.WPA2_PERSONAL -> SummaryObject.WiFi.EncryptionType.WPA2_PERSONAL
            SyncObject.WiFiEncryptionType.WPA3_PERSONAL -> SummaryObject.WiFi.EncryptionType.WPA3_PERSONAL
            SyncObject.WiFiEncryptionType.UNSECURED -> SummaryObject.WiFi.EncryptionType.UNSECURED
            null -> null
        },
    )