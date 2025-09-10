package com.dashlane.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.ui.action.Action
import com.dashlane.util.clipboard.vault.CopyField
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.vault.summary.SummaryObject

class QuickActionCopyIdentity(
    private val vaultItemCopyService: VaultItemCopyService,
    private val summaryObject: SummaryObject,
) : Action {
    override val text: Int = R.string.quick_action_copy_name_holder

    override val icon: Int = R.drawable.ic_action_copy_outlined

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        when (summaryObject) {
            is SummaryObject.DriverLicence -> CopyField.DriverLicenseLinkedIdentity
            is SummaryObject.Passport -> CopyField.PassportLinkedIdentity
            is SummaryObject.IdCard -> CopyField.IdsLinkedIdentity
            is SummaryObject.SocialSecurityStatement -> CopyField.SocialSecurityLinkedIdentity
            is SummaryObject.Address,
            is SummaryObject.AuthCategory,
            is SummaryObject.Authentifiant,
            is SummaryObject.BankStatement,
            is SummaryObject.Collection,
            is SummaryObject.Company,
            is SummaryObject.DataChangeHistory,
            is SummaryObject.Email,
            is SummaryObject.FiscalStatement,
            is SummaryObject.GeneratedPassword,
            is SummaryObject.Identity,
            is SummaryObject.Passkey,
            is SummaryObject.PaymentCreditCard,
            is SummaryObject.PersonalWebsite,
            is SummaryObject.Phone,
            is SummaryObject.Secret,
            is SummaryObject.SecureFileInfo,
            is SummaryObject.SecureNote,
            is SummaryObject.SecureNoteCategory,
            is SummaryObject.SecurityBreach,
            is SummaryObject.WiFi,
            -> null
        }?.let {
            vaultItemCopyService.handleCopy(
                item = summaryObject,
                copyField = it,
            )
        }
    }

    companion object {
        fun createActionIfIdentityExist(
            vaultItemCopyService: VaultItemCopyService,
            summaryObject: SummaryObject,
        ): QuickActionCopyIdentity? {
            when (summaryObject) {
                is SummaryObject.IdCard -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity,
                )
                is SummaryObject.DriverLicence -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity,
                )
                is SummaryObject.Passport -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity,
                )
                is SummaryObject.SocialSecurityStatement -> hasIdentity(
                    summaryObject.socialSecurityFullname,
                    summaryObject.linkedIdentity,
                )
                else -> false
            }.let { hasIdentity ->
                if (hasIdentity) {
                    return QuickActionCopyIdentity(vaultItemCopyService, summaryObject)
                }
            }
            return null
        }

        private fun hasIdentity(fullName: String?, linkedIdentity: String?): Boolean =
            fullName.isNotSemanticallyNull() || linkedIdentity.isNotSemanticallyNull()
    }
}