package com.dashlane.security.darkwebmonitoring.detail

import android.app.Activity
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isVisible
import androidx.lifecycle.LifecycleCoroutineScope
import com.dashlane.R
import com.dashlane.item.subview.view.InfoboxViewProvider
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.dashlane.util.SnackbarUtils
import com.dashlane.util.addOnFieldVisibilityToggleListener
import com.dashlane.util.dpToPx
import com.dashlane.util.getBaseActivity
import com.google.android.material.textfield.TextInputLayout
import com.skocken.presentation.viewproxy.BaseViewProxy

class BreachAlertDetailViewProxy(rootView: View, activityLifecycleScope: LifecycleCoroutineScope) :
    BaseViewProxy<BreachAlertDetail.Presenter>(rootView), BreachAlertDetail.ViewProxy {
    private val domainTextView = findViewByIdEfficient<TextView>(R.id.domain)!!
    private val subtitleTextView = findViewByIdEfficient<TextView>(R.id.subtitle)!!
    private val logoDomain = findViewByIdEfficient<ThumbnailDomainIconView>(R.id.logo_domain)!!
    private val websiteLabelView = findViewByIdEfficient<TextView>(R.id.breach_website_label)!!
    private val websiteView = findViewByIdEfficient<TextView>(R.id.breach_website)!!
    private val emailLabelView = findViewByIdEfficient<View>(R.id.breach_mail_label)!!
    private val emailView = findViewByIdEfficient<TextView>(R.id.breach_mail)!!
    private val dateView = findViewByIdEfficient<TextView>(R.id.breach_date)!!
    private val passwordInputLayout =
        findViewByIdEfficient<TextInputLayout>(R.id.breach_password_input_layout)!!
    private val passwordEditText = findViewByIdEfficient<EditText>(R.id.breach_password_textview)!!
    private val solvedGroup = findViewByIdEfficient<Group>(R.id.breach_solved_group)!!

    private val otherDataLabel = findViewByIdEfficient<TextView>(R.id.breach_other_label)!!
    private val otherData = findViewByIdEfficient<TextView>(R.id.breach_other)!!

    private val adviceLayout = findViewByIdEfficient<LinearLayout>(R.id.breach_infobox_layout)!!
    private val adviceLabel = findViewByIdEfficient<View>(R.id.breach_advice_label)!!

    private val deleteCta = findViewByIdEfficient<Button>(R.id.delete_cta)!!

    private val toolbar
        get() = (context.getBaseActivity() as AppCompatActivity).supportActionBar!!

    init {
        deleteCta.setOnClickListener { presenter.deleteBreach(activityLifecycleScope) }
    }

    override fun updateTitle(darkWebBreach: Boolean, isSolved: Boolean) {
        toolbar.title = if (darkWebBreach) {
            context.getString(R.string.dwm_dark_web_alert_detail_title)
        } else {
            context.getString(R.string.dwm_alert_detail_title)
        }
        if (isSolved) {
            subtitleTextView.isVisible = false
            solvedGroup.isVisible = true
        } else {
            solvedGroup.isVisible = false
            subtitleTextView.isVisible = true
            subtitleTextView.text = if (darkWebBreach) {
                context.getString(R.string.dwm_dark_web_alert_detail_subtitle)
            } else {
                context.getString(R.string.dwm_alert_detail_subtitle)
            }
        }
    }

    override fun setDomain(domain: String?) {
        if (domain != null) {
            domainTextView.visibility = View.VISIBLE
            domainTextView.text = domain
            logoDomain.domainUrl = domain
            logoDomain.contentDescription = context.getString(R.string.and_accessibility_domain_item_logo, domain)
        } else {
            domainTextView.visibility = View.GONE
        }
    }

    override fun showWebsite(website: String) {
        websiteView.visibility = View.VISIBLE
        websiteLabelView.visibility = View.VISIBLE
        websiteView.text = website
    }

    override fun hideWebsite() {
        websiteView.visibility = View.GONE
        websiteLabelView.visibility = View.GONE
    }

    override fun setEmails(emails: List<String>?) {
        if (emails.isNullOrEmpty()) {
            emailLabelView.visibility = View.GONE
            emailView.visibility = View.GONE
        } else {
            emailLabelView.visibility = View.VISIBLE
            emailView.visibility = View.VISIBLE
            emailView.text = emails.joinToString()
        }
    }

    override fun setDate(date: String) {
        dateView.text = date
    }

    override fun setPassword(password: String) {
        if (password.isNotEmpty()) {
            passwordInputLayout.visibility = View.VISIBLE
            
            passwordInputLayout.addOnFieldVisibilityToggleListener { visible ->
                if (visible) {
                    passwordEditText.setText(password)
                } else {
                    
                    passwordEditText.setText(
                        context.getString(R.string.breach_alert_popup_dark_web_password_hidden_placeholder)
                    )
                }
            }
        } else {
            passwordInputLayout.visibility = View.GONE
        }
    }

    override fun setDataInvolved(dataInvolved: String) {
        if (dataInvolved.isNotEmpty()) {
            otherDataLabel.visibility = View.VISIBLE
            otherData.visibility = View.VISIBLE
            otherData.text = dataInvolved
        } else {
            otherDataLabel.visibility = View.GONE
            otherData.visibility = View.GONE
        }
    }

    override fun showAdvicesInfoBox(advices: Set<BreachAlertAdvice>) {
        adviceLayout.removeAllViews()
        if (advices.isEmpty()) {
            adviceLayout.visibility = View.GONE
            adviceLabel.visibility = View.GONE
            return
        }

        adviceLayout.visibility = View.VISIBLE
        adviceLabel.visibility = View.VISIBLE

        var shouldEnable = true
        advices.forEachIndexed { index, breachAlertAdvice ->
            val infoBox = InfoboxViewProvider.create(
                context,
                breachAlertAdvice.content,
                shouldEnable,
                if (!breachAlertAdvice.resolved) breachAlertAdvice.buttonText else null,
                if (breachAlertAdvice.resolved && advices.size > 1) R.drawable.ic_modal_done else null,
                if (!breachAlertAdvice.resolved && advices.size > 1) String.format("%02d", index + 1) else null,
                breachAlertAdvice.buttonAction
            )
            if (!breachAlertAdvice.resolved) {
                shouldEnable = false
            }
            infoBox.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = context.dpToPx(8F).toInt()
            }
            adviceLayout.addView(infoBox)
        }
    }

    override fun showUndoDeletion(activity: Activity, onRestore: () -> Unit) {
        SnackbarUtils.showSnackbar(
            activity,
            activity.getString(R.string.dwm_breach_delete_undo_desc)
        ) {
            setAction(activity.getString(R.string.dwm_breach_delete_undo_cta)) {
                onRestore()
            }
        }
    }
}
