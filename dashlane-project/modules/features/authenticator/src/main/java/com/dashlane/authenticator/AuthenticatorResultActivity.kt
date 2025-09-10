package com.dashlane.authenticator

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.dashlane.authenticator.ui.AuthenticatorResultScreen
import com.dashlane.authenticator.util.SetUpAuthenticatorResultContract.SuccessResultContract.Input
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.getParcelableExtraCompat

class AuthenticatorResultActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val credentialName: String? = intent.getStringExtra(EXTRA_CREDENTIAL_NAME)
            ?: intent.getParcelableExtraCompat<Input>(EXTRA_INPUT)?.let { it.itemTitle ?: it.domain }
        val success = intent.extras?.getBoolean(EXTRA_SUCCESS)
        require(success != null) {
            "Required arguments cannot be parsed as expected"
        }

        setContent {
            DashlaneTheme {
                AuthenticatorResultScreen(
                    success = success,
                    credentialName = credentialName,
                    onOkClick = ::onClickPositiveButton,
                    onRetryClick = ::onClickPositiveButton,
                    onCancelClick = ::onClickNegativeButton,
                )
            }
        }
    }

    private fun onClickPositiveButton() {
        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(RESULT_INPUT, intent.getParcelableExtraCompat<Input>(EXTRA_INPUT))
            },
        )
        finish()
    }

    private fun onClickNegativeButton() {
        setResult(RESULT_CANCELED)
        finish()
    }

    companion object {
        private const val EXTRA_SUCCESS = "extra_success"
        private const val EXTRA_INPUT = "extra_input"
        private const val EXTRA_CREDENTIAL_NAME = "extra_input"

        const val RESULT_INPUT = "result_input"

        fun newIntent(
            context: Context,
            success: Boolean,
            input: Input,
        ) = Intent(context, AuthenticatorResultActivity::class.java).apply {
            putExtra(EXTRA_SUCCESS, success)
            putExtra(EXTRA_INPUT, input)
        }

        fun newSuccessIntent(
            context: Context,
            credentialName: String?,
        ) = Intent(context, AuthenticatorResultActivity::class.java).apply {
            putExtra(EXTRA_SUCCESS, true)
            putExtra(EXTRA_CREDENTIAL_NAME, credentialName)
        }

        fun newFailedIntent(
            context: Context,
            credentialName: String?,
        ) = Intent(context, AuthenticatorResultActivity::class.java).apply {
            putExtra(EXTRA_SUCCESS, false)
            putExtra(EXTRA_CREDENTIAL_NAME, credentialName)
        }
    }
}