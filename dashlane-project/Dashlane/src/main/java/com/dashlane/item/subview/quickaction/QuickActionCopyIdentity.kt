package com.dashlane.item.subview.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.item.subview.Action
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.isNotSemanticallyNull
import com.dashlane.vault.model.fullName
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

class QuickActionCopyIdentity(private val summaryObject: SummaryObject) : Action {

    override val text: Int = R.string.quick_action_copy_name_holder

    override val icon: Int = R.drawable.ic_item_action_copy

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        val clipboardCopy = SingletonProvider.getClipboardCopy()
        when (summaryObject) {
            is SummaryObject.IdCard -> getIdentityFullName(
                summaryObject.fullname,
                summaryObject.linkedIdentity
            )
            is SummaryObject.DriverLicence -> getIdentityFullName(
                summaryObject.fullname,
                summaryObject.linkedIdentity
            )
            is SummaryObject.Passport -> getIdentityFullName(
                summaryObject.fullname,
                summaryObject.linkedIdentity
            )
            is SummaryObject.SocialSecurityStatement -> getIdentityFullName(
                summaryObject.socialSecurityFullname,
                summaryObject.linkedIdentity
            )
            else -> null
        }?.let {
            clipboardCopy.copyToClipboard(
                it,
                sensitiveData = false,
                autoClear = true,
                feedback = R.string.feedback_copy_name_holder
            )
        }
    }

    private fun getIdentityFullName(fullName: String?, linkedIdentity: String?): String? {
        if (fullName != null) {
            return fullName
        } else if (linkedIdentity != null) {
            SingletonProvider.getMainDataAccessor().getVaultDataQuery().query(
                vaultFilter {
                    specificUid(linkedIdentity)
                    specificDataType(SyncObjectType.IDENTITY)
                }
            )?.syncObject?.let { identitySyncObject ->
                if (identitySyncObject is SyncObject.Identity) {
                    return identitySyncObject.fullName
                }
            }
        }
        return null
    }

    companion object {
        fun createActionIfIdentityExist(summaryObject: SummaryObject): QuickActionCopyIdentity? {
            when (summaryObject) {
                is SummaryObject.IdCard -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity
                )
                is SummaryObject.DriverLicence -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity
                )
                is SummaryObject.Passport -> hasIdentity(
                    summaryObject.fullname,
                    summaryObject.linkedIdentity
                )
                is SummaryObject.SocialSecurityStatement -> hasIdentity(
                    summaryObject.socialSecurityFullname,
                    summaryObject.linkedIdentity
                )
                else -> false
            }.let { hasIdentity ->
                if (hasIdentity) {
                    return QuickActionCopyIdentity(summaryObject)
                }
            }
            return null
        }

        private fun hasIdentity(fullName: String?, linkedIdentity: String?): Boolean =
            fullName.isNotSemanticallyNull() || linkedIdentity.isNotSemanticallyNull()
    }
}