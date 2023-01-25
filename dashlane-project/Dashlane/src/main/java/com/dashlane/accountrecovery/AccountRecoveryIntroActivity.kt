package com.dashlane.accountrecovery

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

class AccountRecoveryIntroActivity : DashlaneActivity() {

    private val accountRecovery: AccountRecovery
        get() = SingletonProvider.getComponent().accountRecovery

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        accountRecovery.isFeatureKnown = true

        setContentView(R.layout.activity_intro)

        Presenter().setView(IntroScreenViewProxy(this))

        if (savedInstanceState == null) {
            accountRecovery.logger.logAccountRecoveryIntroDisplay()
        }
    }

    private class Presenter : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {
        override fun onViewChanged() {
            super.onViewChanged()
            view.run {
                setImageResource(R.drawable.ic_account_recovery_intro)
                setTitle(R.string.account_recovery_intro_title)
                setDescription(R.string.account_recovery_intro_description)
                setPositiveButton(R.string.account_recovery_intro_positive_cta)
                setNegativeButton(R.string.account_recovery_intro_negative_cta)
            }
        }

        override fun onClickPositiveButton() {
            val activity = (activity as? AccountRecoveryIntroActivity) ?: return
            activity.accountRecovery.setFeatureEnabled(true, UsageLogConstant.ViewType.accountRecoveryIntro)
            activity.finish()
        }

        override fun onClickNegativeButton() {
            activity?.finish()
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) = Unit
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, AccountRecoveryIntroActivity::class.java)
    }
}