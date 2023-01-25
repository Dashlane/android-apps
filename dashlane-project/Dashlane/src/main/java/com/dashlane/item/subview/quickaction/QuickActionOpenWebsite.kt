package com.dashlane.item.subview.quickaction

import com.dashlane.R
import com.dashlane.item.subview.action.LoginAction
import com.dashlane.vault.summary.SummaryObject

class QuickActionOpenWebsite(url: String, linkedServices: SummaryObject.LinkedServices?) :
    LoginAction(url, linkedServices) {

    override val icon: Int = R.drawable.ic_action_open

    override val text: Int = R.string.quick_action_open_website

    override val tintColorRes = R.color.text_neutral_catchy
}