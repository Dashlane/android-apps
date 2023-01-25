package com.dashlane.disabletotp

import android.content.Intent
import android.os.Bundle
import com.dashlane.activatetotp.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.setIntroScreenContent
import com.dashlane.util.clearTop
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DisableTotpEnforcedIntroActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setIntroScreenContent(
            imageResId = R.drawable.picto_authenticator,
            titleResId = R.string.disable_totp_enforced_intro_title,
            descriptionResId = R.string.disable_totp_enforced_intro_description,
            positiveButtonResId = R.string.disable_totp_enforced_intro_cta_positive,
            onClickPositiveButton = {
                startActivity(
                    Intent(
                        this,
                        DisableTotpActivity::class.java
                    ).clearTop()
                )
                finish()
            },
            negativeButtonResId = R.string.disable_totp_enforced_intro_cta_negative,
            onClickNegativeButton = { finish() }
        )
    }
}
