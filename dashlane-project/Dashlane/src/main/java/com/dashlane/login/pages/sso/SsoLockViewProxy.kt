package com.dashlane.login.pages.sso

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import com.dashlane.R
import com.dashlane.login.pages.LoginSwitchAccountUtil
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.showToaster
import com.skocken.presentation.viewproxy.BaseViewProxy

class SsoLockViewProxy(
    view: View
) : BaseViewProxy<SsoLockContract.Presenter>(view),
    SsoLockContract.ViewProxy {
    private val emailTextView get() = findViewByIdEfficient<TextView>(R.id.email)!!
    private val emailSpinner get() = findViewByIdEfficient<Spinner>(R.id.email_spinner)!!
    private val cancelButton get() = findViewByIdEfficient<Button>(R.id.cancel)!!
    private val unlockButton get() = findViewByIdEfficient<Button>(R.id.unlock)!!
    private val progressView get() = findViewByIdEfficient<View>(R.id.progress)!!

    override var email: String?
        get() = emailTextView.text.toString()
        set(value) {
            emailTextView.text = value
        }

    override var showProgress: Boolean
        get() = progressView.visibility == View.VISIBLE
        set(value) {
            unlockButton.setTextColor(if (value) Color.TRANSPARENT else context.getThemeAttrColor(R.attr.colorOnSecondary))
            unlockButton.isEnabled = !value
            progressView.visibility = if (value) View.VISIBLE else View.GONE
        }

    override fun init(savedInstanceState: Bundle?) {
        cancelButton.setOnClickListener {
            presenter.onCancelClicked()
        }

        unlockButton.setOnClickListener {
            presenter.onNextClicked()
        }
    }

    override fun initSpinner(loginHistory: List<String>) {
        LoginSwitchAccountUtil.setupSpinner(emailSpinner, email, loginHistory) {
            presenter.onClickChangeAccount(it)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) = Unit

    override fun prepareForTransitionStart() = Unit

    override fun prepareForTransitionEnd() = Unit

    override fun requestFocus() = Unit

    override fun showError(errorResId: Int, onClick: () -> Unit) {
        showError(context.getString(errorResId), onClick)
    }

    override fun showError(error: CharSequence?, onClick: () -> Unit) {
        context.showToaster(error, Toast.LENGTH_SHORT)
    }

    override fun setCancelable(cancelable: Boolean) {
        cancelButton.visibility = if (cancelable) View.VISIBLE else View.GONE
    }

    override fun canSwitchAccount(switchAccount: Boolean) {
        emailSpinner.visibility = if (switchAccount) View.VISIBLE else View.GONE
    }
}