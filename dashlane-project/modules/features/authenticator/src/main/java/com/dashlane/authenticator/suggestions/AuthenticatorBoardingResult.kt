package com.dashlane.authenticator.suggestions

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.dashlane.authenticator.AuthenticatorBoardingActivity
import com.dashlane.authenticator.AuthenticatorBoardingActivity.Companion.EXTRA_CREDENTIAL_ID
import com.dashlane.authenticator.AuthenticatorBoardingActivity.Companion.EXTRA_CREDENTIAL_NAME
import com.dashlane.authenticator.AuthenticatorBoardingActivity.Companion.EXTRA_CREDENTIAL_PACKAGE_NAME
import com.dashlane.authenticator.AuthenticatorBoardingActivity.Companion.EXTRA_CREDENTIAL_TOP_DOMAIN
import com.dashlane.authenticator.AuthenticatorBoardingActivity.Companion.RESULT_ITEM_ID
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.R
import com.dashlane.authenticator.suggestions.AuthenticatorSuggestionsUiState.HasLogins.CredentialItem
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.vault.model.isSemanticallyNull

class AuthenticatorBoardingResult : ActivityResultContract<CredentialItem, Pair<String?, Otp?>>() {
    override fun createIntent(context: Context, item: CredentialItem): Intent {
        return Intent(context, AuthenticatorBoardingActivity::class.java).apply {
            putExtra(
                EXTRA_CREDENTIAL_NAME,
                if (item.title.isSemanticallyNull()) {
                    context.getString(R.string.authenticator_default_account_name)
                } else {
                    item.title
                },
            )
            putExtra(EXTRA_CREDENTIAL_ID, item.id)
            putExtra(EXTRA_CREDENTIAL_TOP_DOMAIN, item.domain)
            putExtra(EXTRA_CREDENTIAL_PACKAGE_NAME, item.packageName)
            putExtra(AuthenticatorBoardingActivity.EXTRA_CREDENTIAL_PROFESSIONAL, item.professional)
        }
    }

    override fun parseResult(resultCode: Int, intent: Intent?): Pair<String?, Otp?> {
        val itemId = intent?.getStringExtra(RESULT_ITEM_ID)
        if (resultCode != Activity.RESULT_OK) return itemId to null
        return itemId to intent?.getParcelableExtraCompat(AuthenticatorBoardingActivity.RESULT_OTP)
    }
}