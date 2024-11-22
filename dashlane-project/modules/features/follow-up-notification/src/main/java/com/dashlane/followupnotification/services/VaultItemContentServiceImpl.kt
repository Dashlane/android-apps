package com.dashlane.followupnotification.services

import com.dashlane.followupnotification.domain.FollowUpNotification.FieldContent
import com.dashlane.followupnotification.domain.FollowUpNotification.FieldContent.ClearContent
import com.dashlane.followupnotification.domain.FollowUpNotification.FieldContent.ObfuscatedContent
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemFieldContentService
import com.dashlane.util.otpToDisplay
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class VaultItemContentServiceImpl @Inject constructor(
    private val vaultItemFieldContentService: VaultItemFieldContentService
) : VaultItemContentService {

    override fun getContent(summaryObject: SummaryObject, copyField: CopyField): FieldContent? {
        return when (copyField) {
            CopyField.Password -> hiddenContentString(8, summaryObject, copyField)?.asObfuscatedContent()
            CopyField.PaymentsNumber -> (summaryObject as? SummaryObject.PaymentCreditCard)?.cardNumberObfuscate?.asObfuscatedContent()
            CopyField.PaymentsSecurityCode -> hiddenContentString(3, summaryObject, copyField)?.asObfuscatedContent()
            CopyField.BankAccountBicSwift,
            CopyField.BankAccountRoutingNumber,
            CopyField.BankAccountSortCode -> hiddenContentString(3, summaryObject, copyField)?.asObfuscatedContent()
            CopyField.BankAccountIban,
            CopyField.BankAccountAccountNumber,
            CopyField.BankAccountClabe -> hiddenContentString(25, summaryObject, copyField)?.asObfuscatedContent()
            CopyField.OtpCode -> vaultItemFieldContentService.getContent(summaryObject, copyField)?.otpToDisplay()
                ?.asClearContent()
            else -> vaultItemFieldContentService.getContent(summaryObject, copyField)?.asClearContent()
        }
    }

    private fun hiddenContentString(stringLength: Int, item: SummaryObject, copyField: CopyField): String? {
        return "*".repeat(stringLength).takeIf {
            vaultItemFieldContentService.hasContent(item, copyField)
        }
    }

    fun String.asObfuscatedContent(): ObfuscatedContent =
        ObfuscatedContent(this)

    fun String.asClearContent(): ClearContent =
        ClearContent(this)
}
