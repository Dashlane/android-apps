package com.dashlane.followupnotification.domain

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.dashlane.followupnotification.R
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.toBitmap
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.utils.Country

enum class FollowUpNotificationsTypes(
    val syncObjectType: SyncObjectType,
    @DrawableRes val iconId: Int,
    vararg val copyField: CopyField,
    var icon: Bitmap? = null,
) {
    PASSWORDS(
        SyncObjectType.AUTHENTIFIANT,
        R.drawable.ic_item_login_outlined,
        CopyField.Login,
        CopyField.Email,
        CopyField.Password,
        CopyField.OtpCode
    ),
    ADDRESS(
        SyncObjectType.ADDRESS,
        R.drawable.ic_home_outlined,
        CopyField.Address,
        CopyField.City,
        CopyField.ZipCode
    ),
    BANK_ACCOUNT(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_item_bank_account_outlined,
        CopyField.BankAccountBank,
        CopyField.BankAccountBicSwift,
        CopyField.BankAccountIban
    ),
    BANK_ACCOUNT_US(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_item_bank_account_outlined,
        CopyField.BankAccountBank,
        CopyField.BankAccountRoutingNumber,
        CopyField.BankAccountAccountNumber
    ),
    BANK_ACCOUNT_MX(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_item_bank_account_outlined,
        CopyField.BankAccountBank,
        CopyField.BankAccountBicSwift,
        CopyField.BankAccountClabe
    ),
    BANK_ACCOUNT_GB(
        SyncObjectType.BANK_STATEMENT,
        R.drawable.ic_item_bank_account_outlined,
        CopyField.BankAccountBank,
        CopyField.BankAccountSortCode,
        CopyField.BankAccountAccountNumber
    ),
    DRIVERS_LICENSE(
        SyncObjectType.DRIVER_LICENCE,
        R.drawable.ic_item_drivers_license_outlined,
        CopyField.DriverLicenseNumber,
        CopyField.DriverLicenseIssueDate,
        CopyField.DriverLicenseExpirationDate
    ),
    ID_CARD(
        SyncObjectType.ID_CARD,
        R.drawable.ic_item_personal_info_outlined,
        CopyField.IdsNumber,
        CopyField.IdsIssueDate,
        CopyField.IdsExpirationDate
    ),
    PASSPORT(
        SyncObjectType.PASSPORT,
        R.drawable.ic_item_passport_outlined,
        CopyField.PassportNumber,
        CopyField.PassportIssueDate,
        CopyField.PassportExpirationDate
    ),
    PAYMENTS_CARD(
        SyncObjectType.PAYMENT_CREDIT_CARD,
        R.drawable.ic_item_payment_outlined,
        CopyField.PaymentsNumber,
        CopyField.PaymentsSecurityCode,
        CopyField.PaymentsExpirationDate
    )
}

internal fun SummaryObject.getFollowUpType(context: Context, country: Country?): FollowUpNotificationsTypes? {
    return if (this is SummaryObject.BankStatement) {
        
        when (country) {
            Country.UnitedStates -> FollowUpNotificationsTypes.BANK_ACCOUNT_US
            Country.UnitedKingdom -> FollowUpNotificationsTypes.BANK_ACCOUNT_GB
            Country.Mexico -> FollowUpNotificationsTypes.BANK_ACCOUNT_MX
            else -> FollowUpNotificationsTypes.BANK_ACCOUNT
        }
    } else {
        FollowUpNotificationsTypes.values().firstOrNull { it.syncObjectType == syncObjectType }
    }?.also {
        it.icon = AppCompatResources.getDrawable(context, it.iconId)?.apply {
            setTint(context.resources.getColor(R.color.text_neutral_standard, context.theme))
        }?.toBitmap()
    }
}