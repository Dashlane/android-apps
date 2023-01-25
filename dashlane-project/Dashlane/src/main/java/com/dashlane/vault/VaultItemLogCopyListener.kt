package com.dashlane.vault

import com.dashlane.followupnotification.data.FollowUpNotificationRepository
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.Highlight
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyListener
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.isProtected
import javax.inject.Inject

class VaultItemLogCopyListener @Inject constructor(
    private val vaultItemLogger: VaultItemLogger,
    private val followUpNotificationRepository: FollowUpNotificationRepository
) : VaultItemCopyListener {
    override fun onCopyFromVault(
        summaryObject: SummaryObject,
        copyField: CopyField,
        highlight: Highlight?,
        index: Double?,
        totalCount: Int?
    ) {
        val (field, itemType) = copyField.toFieldItemType()

        logCopy(highlight, field, summaryObject.id, index, totalCount, itemType, summaryObject.isProtected)
    }

    override fun onCopyFromFollowUpNotification(notificationId: String, copyField: CopyField) {
        val notification = followUpNotificationRepository.get(notificationId) ?: return
        val (field, itemType) = copyField.toFieldItemType()
        logCopy(null, field, notification.vaultItemId, null, null, itemType, notification.isItemProtected)
    }

    private fun logCopy(
        highlight: Highlight?,
        field: Field,
        vaultItemId: String,
        index: Double?,
        totalCount: Int?,
        itemType: ItemType,
        isItemProtected: Boolean
    ) {
        vaultItemLogger.logCopyField(highlight, field, vaultItemId, itemType, totalCount, index, isItemProtected, null)
    }
}

@SuppressWarnings("kotlin:S1479")
fun CopyField.toFieldItemType() = when (this) {
    CopyField.Password -> Field.PASSWORD to ItemType.CREDENTIAL
    CopyField.Login -> Field.LOGIN to ItemType.CREDENTIAL
    CopyField.Email -> Field.EMAIL to ItemType.CREDENTIAL
    CopyField.SecondaryLogin -> Field.SECONDARY_LOGIN to ItemType.CREDENTIAL
    CopyField.OtpCode -> Field.OTP_SECRET to ItemType.CREDENTIAL
    CopyField.PaymentsNumber -> Field.CARD_NUMBER to ItemType.CREDIT_CARD
    CopyField.PaymentsSecurityCode -> Field.SECURITY_CODE to ItemType.CREDIT_CARD
    CopyField.PaymentsExpirationDate -> Field.EXPIRE_DATE to ItemType.CREDIT_CARD
    CopyField.BankAccountBank -> Field.NAME to ItemType.BANK_STATEMENT
    CopyField.BankAccountBicSwift, CopyField.BankAccountRoutingNumber, CopyField.BankAccountSortCode -> Field.BIC to ItemType.BANK_STATEMENT
    CopyField.BankAccountIban, CopyField.BankAccountAccountNumber, CopyField.BankAccountClabe -> Field.IBAN to ItemType.BANK_STATEMENT
    CopyField.Address -> Field.ADDRESS_NAME to ItemType.ADDRESS
    CopyField.City -> Field.CITY to ItemType.ADDRESS
    CopyField.ZipCode -> Field.ZIP_CODE to ItemType.ADDRESS
    CopyField.IdsNumber -> Field.NUMBER to ItemType.ID_CARD
    CopyField.IdsIssueDate -> Field.DELIVERY_DATE to ItemType.ID_CARD
    CopyField.IdsExpirationDate -> Field.EXPIRE_DATE to ItemType.ID_CARD
    CopyField.PassportNumber -> Field.NUMBER to ItemType.PASSPORT
    CopyField.PassportIssueDate -> Field.DELIVERY_DATE to ItemType.PASSPORT
    CopyField.PassportExpirationDate -> Field.EXPIRE_DATE to ItemType.PASSPORT
    CopyField.DriverLicenseNumber -> Field.NUMBER to ItemType.DRIVER_LICENCE
    CopyField.DriverLicenseIssueDate -> Field.DELIVERY_DATE to ItemType.DRIVER_LICENCE
    CopyField.DriverLicenseExpirationDate -> Field.EXPIRE_DATE to ItemType.DRIVER_LICENCE
    CopyField.SocialSecurityNumber -> Field.SOCIAL_SECURITY_NUMBER to ItemType.SOCIAL_SECURITY
    CopyField.TaxOnlineNumber -> Field.TELEDECLARANT_NUMBER to ItemType.SOCIAL_SECURITY
    CopyField.TaxNumber -> Field.FISCAL_NUMBER to ItemType.FISCAL_STATEMENT
    CopyField.JustEmail -> Field.EMAIL to ItemType.EMAIL
    CopyField.PhoneNumber -> Field.NUMBER to ItemType.PHONE
    CopyField.PersonalWebsite -> Field.WEBSITE to ItemType.WEBSITE
    CopyField.PayPalPassword -> Field.PASSWORD to ItemType.PAYPAL
    CopyField.PayPalLogin -> Field.LOGIN to ItemType.PAYPAL
    CopyField.FirstName -> Field.FIRST_NAME to ItemType.IDENTITY
    CopyField.LastName -> Field.LAST_NAME to ItemType.IDENTITY
    CopyField.MiddleName -> Field.MIDDLE_NAME to ItemType.IDENTITY
    CopyField.IdentityLogin -> Field.LOGIN to ItemType.IDENTITY
    CopyField.CompanyName -> Field.NAME to ItemType.COMPANY
    CopyField.CompanyTitle -> Field.JOB_TITLE to ItemType.COMPANY
}