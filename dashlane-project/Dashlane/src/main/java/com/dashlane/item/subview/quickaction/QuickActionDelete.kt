package com.dashlane.item.subview.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.item.subview.Action
import com.dashlane.vault.summary.SummaryObject

class QuickActionDelete(private val summaryObject: SummaryObject) : Action {
    override val text: Int = R.string.quick_action_delete

    override val icon: Int = R.drawable.action_bar_menu_delete

    override val tintColorRes = R.color.text_danger_standard

    override fun onClickAction(activity: AppCompatActivity) {
        SingletonProvider.getNavigator()
            .goToDeleteVaultItem(summaryObject.id, summaryObject.isShared)
    }

    companion object {
        fun createActionIfCanDelete(summaryObject: SummaryObject): QuickActionDelete? {
            val sharingPolicy = SingletonProvider.getSharingPolicyDataProvider()
            if (sharingPolicy.isDeleteAllowed(summaryObject.id, false, summaryObject.isShared)) {
                return QuickActionDelete(summaryObject)
            }
            return null
        }
    }
}