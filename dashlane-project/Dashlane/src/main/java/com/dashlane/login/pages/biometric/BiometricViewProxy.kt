package com.dashlane.login.pages.biometric

import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.dashlane.R
import com.skocken.presentation.viewproxy.BaseViewProxy

class BiometricViewProxy(view: View) :
    BaseViewProxy<BiometricContract.Presenter>(view), BiometricContract.ViewProxy {

    override var showProgress: Boolean = false
    override var email: String? = null

    private val emailView: TextView
        get() = findViewByIdEfficient(R.id.view_login_email_header)!!

    override fun prepareForTransitionStart() {
        
    }

    override fun prepareForTransitionEnd() {
        
    }

    override fun init(savedInstanceState: Bundle?) {
        
    }

    override fun onSaveInstanceState(outState: Bundle) {
        
    }

    override fun requestFocus() {
        
    }

    override fun showError(errorResId: Int, onClick: () -> Unit) {
        
    }

    override fun showError(error: CharSequence?, onClick: () -> Unit) {
        
    }

    override fun showEmail(email: String) {
        emailView.text = email
    }
}