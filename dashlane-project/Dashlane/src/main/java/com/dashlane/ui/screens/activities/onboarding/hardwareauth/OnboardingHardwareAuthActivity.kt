package com.dashlane.ui.screens.activities.onboarding.hardwareauth

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.dashlane.R
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.clearTop
import com.skocken.presentation.presenter.BasePresenter

class OnboardingHardwareAuthActivity : DashlaneActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)

        Presenter().setView(IntroScreenViewProxy(this))
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode != REQUEST_CODE_ACTIVATION && resultCode != Activity.RESULT_OK) return

        val isActivationSuccessful = data?.getBooleanExtra(
            HardwareAuthActivationActivity.EXTRA_IS_SUCCESSFUL,
            false
        ) ?: false

        if (isActivationSuccessful) finish()
    }

    private class Presenter :
        BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {
        override fun onViewChanged() {
            super.onViewChanged()
            view.setImageResource(R.drawable.ic_biometric)
            view.setTitle(R.string.onboarding_biometric_intro_title)
            view.setDescription(R.string.onboarding_biometric_intro_description)
            view.setPositiveButton(R.string.onboarding_biometric_intro_confirm_button)
            view.setNegativeButton(R.string.onboarding_biometric_intro_cancel_button)
        }

        override fun onClickPositiveButton() {
            val activity = activity!!

            val activationIntent = HardwareAuthActivationActivity.newIntent(activity).clearTop()

            activity.startActivityForResult(activationIntent, REQUEST_CODE_ACTIVATION)
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickNegativeButton() {
            activity!!.finish()
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }

    companion object {
        private const val REQUEST_CODE_ACTIVATION = 42
    }
}
