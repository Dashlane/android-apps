package com.dashlane.item.subview.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.navigation.Navigator
import com.dashlane.ui.screens.fragments.SharingPolicyDataProvider
import com.dashlane.vault.summary.SummaryObject

class QuickActionEdit(private val summaryObject: SummaryObject, private val navigator: Navigator) : Action {

    override val text: Int = R.string.quick_action_edit

    override val icon: Int = R.drawable.edit

    override val tintColorRes: Int = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        navigator.goToItem(summaryObject.id, summaryObject.syncObjectType.xmlObjectName, true)
    }

    companion object {
        fun createActionIfCanEdit(
            summaryObject: SummaryObject,
            sharingPolicy: SharingPolicyDataProvider,
            navigator: Navigator,
            isAccountFrozen: Boolean
        ): QuickActionEdit? {
            return if (sharingPolicy.canEditItem(summaryObject, false) && !isAccountFrozen) {
                return QuickActionEdit(summaryObject, navigator)
            } else {
                null
            }
        }
    }
}