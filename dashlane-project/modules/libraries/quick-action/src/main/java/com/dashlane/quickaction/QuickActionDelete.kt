package com.dashlane.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.navigation.Navigator
import com.dashlane.sharingpolicy.SharingPolicyDataProvider
import com.dashlane.ui.action.Action
import com.dashlane.vault.summary.SummaryObject

class QuickActionDelete(private val summaryObject: SummaryObject, private val navigator: Navigator) : Action {
    override val text: Int = R.string.quick_action_delete

    override val icon: Int = R.drawable.ic_action_delete_outlined

    override val tintColorRes = R.color.text_danger_standard

    override fun onClickAction(activity: AppCompatActivity) {
        navigator.goToDeleteVaultItem(summaryObject.id, summaryObject.isShared)
    }

    companion object {
        fun createActionIfCanDelete(
            summaryObject: SummaryObject,
            sharingPolicy: SharingPolicyDataProvider,
            navigator: Navigator
        ): QuickActionDelete? {
            if (sharingPolicy.isDeleteAllowed(summaryObject.id, false, summaryObject.isShared)) {
                return QuickActionDelete(summaryObject, navigator)
            }
            return null
        }
    }
}