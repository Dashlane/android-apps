package com.dashlane.authenticator.dashboard

import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.dashlane.authenticator.R
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardCredentialItemAdapter.Listener
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins.CredentialItem
import com.dashlane.authenticator.item.AuthenticatorViewProxy
import com.dashlane.ui.widgets.view.ExpandableCardView
import com.dashlane.util.getBaseActivity
import com.dashlane.util.graphics.RemoteImageRoundRectDrawable
import com.dashlane.util.graphics.RoundRectDrawable
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AuthenticatorDashboardCredentialItemViewHolder(v: View) :
    EfficientViewHolder<CredentialItem>(v) {
    private val name = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_name)!!
    private val login = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_login)!!
    private val logo = findViewByIdEfficient<ImageView>(R.id.authenticator_credential_item_icon)!!
    private val code = findViewByIdEfficient<TextView>(R.id.authenticator_credential_item_code)!!
    private val countdown =
        findViewByIdEfficient<ImageView>(R.id.authenticator_credential_item_countdown)!!
    private val copy = findViewByIdEfficient<ImageView>(R.id.authenticator_credential_item_copy)!!
    private val delete =
        findViewByIdEfficient<ImageView>(R.id.authenticator_credential_item_delete)!!
    private val arrow = findViewByIdEfficient<ImageView>(R.id.collapse_arrow)!!
    private val drawable = RemoteImageRoundRectDrawable(v.context, Color.WHITE).also {
        it.setPreferImageBackgroundColor(true)
    }
    lateinit var listener: Listener

    override fun updateView(context: Context, item: CredentialItem?) {
        item ?: return
        name.text = item.title
        login.text = item.username
        logo.setImageDrawable(drawable)
        (view as ExpandableCardView).setExpanded(item.expanded, false)
        val scope = (context.getBaseActivity()!! as AppCompatActivity).lifecycleScope
        AuthenticatorViewProxy(code, countdown, scope, item.otp) {
            
            listener.onOtpCounterUpdate(item.id, it)
        }
        copy.setOnClickListener { item.otp.getPin()?.let { listener.onOtpCopy(it.code, item.id, item.domain) } }
        delete.setOnClickListener { listener.onOtpDelete(item, item.otp.issuer) }
        try {
            drawable.loadImage(item.domain, RoundRectDrawable(context, Color.WHITE))
        } catch (e: IllegalArgumentException) {
            
        }
        setMode(item)
    }

    private fun setMode(item: CredentialItem) {
        val editMode = item.editMode
        copy.isVisible = !editMode
        arrow.isVisible = !editMode
        delete.isVisible = editMode
        code.textSize = if (editMode) 30f else 40f
    }
}