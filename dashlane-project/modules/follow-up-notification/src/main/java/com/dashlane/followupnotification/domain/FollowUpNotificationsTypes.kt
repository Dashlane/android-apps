package com.dashlane.followupnotification.domain

import androidx.annotation.DrawableRes
import com.dashlane.followupnotification.R
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country

enum class FollowUpNotificationsTypes(
    val syncObjectType: SyncObjectType,
    @DrawableRes val iconId: Int,
    vararg val copyField: CopyField
) {
    PASSWORDS(
        SyncObjectType.AUTHENTIFIANT,
        R.drawable.ic_thumbnail_password,
        CopyField.Login,
        CopyField.Email,
        CopyField.Password,
        CopyField.OtpCode
    ),
    ADDRESS(
        SyncObjectType.ADDRESS,
        R.drawable.ic_thumbnail_address,
        CopyField.Address,
        CopyField.City,
        CopyField.ZipCode
    ),
    BANK_ACCOUNT(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_thumbnail_bank_account,
        CopyField.BankAccountBank,
        CopyField.BankAccountBicSwift,
        CopyField.BankAccountIban
    ),
    BANK_ACCOUNT_US(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_thumbnail_bank_account,
        CopyField.BankAccountBank,
        CopyField.BankAccountRoutingNumber,
        CopyField.BankAccountAccountNumber
    ),
    BANK_ACCOUNT_MX(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_thumbnail_bank_account,
        CopyField.BankAccountBank,
        CopyField.BankAccountBicSwift,
        CopyField.BankAccountClabe
    ),
    BANK_ACCOUNT_GB(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_thumbnail_bank_account,
        CopyField.BankAccountBank,
        CopyField.BankAccountSortCode,
        CopyField.BankAccountAccountNumber
    ),
    DRIVERS_LICENSE(
        SyncObjectType.DRIVER_LICENCE,
        R.drawable.ic_thumbnail_driving_license,
        CopyField.DriverLicenseNumber,
        CopyField.DriverLicenseIssueDate,
        CopyField.DriverLicenseExpirationDate
    ),
    ID_CARD(
        SyncObjectType.ID_CARD,
        R.drawable.ic_thumbnail_id_card,
        CopyField.IdsNumber,
        CopyField.IdsIssueDate,
        CopyField.IdsExpirationDate
    ),
    PASSPORT(
        SyncObjectType.PASSPORT,
        R.drawable.ic_thumbnail_passport,
        CopyField.PassportNumber,
        CopyField.PassportIssueDate,
        CopyField.PassportExpirationDate
    ),
    PAYMENTS_CARD(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        R.drawable.ic_thumbnail_credit_card,
        CopyField.PaymentsNumber,
        CopyField.PaymentsSecurityCode,
        CopyField.PaymentsExpirationDate
    ),
    PAYPAL(
        SyncObjectType.PAYMENT_PAYPAL,
        R.drawable.ic_thumbnail_paypal,
        CopyField.PayPalLogin,
        CopyField.PayPalPassword
    )
}

internal fun SummaryObject.getFollowUpType(country: Country?): FollowUpNotificationsTypes? {
    return if (this is SummaryObject.BankStatement) {
        
        when (country) {
            Country.UnitedStates -> FollowUpNotificationsTypes.BANK_ACCOUNT_US
            Country.UnitedKingdom -> FollowUpNotificationsTypes.BANK_ACCOUNT_GB
            Country.Mexico -> FollowUpNotificationsTypes.BANK_ACCOUNT_MX
            else -> FollowUpNotificationsTypes.BANK_ACCOUNT
        }
    } else {
        FollowUpNotificationsTypes.values().firstOrNull { it.syncObjectType == syncObjectType }
    }
}