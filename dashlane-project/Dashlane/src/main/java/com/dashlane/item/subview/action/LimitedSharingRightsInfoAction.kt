package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.ui.action.Action
import com.dashlane.ui.util.DialogHelper

class LimitedSharingRightsInfoAction : Action {

    override val icon: Int = R.drawable.ic_info_24

    override val tintColorRes: Int? = null

    override val text: Int = R.string.and_accessibility_info

    override fun onClickAction(activity: AppCompatActivity) {
        DialogHelper().builder(activity)
            .setTitle(R.string.reveal_password_permission_title)
            .setMessage(R.string.reveal_password_permission_body)
            .setPositiveButton(R.string.ok) { _, _ -> }
            .setCancelable(true)
            .show()
    }
}