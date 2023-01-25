package com.dashlane.accountrecovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.HardwareAuthActivationActivity
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.skocken.presentation.presenter.BasePresenter

class AccountRecoveryBiometricIntroActivity : DashlaneActivity() {

    private val accountRecovery: AccountRecovery
        get() = SingletonProvider.getComponent().accountRecovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountRecovery.isFeatureKnown = true

        setContentView(R.layout.activity_intro)

        val hardwareAuthActivationActivityResultLauncher = registerForActivityResult(
            HardwareAuthActivationActivity.ResultContract(),
            ::onHardwareAuthActivationActivityResult
        )
        Presenter(hardwareAuthActivationActivityResultLauncher).setView(IntroScreenViewProxy(this))

        if (savedInstanceState == null) {
            accountRecovery.logger.logBiometricIntroDisplay()
        }
    }

    private fun onHardwareAuthActivationActivityResult(success: Boolean) {
        if (success) {
            accountRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.biometricIntro)
            finish()
        }
    }

    private class Presenter(
        private val hardwareAuthActivationActivityResultLauncher: ActivityResultLauncher<Intent?>
    ) : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        override fun onViewChanged() {
            super.onViewChanged()
            view.run {
                setImageResource(R.drawable.ic_biometric)
                setTitle(R.string.account_recovery_biometric_intro_title)
                setDescription(R.string.account_recovery_biometric_intro_description)
                setPositiveButton(R.string.account_recovery_biometric_intro_positive_cta)
                setNegativeButton(R.string.account_recovery_biometric_intro_negative_cta)
            }
        }

        override fun onClickPositiveButton() {
            hardwareAuthActivationActivityResultLauncher.launch(null)
        }

        override fun onClickNegativeButton() {
            activity?.finish()
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) = Unit
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, AccountRecoveryBiometricIntroActivity::class.java)
    }
}