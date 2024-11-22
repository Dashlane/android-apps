package com.dashlane.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.navigation.Navigator
import com.dashlane.securefile.extensions.hasAttachments
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.ui.action.Action
import com.dashlane.vault.summary.SummaryObject

class QuickActionShare(
    private val summaryObject: SummaryObject,
    private val navigator: Navigator,
) : Action {
    override val text: Int = R.string.quick_action_share

    override val icon: Int = R.drawable.ic_share

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        navigator.showNewSharing(
            id = summaryObject.id,
            fromAuthentifiant = summaryObject is SummaryObject.Authentifiant,
            fromSecureNote = summaryObject is SummaryObject.SecureNote,
        )
    }

    companion object {
        fun createActionIfShareAvailable(
            summaryObject: SummaryObject,
            sharingPolicy: SharingPolicyDataProvider,
            navigator: Navigator,
            isAccountFrozen: Boolean = false
        ): QuickActionShare? {
            if (sharingPolicy.canShareItem(summaryObject) && !isAccountFrozen) {
                if (summaryObject is SummaryObject.SecureNote) {
                    if (!summaryObject.hasAttachments()) {
                        return QuickActionShare(summaryObject = summaryObject, navigator = navigator)
                    }
                } else {
                    return QuickActionShare(summaryObject = summaryObject, navigator = navigator)
                }
            }
            return null
        }
    }
}