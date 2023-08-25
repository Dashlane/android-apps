package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.subview.Action
import com.dashlane.url.toUrlOrNull
import com.dashlane.vault.model.getAllLinkedPackageName
import com.dashlane.vault.summary.SummaryObject

open class LoginAction(
    private val url: String,
    private val linkedServices: SummaryObject.LinkedServices?,
    private val listener: LoginOpener.Listener? = null
) : Action {

    override val icon: Int = R.drawable.ic_action_open

    override val tintColorRes: Int? = null

    override val text: Int = R.string.login_alt

    override fun onClickAction(activity: AppCompatActivity) {
        linkedServices.getAllLinkedPackageName().let { packageNames ->
            LoginOpener(activity).show(url.toUrlOrNull()?.toString(), packageNames, listener)
        }
    }
}