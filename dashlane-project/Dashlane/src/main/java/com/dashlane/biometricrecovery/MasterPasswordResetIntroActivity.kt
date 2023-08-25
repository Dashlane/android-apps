package com.dashlane.biometricrecovery

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.useractivity.log.usage.UsageLogConstant
import com.skocken.presentation.presenter.BasePresenter

class MasterPasswordResetIntroActivity : DashlaneActivity() {

    private val biometricRecovery: BiometricRecovery
        get() = SingletonProvider.getComponent().biometricRecovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        biometricRecovery.isFeatureKnown = true

        setContentView(R.layout.activity_intro)

        Presenter().setView(IntroScreenViewProxy(this))

        if (savedInstanceState == null) {
            biometricRecovery.logger.logAccountRecoveryIntroDisplay()
        }
    }

    private class Presenter :
        BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {
        override fun onViewChanged() {
            super.onViewChanged()
            view.run {
                setImageResource(R.drawable.ic_biometric_recovery_intro)
                setTitle(R.string.account_recovery_intro_title)
                setDescription(R.string.account_recovery_intro_description)
                setPositiveButton(R.string.account_recovery_intro_positive_cta)
                setNegativeButton(R.string.account_recovery_intro_negative_cta)
            }
        }

        override fun onClickPositiveButton() {
            val activity = (activity as? MasterPasswordResetIntroActivity) ?: return
            activity.biometricRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.accountRecoveryIntro)
            activity.finish()
        }

        override fun onClickNegativeButton() {
            activity?.finish()
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) = Unit
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MasterPasswordResetIntroActivity::class.java)
    }
}