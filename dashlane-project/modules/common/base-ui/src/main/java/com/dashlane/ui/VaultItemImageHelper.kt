package com.dashlane.ui

import com.dashlane.design.component.compat.view.ThumbnailViewType
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObjectType

object VaultItemImageHelper {

    data class ThumbnailViewConfiguration(
        val type: ThumbnailViewType,
        val iconRes: Int?,
        val colorRes: Int?,
        val urlDomain: String?,
    )

    fun getThumbnailViewConfiguration(
        syncObject: SummaryObject
    ) = ThumbnailViewConfiguration(
        type = syncObject.getThumbnailViewType(),
        iconRes = syncObject.syncObjectType.getThumbnailIcon(),
        colorRes = syncObject.getThumbnailColor(),
        urlDomain = syncObject.getUrlDomain()
    )

    fun SyncObjectType.getThumbnailIcon(): Int? = when {
        this == SyncObjectType.PAYMENT_CREDIT_CARD -> R.drawable.ic_item_payment_outlined
        this == SyncObjectType.SECURE_NOTE -> R.drawable.ic_item_secure_note_outlined
        this == SyncObjectType.ADDRESS -> R.drawable.ic_home_outlined
        this == SyncObjectType.BANK_STATEMENT -> R.drawable.ic_item_bank_account_outlined
        this == SyncObjectType.COMPANY -> R.drawable.ic_item_company_outlined
        this == SyncObjectType.DRIVER_LICENCE -> R.drawable.ic_item_drivers_license_outlined
        this == SyncObjectType.EMAIL -> R.drawable.ic_item_email_outlined
        this == SyncObjectType.FISCAL_STATEMENT -> R.drawable.ic_item_tax_number_outlined
        this == SyncObjectType.ID_CARD -> R.drawable.ic_item_id_outlined
        this == SyncObjectType.IDENTITY -> R.drawable.ic_item_id_outlined
        this == SyncObjectType.PASSPORT -> R.drawable.ic_item_passport_outlined
        this == SyncObjectType.PAYMENT_PAYPAL -> R.drawable.ic_fab_menu_paypal
        this == SyncObjectType.PERSONAL_WEBSITE -> R.drawable.ic_web_outlined
        this == SyncObjectType.PHONE -> R.drawable.ic_item_phone_mobile_outlined
        this == SyncObjectType.SOCIAL_SECURITY_STATEMENT -> R.drawable.ic_item_social_security_outlined
        this == SyncObjectType.SECRET -> R.drawable.ic_item_secret_outlined
        else -> null
    }

    private fun SummaryObject.getThumbnailViewType(): ThumbnailViewType =
        when (this) {
            is SummaryObject.Authentifiant,
            is SummaryObject.Passkey -> ThumbnailViewType.VAULT_ITEM_DOMAIN_ICON
            is SummaryObject.SecureNote,
            is SummaryObject.PaymentCreditCard -> ThumbnailViewType.VAULT_ITEM_LEGACY_OTHER_ICON
            else -> ThumbnailViewType.VAULT_ITEM_OTHER_ICON
        }

    private fun SummaryObject.getThumbnailColor(): Int? =
        when (this) {
            is SummaryObject.SecureNote -> this.type.getColorId()
            is SummaryObject.PaymentCreditCard -> this.color.getColorResource()
            else -> null
        }

    private fun SummaryObject.getUrlDomain(): String? =
        when (this) {
            is SummaryObject.Authentifiant -> this.urlForUI()
            is SummaryObject.Passkey -> this.urlForGoToWebsite
            else -> null
        }
}