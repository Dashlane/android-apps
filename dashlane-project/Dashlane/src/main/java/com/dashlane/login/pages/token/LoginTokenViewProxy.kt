package com.dashlane.login.pages.token

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.debug.DeveloperUtilities
import com.dashlane.login.CodeInputViewHelper
import com.dashlane.login.pages.LoginBaseSubViewProxy
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.registerExportedReceiverCompat
import com.google.android.material.textfield.TextInputLayout

class LoginTokenViewProxy(view: View) :
    LoginBaseSubViewProxy<LoginTokenContract.Presenter>(view),
    LoginTokenContract.ViewProxy {

    private val tokenLayout = findViewByIdEfficient<TextInputLayout>(R.id.view_login_token_layout)!!
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

    @SuppressLint("SetTextI18n")
    override fun initDebug(username: String) {
        if (username in PREFILL_TOKEN_EMAIL_LIST) {
            tokenView.post { tokenView.setText("000000") }
        }
        val listener: View.OnAttachStateChangeListener = object : View.OnAttachStateChangeListener {

            private var broadcastReceiver: BroadcastReceiver? = null

            override fun onViewDetachedFromWindow(v: View) {
                broadcastReceiver?.let {
                    try {
                        context.unregisterReceiver(it)
                    } catch (e: Exception) {
                    }
                    broadcastReceiver = null
                }
            }

            override fun onViewAttachedToWindow(v: View) {
                if (!DeveloperUtilities.systemIsInDebug(context.applicationContext)) return

                broadcastReceiver = broadcastReceiver ?: object : BroadcastReceiver() {
                    override fun onReceive(context: Context, intent: Intent) {
                        val securityCode = intent.getStringExtra("securityCode")
                        if (securityCode != null) {
                            tokenView.setText(securityCode)
                        }
                    }
                }.also {
                    context.registerExportedReceiverCompat(it, IntentFilter("com.dashlane.dadada.DEBUG_SECURITY_CODE"))
                }

                SingletonProvider.getDaDaDa().refreshSecurityCode(context, username)
            }
        }
        tokenLayout.addOnAttachStateChangeListener(listener)
        listener.onViewAttachedToWindow(tokenLayout)
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
        private val PREFILL_TOKEN_EMAIL_LIST = listOf(
            "randomemail@provider.com",
            "randomemail@provider.com"
        )
    }
}
