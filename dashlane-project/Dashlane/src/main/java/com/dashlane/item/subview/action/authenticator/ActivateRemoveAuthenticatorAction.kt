package com.dashlane.item.subview.action.authenticator

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.authenticator.AuthenticatorIntro
import com.dashlane.authenticator.AuthenticatorLogger
import com.dashlane.authenticator.util.showAuthenticatorRemoveConfirmDialog
import com.dashlane.item.ItemEditViewContract.View.UiUpdateListener
import com.dashlane.item.subview.Action
import com.dashlane.item.subview.edit.ItemAuthenticatorEditSubView
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.startActivityForResult
import com.dashlane.vault.model.getAllLinkedPackageName
import java.time.Duration

class ActivateRemoveAuthenticatorAction(
    private val item: ItemAuthenticatorEditSubView,
    private val listener: UiUpdateListener,
    private val authenticatorLogger: AuthenticatorLogger
) : Action {

    override val icon = -1
    override val text: Int
        get() = if (item.value == null) R.string.authenticator_item_edit_activate_action else R.string.authenticator_item_edit_remove_action
    override val tintColorRes: Int? = null

    override fun onClickAction(activity: AppCompatActivity) {
        if (item.value == null) setup(activity) else remove(activity)
    }

    private fun setup(activity: AppCompatActivity) {
        if (activity is DashlaneActivity) {
            activity.lockHelper.startAutoLockGracePeriod(Duration.ofMinutes(2))
        }
        activity.startActivityForResult<AuthenticatorIntro>(REQUEST_CODE) {
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_NAME, item.credentialName)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_ID, item.itemId)
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_TOP_DOMAIN, item.topDomain)
            putExtra(
                AuthenticatorIntro.EXTRA_CREDENTIAL_PACKAGE_NAME,
                item.linkedServices.getAllLinkedPackageName().firstOrNull()
            )
            putExtra(AuthenticatorIntro.EXTRA_CREDENTIAL_PROFESSIONAL, item.professional)
        }
    }

    private fun remove(activity: AppCompatActivity) {
        DialogHelper().showAuthenticatorRemoveConfirmDialog(
            activity,
            item.credentialName,
            item.topDomain,
            item.linkedServices,
            item.professional,
            item.value?.issuer,
            authenticatorLogger
        ) {
            
            item.notifyValueChanged(null)
            listener.notifySubViewChanged(item)
        }
    }

    companion object {
        const val REQUEST_CODE = 2846
    }
}