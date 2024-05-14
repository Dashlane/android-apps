package com.dashlane.login.pages.token

import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.dashlane.R
import com.dashlane.login.CodeInputViewHelper
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.getThemeAttrColor

class LoginTokenViewProxy(view: View) :
    LoginBaseSubViewProxy<LoginTokenContract.Presenter>(view),
    LoginTokenContract.ViewProxy {

    private val tokenView = findViewByIdEfficient<EditText>(R.id.view_login_token)!!
    private val btnWhereIs = findViewByIdEfficient<AppCompatButton>(R.id.btn_where_is)!!
    private val tokenExpirationHint = context.getString(R.string.receive_sec_code_expires)

    override var tokenText: String
        get() = tokenView.text.toString()
        set(value) {
            tokenView.setText(value)
        }

    override var tokenViewWidth: Int
        get() = tokenView.width
        set(value) {
            CodeInputViewHelper.initialize(tokenView, value)
        }

    init {

        
        
        btnWhereIs.maxLines = 2

        btnWhereIs.setOnClickListener { presenter.onWhereIsClicked() }

        tokenView.addTextChangedListener {
            afterTextChanged {
                if (!errorTextView?.text.isNullOrEmpty()) {
                    showError(null)
                    errorTextView?.apply {
                        animate().cancel()
                        restoreExpirationHint()
                    }
                }
                if (tokenView.text.length == LoginTokenDataProvider.VALID_TOKEN_LENGTH) presenter.onCodeCompleted()
            }
        }
        tokenView.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                presenter.onNextClicked()
                true
            } else {
                false
            }
        }

        errorTextView?.restoreExpirationHint()
    }

    override fun requestFocus() {
        tokenView.requestFocus()
    }

    private fun TextView.restoreExpirationHint() {
        setTextColor(context.getThemeAttrColor(R.attr.colorOnBackground))
        text = tokenExpirationHint
        visibility = View.VISIBLE
    }

    override fun init(savedInstanceState: Bundle?) {
        val width = savedInstanceState?.getInt(STATE_TOKEN_VIEW_WIDTH) ?: 0
        tokenViewWidth = width
    }

    override fun onSaveInstanceState(outState: Bundle) {
        val width = tokenViewWidth
        outState.putInt(STATE_TOKEN_VIEW_WIDTH, width)
    }

    companion object {
        const val STATE_TOKEN_VIEW_WIDTH = "login_token_width"
    }
}
