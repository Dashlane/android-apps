package com.dashlane.followupnotification.services

import android.content.Context
import com.dashlane.followupnotification.R
import com.dashlane.followupnotification.domain.FollowUpNotificationsTypes
import com.dashlane.util.clipboard.vault.CopyField
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject



class FollowUpNotificationsStringsImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : FollowUpNotificationsStrings {
    override fun getFollowUpNotificationsTypesLabels(followUpNotificationsTypes: FollowUpNotificationsTypes): String {
        return when (followUpNotificationsTypes) {
            FollowUpNotificationsTypes.ADDRESS -> context.getString(R.string.follow_up_notification_vault_item_label_address)
            FollowUpNotificationsTypes.PASSWORDS -> context.getString(R.string.follow_up_notification_vault_item_label_authentifiant)
            FollowUpNotificationsTypes.BANK_ACCOUNT,
            FollowUpNotificationsTypes.BANK_ACCOUNT_US,
            FollowUpNotificationsTypes.BANK_ACCOUNT_GB,
            FollowUpNotificationsTypes.BANK_ACCOUNT_MX -> context.getString(
                R.string.follow_up_notification_vault_item_label_bank
            )
            FollowUpNotificationsTypes.DRIVERS_LICENSE -> context.getString(R.string.follow_up_notification_vault_item_label_drivers_license)
            FollowUpNotificationsTypes.ID_CARD -> context.getString(R.string.follow_up_notification_vault_item_label_ids)
            FollowUpNotificationsTypes.PASSPORT -> context.getString(R.string.follow_up_notification_vault_item_label_passport)
            FollowUpNotificationsTypes.PAYMENTS_CARD -> context.getString(R.string.follow_up_notification_vault_item_label_credit_card)
            FollowUpNotificationsTypes.PAYPAL -> context.getString(R.string.follow_up_notification_vault_item_label_paypal)
        }
    }

    override fun getFieldLabel(copyField: CopyField): String? {
        val copyFieldResourceId = when (copyField) {
            CopyField.Password -> R.string.follow_up_notification_label_password
            CopyField.Login -> R.string.follow_up_notification_label_login
            CopyField.OtpCode -> R.string.follow_up_notification_label_otp_code
            CopyField.Email -> R.string.follow_up_notification_label_email
            CopyField.SecondaryLogin -> R.string.follow_up_notification_label_secondary_login
            CopyField.PaymentsNumber -> R.string.follow_up_notification_label_payments_number
            CopyField.PaymentsSecurityCode -> R.string.follow_up_notification_label_payments_security_code
            CopyField.PaymentsExpirationDate -> R.string.follow_up_notification_label_payments_expiration_date
            CopyField.PayPalLogin -> R.string.follow_up_notification_label_paypal_login
            CopyField.PayPalPassword -> R.string.follow_up_notification_label_paypal_password
            CopyField.BankAccountBank -> R.string.follow_up_notification_label_bankaccount_bank
            CopyField.BankAccountBicSwift -> R.string.follow_up_notification_label_bankaccount_bic_swift
            CopyField.BankAccountRoutingNumber -> R.string.follow_up_notification_label_bankaccount_routing_number
            CopyField.BankAccountSortCode -> R.string.follow_up_notification_label_bankaccount_sort_code
            CopyField.BankAccountIban -> R.string.follow_up_notification_label_bankaccount_iban
            CopyField.BankAccountAccountNumber -> R.string.follow_up_notification_label_bankaccount_account_number
            CopyField.BankAccountClabe -> R.string.follow_up_notification_label_bankaccount_clabe
            CopyField.Address -> R.string.follow_up_notification_label_address
            CopyField.City -> R.string.follow_up_notification_label_city
            CopyField.ZipCode -> R.string.follow_up_notification_label_zip_code
            CopyField.IdsNumber -> R.string.follow_up_notification_label_ids_number
            CopyField.IdsIssueDate -> R.string.follow_up_notification_label_ids_issue_date
            CopyField.IdsExpirationDate -> R.string.follow_up_notification_label_ids_expiration_date
            CopyField.PassportNumber -> R.string.follow_up_notification_label_passport_number
            CopyField.PassportIssueDate -> R.string.follow_up_notification_label_passport_issue_date
            CopyField.PassportExpirationDate -> R.string.follow_up_notification_label_passport_expiration_date
            CopyField.DriverLicenseNumber -> R.string.follow_up_notification_label_driverlicense_number
            CopyField.DriverLicenseIssueDate -> R.string.follow_up_notification_label_driverlicense_issue_date
            CopyField.DriverLicenseExpirationDate -> R.string.follow_up_notification_label_driverlicense_expiration_date
            else -> null
        } ?: return null

        return context.getString(copyFieldResourceId)
    }

    override fun getFollowUpNotificationsTitle(): String {
        return context.getString(R.string.follow_up_settings_title)
    }

    override fun getFollowUpNotificationsDescription(): String {
        return context.getString(R.string.follow_up_settings_description)
    }
}