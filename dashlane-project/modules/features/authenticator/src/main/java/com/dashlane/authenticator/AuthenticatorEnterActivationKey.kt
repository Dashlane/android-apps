package com.dashlane.authenticator

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.dashlane.authenticator.AuthenticatorIntro.Companion.EXTRA_CREDENTIAL_NAME
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.isNotSemanticallyNull



class AuthenticatorEnterActivationKey : DashlaneActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_enter_activation_key)
        val name = intent.extras?.getString(EXTRA_CREDENTIAL_NAME)
        if (name == null) {
            finish()
        }
        val input = findViewById<EditText>(R.id.authenticator_enter_activation_key_input)
        val button =
            findViewById<Button>(R.id.authenticator_enter_activation_key_positive_button).apply {
                setOnClickListener {
                    val secret = input.text.toString()
                    setResult(RESULT_OK, Intent().apply {
                        putExtra(RESULT_OTP_SECRET, secret)
                    })
                    finish()
                }
            }
        input.addTextChangedListener {
            afterTextChanged {
                button.isEnabled = input.text.toString().isNotSemanticallyNull()
            }
        }
        val title = getString(R.string.authenticator_enter_activation_key_title, name)
        findViewById<TextView>(R.id.authenticator_enter_activation_key_title).text = title
    }

    companion object {
        const val RESULT_OTP_SECRET = "result_otp_secret"
    }
}