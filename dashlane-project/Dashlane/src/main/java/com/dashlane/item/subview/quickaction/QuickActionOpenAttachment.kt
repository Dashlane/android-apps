package com.dashlane.item.subview.quickaction

import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.action.showAttachments
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.vault.model.getColorId
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.util.attachmentsAllowed
import com.dashlane.vault.util.attachmentsCount
import com.dashlane.xml.domain.SyncObject

class QuickActionOpenAttachment(private val summaryObject: SummaryObject.SecureNote, attachmentsCount: Int) : Action {

    override val text: Int = if (attachmentsCount == 0) {
        R.string.quick_action_add_attachment
    } else {
        R.string.quick_action_open_attachment
    }

    override val icon: Int = R.drawable.ic_attachment

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        val secureNoteType = summaryObject.type ?: SyncObject.SecureNoteType.NO_TYPE
        val secureNoteColor = ContextCompat.getColor(activity, secureNoteType.getColorId())
        summaryObject.showAttachments(activity, secureNoteColor)
    }

    companion object {
        fun createAttachmentsAction(
            summaryObject: SummaryObject,
            userFeaturesChecker: UserFeaturesChecker,
            isAccountFrozen: Boolean = false
        ): QuickActionOpenAttachment? {
            if (summaryObject is SummaryObject.SecureNote && summaryObject.attachmentsAllowed(
                    userFeaturesChecker,
                    isAccountFrozen = isAccountFrozen
                )
            ) {
                return QuickActionOpenAttachment(summaryObject, summaryObject.attachmentsCount())
            }
            return null
        }
    }
}