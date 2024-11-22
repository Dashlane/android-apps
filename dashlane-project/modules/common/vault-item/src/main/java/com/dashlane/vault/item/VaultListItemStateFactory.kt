package com.dashlane.vault.item

import com.dashlane.design.iconography.IconToken
import com.dashlane.design.iconography.IconTokens
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.model.getColorResource
import com.dashlane.vault.model.urlForGoToWebsite
import com.dashlane.vault.model.urlForUI
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class VaultListItemStateFactory @Inject constructor(
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver
) {
    fun buildVaultListItemState(item: SummaryObject): VaultListItemState {
        val description = dataIdentifierListTextResolver.getLine2(item)

        return VaultListItemState(
            id = item.id,
            title = dataIdentifierListTextResolver.getLine1(item).text,
            description = description.text,
            hasError = description.isWarning,
            thumbnail = buildThumbnailType(item),
        )
    }

    private fun buildThumbnailType(item: SummaryObject): VaultListItemState.ThumbnailState? {
        return when (item) {
            is SummaryObject.Authentifiant ->
                VaultListItemState.ThumbnailState.DomainIcon(item.urlForUI())

            is SummaryObject.Passkey ->
                VaultListItemState.ThumbnailState.DomainIcon(item.urlForGoToWebsite)

            is SummaryObject.PaymentCreditCard ->
                VaultListItemState.ThumbnailState.LegacyOtherIcon(
                    token = IconTokens.itemPaymentOutlined,
                    color = item.color.getColorResource()
                )

            is SummaryObject.SecureNote ->
                VaultListItemState.ThumbnailState.LegacyOtherIcon(
                    token = IconTokens.itemSecureNoteOutlined,
                    color = item.type.getColorId()
                )

            is SummaryObject.Address,
            is SummaryObject.BankStatement,
            is SummaryObject.Company,
            is SummaryObject.DriverLicence,
            is SummaryObject.Email,
            is SummaryObject.FiscalStatement,
            is SummaryObject.IdCard,
            is SummaryObject.Identity,
            is SummaryObject.Passport,
            is SummaryObject.PaymentPaypal,
            is SummaryObject.PersonalWebsite,
            is SummaryObject.Phone,
            is SummaryObject.SocialSecurityStatement,
            is SummaryObject.Secret -> {
                item.syncObjectType.getThumbnailIconToken()?.let { icon ->
                    VaultListItemState.ThumbnailState.OtherIcon(token = icon)
                }
            }
            else -> null
        }
    }

    private fun SyncObjectType.getThumbnailIconToken(): IconToken? = when {
        this == SyncObjectType.PAYMENT_CREDIT_CARD -> IconTokens.itemPaymentOutlined
        this == SyncObjectType.SECURE_NOTE -> IconTokens.itemSecureNoteOutlined
        this == SyncObjectType.ADDRESS -> IconTokens.homeOutlined
        this == SyncObjectType.BANK_STATEMENT -> IconTokens.itemBankAccountOutlined
        this == SyncObjectType.COMPANY -> IconTokens.itemCompanyOutlined
        this == SyncObjectType.DRIVER_LICENCE -> IconTokens.itemDriversLicenseOutlined
        this == SyncObjectType.EMAIL -> IconTokens.itemEmailOutlined
        this == SyncObjectType.FISCAL_STATEMENT -> IconTokens.itemTaxNumberOutlined
        this == SyncObjectType.ID_CARD -> IconTokens.itemIdOutlined
        this == SyncObjectType.IDENTITY -> IconTokens.itemIdOutlined
        this == SyncObjectType.PASSPORT -> IconTokens.itemPassportOutlined
        this == SyncObjectType.PAYMENT_PAYPAL -> IconTokens.itemBankAccountOutlined
        this == SyncObjectType.PERSONAL_WEBSITE -> IconTokens.webOutlined
        this == SyncObjectType.PHONE -> IconTokens.itemPhoneMobileOutlined
        this == SyncObjectType.SOCIAL_SECURITY_STATEMENT -> IconTokens.itemSocialSecurityOutlined
        this == SyncObjectType.SECRET -> IconTokens.itemSecretOutlined
        else -> null
    }
}