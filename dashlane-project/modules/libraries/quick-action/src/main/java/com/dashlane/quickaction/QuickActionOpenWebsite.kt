package com.dashlane.quickaction

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.navigation.Navigator
import com.dashlane.ui.action.Action
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.summary.SummaryObject

class QuickActionOpenWebsite(
    private val url: String,
    private val linkedServices: SummaryObject.LinkedServices?,
    private val navigator: Navigator,
) : Action {

    override val icon: Int = R.drawable.ic_action_open_external_link_outlined

    override val text: Int = R.string.quick_action_open_website

    override val tintColorRes = R.color.text_neutral_catchy

    override fun onClickAction(activity: AppCompatActivity) {
        linkedServices.getAllLinkedPackageName().let { packageNames ->
            navigator.openWebsite(
                url = url.toUrlOrNull()?.toString(),
                packageNames = packageNames
            )
        }
    }
}