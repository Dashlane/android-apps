package com.dashlane.login.lock

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.isVisible
import com.dashlane.R
import com.dashlane.biometricrecovery.BiometricRecovery
import com.dashlane.biometricrecovery.MasterPasswordResetIntroActivity
import com.dashlane.help.HelpCenterLink
import com.dashlane.login.LoginActivity
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.security.SecurityHelper
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.screens.activities.onboarding.hardwareauth.HardwareAuthActivationActivity
import com.dashlane.util.clearTop
import com.dashlane.util.dpToPx
import com.dashlane.util.getParcelableExtraCompat
import com.dashlane.util.hardwaresecurity.BiometricAuthModule
import com.dashlane.util.launchUrl
import com.google.android.material.button.MaterialButton
import com.skocken.presentation.definition.Base
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingApplicationLockActivity : DashlaneActivity() {

    @Inject
    lateinit var biometricAuthModule: BiometricAuthModule

    @Inject
    lateinit var biometricRecovery: BiometricRecovery

    @Inject
    lateinit var lockTypeManager: LockTypeManager

    @Inject
    lateinit var securityHelper: SecurityHelper

    @Inject
    lateinit var userPreferencesManager: UserPreferencesManager

    private lateinit var presenter: Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        val nextIntent = intent?.getParcelableExtraCompat<Intent>(EXTRA_NEXT_INTENT)
        val fromUse2fa = intent?.getBooleanExtra(EXTRA_FROM_USE_2FA, false) ?: false

        if (nextIntent == null) {
            finish()
            return
        }

        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_intro)

        
        userPreferencesManager.putLong(
            ConstantsPrefs.LOCK_POPUP_LATEST_TIMESTAMP,
            System.currentTimeMillis()
        )

        presenter = Presenter(
            lockTypeManager,
            biometricAuthModule,
            securityHelper,
            biometricRecovery,
            nextIntent,
            fromUse2fa
        )
        presenter.setView(ViewProxy(this))
    }

    override fun onResume() {
        super.onResume()
        presenter.onResume()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        presenter.onActivityResult(requestCode, resultCode, data)
    }

    private class ViewProxy(private val delegate: IntroScreenViewProxy) :
        IntroScreenContract.ViewProxy by delegate {
        private var presenter: Presenter? = null

        private val skipButton = MaterialButton(context, null, R.attr.borderlessButtonStyle).apply {
            id = View.generateViewId()
            setText(R.string.onboarding_application_lock_cta_skip_default)
            setOnClickListener { presenter?.onClickSkip() }
        }

        constructor(activity: Activity) : this(IntroScreenViewProxy(activity))

        init {
            val root = delegate.findViewByIdEfficient<ConstraintLayout>(R.id.view_root)!!
                .apply { addView(skipButton) }

            
            ConstraintSet().run {
                val margin = context.dpToPx(8f).toInt()
                clone(root)
                connect(skipButton.id, ConstraintSet.END, root.id, ConstraintSet.END, margin)
                connect(skipButton.id, ConstraintSet.TOP, root.id, ConstraintSet.TOP, margin)
                applyTo(root)
            }
        }

        override fun setPresenter(presenter: Base.IPresenter?) {
            this.presenter = presenter as? Presenter?
            delegate.setPresenter(presenter)
        }

        fun showSkip(show: Boolean) {
            skipButton.isVisible = show
        }
    }

    private class Presenter(
        private val lockTypeManager: LockTypeManager,
        private val biometricAuthModule: BiometricAuthModule,
        private val securityHelper: SecurityHelper,
        private val biometricRecovery: BiometricRecovery,
        private val nextIntent: Intent,
        private val fromUse2fa: Boolean
    ) : BasePresenter<IntroScreenContract.DataProvider, ViewProxy>(),
        IntroScreenContract.Presenter {
        private var uiConfig = getUiConfig()

        fun onResume() {
            refreshUi()
        }

        fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            if (resultCode != Activity.RESULT_OK) return

            val enabled = when (requestCode) {
                REQUEST_CODE_SET_PIN -> true
                REQUEST_CODE_ACTIVATE_BIOMETRIC -> data?.getBooleanExtra(
                    HardwareAuthActivationActivity.EXTRA_IS_SUCCESSFUL,
                    false
                ) == true
                else -> false
            }

            if (enabled) done(biometricEnabled = requestCode == REQUEST_CODE_ACTIVATE_BIOMETRIC)
        }

        override fun onViewChanged() {
            super.onViewChanged()
            refreshUi()
        }

        fun onClickSkip() {
            done()
        }

        override fun onClickPositiveButton() {
            if (uiConfig.hasBiometrics) {
                showBiometricActivation()
            } else {
                showPinCodeSetter()
            }
        }

        override fun onClickNegativeButton() {
            if (uiConfig.hasBiometrics) {
                showPinCodeSetter()
            }
        }

        override fun onClickNeutralButton() = Unit

        override fun onClickLink(position: Int, label: Int) {
            uiConfig.link?.uri?.let {
                context?.launchUrl(it)
            }
        }

        private fun showBiometricActivation() {
            activity?.run {
                startActivityForResult(
                    HardwareAuthActivationActivity.newIntent(this).clearTop(),
                    REQUEST_CODE_ACTIVATE_BIOMETRIC
                )
            }
        }

        private fun showPinCodeSetter() {
            activity?.run {
                startActivityForResult(
                    Intent(context, LoginActivity::class.java)
                        .putExtra(LockSetting.EXTRA_IS_LOCK_CANCELABLE, true)
                        .putExtra(LockSetting.EXTRA_LOCK_TYPE_IS_PIN_SET, true)
                        .clearTop(),
                    REQUEST_CODE_SET_PIN
                )
            }
        }

        private fun refreshUi() {
            
            if (lockTypeManager.getLockType() != LockTypeManager.LOCK_TYPE_MASTER_PASSWORD) {
                done()
                return
            }

            if (uiConfig.canSkip && !securityHelper.allowedToUsePin()) {
                
                done()
                return
            } else if (!securityHelper.allowedToUsePin()) {
                securityHelper.showPopupPinCodeDisable(activity)
            }

            uiConfig = getUiConfig()

            viewOrNull?.run {
                setImageResource(uiConfig.imageRes)
                setTitle(uiConfig.titleRes)
                setDescription(uiConfig.descriptionRes)
                setPositiveButton(uiConfig.positiveButtonRes)
                setNegativeButton(uiConfig.negativeButtonRes)
                showSkip(uiConfig.canSkip)
                uiConfig.linkRes?.let { setLinks(it) }
            }
        }

        private fun done(biometricEnabled: Boolean = false) {
            activity?.run {
                startActivities(
                    buildList {
                        add(nextIntent.clearTop())

                        if (biometricEnabled && biometricRecovery.isFeatureAvailable()) {
                            add(MasterPasswordResetIntroActivity.newIntent(this@run))
                        }
                    }.toTypedArray()
                )
                finish()
            }
        }

        private fun getUiConfig() = UiConfig(
            hasBiometrics = biometricAuthModule.isHardwareSetUp(),
            fromUse2fa = fromUse2fa
        )

        private data class UiConfig(
            val hasBiometrics: Boolean,
            val fromUse2fa: Boolean
        ) {
            @get:DrawableRes
            val imageRes
                get() = when {
                    fromUse2fa -> R.drawable.picto_authenticator
                    else -> R.drawable.ic_onboarding_application_lock
                }

            @get:StringRes
            val titleRes
                get() = when {
                    fromUse2fa -> R.string.onboarding_application_lock_title_use_2fa
                    !hasBiometrics -> R.string.onboarding_application_lock_title_pin
                    else -> R.string.onboarding_application_lock_title_default
                }

            @get:StringRes
            val descriptionRes
                get() = when {
                    fromUse2fa -> R.string.onboarding_application_lock_description_use_2fa
                    !hasBiometrics -> R.string.onboarding_application_lock_description_pin
                    else -> R.string.onboarding_application_lock_description_default
                }

            @get:StringRes
            val positiveButtonRes
                get() = if (hasBiometrics) {
                    R.string.onboarding_application_lock_cta_positive_default
                } else {
                    R.string.onboarding_application_lock_cta_positive_pin
                }

            @get:StringRes
            val negativeButtonRes
                get() = if (hasBiometrics) {
                    R.string.onboarding_application_lock_cta_negative_default
                } else {
                    0
                }

            @get:StringRes
            val linkRes
                get() = if (fromUse2fa) {
                    R.string.onboarding_application_lock_link_use_2fa
                } else {
                    null
                }

            val link
                get() = if (fromUse2fa) {
                    HelpCenterLink.ARTICLE_AUTHENTICATOR_APP
                } else {
                    null
                }

            val canSkip get() = !fromUse2fa
        }

        companion object {
            private const val REQUEST_CODE_SET_PIN = 8_479
            private const val REQUEST_CODE_ACTIVATE_BIOMETRIC = 43_567
        }
    }

    companion object {
        private const val EXTRA_NEXT_INTENT = "next_intent"
        private const val EXTRA_FROM_USE_2FA = "from_use_2fa"

        fun newIntent(context: Context, nextIntent: Intent, fromUse2fa: Boolean = false): Intent =
            Intent(context, OnboardingApplicationLockActivity::class.java)
                .putExtra(EXTRA_NEXT_INTENT, nextIntent)
                .putExtra(EXTRA_FROM_USE_2FA, fromUse2fa)
    }
}