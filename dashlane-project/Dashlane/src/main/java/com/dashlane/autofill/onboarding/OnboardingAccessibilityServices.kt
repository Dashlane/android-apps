package com.dashlane.autofill.onboarding

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import com.dashlane.R
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.login.lock.LockManager
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.fragments.BaseUiFragment
import com.dashlane.util.getSerializableCompat
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingAccessibilityServices : BaseUiFragment() {
    @Inject
    lateinit var preferenceManager: GlobalPreferencesManager

    @Inject
    lateinit var inAppLoginManager: InAppLoginManager

    @Inject
    lateinit var lockManager: LockManager

    private var onboardingType: OnboardingType? = null
    private var positiveButton: Button? = null
    private var negativeButton: Button? = null

    private var agreementCheckbox: CheckBox? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        parseArguments(arguments)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_onboarding_inapp_login, container, true)
        positiveButton = view.findViewById(R.id.onboarding_positive_button)
        negativeButton = view.findViewById(R.id.onboarding_negative_button)
        agreementCheckbox = view.findViewById(R.id.onboarding_agreement_checkbox)

        val checkBoxSavedState = savedInstanceState?.getBoolean(STATE_CHECKBOX) ?: false
        agreementCheckbox?.apply {
            isChecked = checkBoxSavedState
            setOnCheckedChangeListener { _, isChecked ->
                positiveButton?.isEnabled = isChecked
            }
        }
        negativeButton?.setOnClickListener { onNegativeButtonClicked() }
        positiveButton?.apply {
            setOnClickListener { onPositiveButtonClicked() }
            isEnabled = checkBoxSavedState
        }
        return view
    }

    override fun onResume() {
        super.onResume()

        val byAccessibilityManager = inAppLoginManager.inAppLoginByAccessibilityManager
        if (!byAccessibilityManager.isDrawOnTopPermissionEnabled()) {
            preferenceManager.putBoolean(ConstantsPrefs.CALL_PERMISSION, true)
        }
    }

    fun parseArguments(args: Bundle?) {
        if (args != null) {
            onboardingType = args.getSerializableCompat(ARGS_ONBOARDING_TYPE)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_CHECKBOX, agreementCheckbox?.isChecked ?: false)
    }

    private fun onPositiveButtonClicked() {
        lockManager.startAutoLockGracePeriod()
        openAccessibilitySettings()
    }

    private fun onNegativeButtonClicked() {
        if (activity is OnboardingInAppLoginActivity) {
            activity?.finish()
        }
    }

    private fun openAccessibilitySettings() {
        val activity = activity
        if (activity is OnboardingInAppLoginActivity) {
            activity.userWentToAccessibilitySettings = true
        }
        inAppLoginManager.inAppLoginByAccessibilityManager.startActivityToChooseProvider(activity)
        lockManager.startAutoLockGracePeriod()
    }

    companion object {
        private const val ARGS_ONBOARDING_TYPE = "args_onboarding_type"
        private const val STATE_CHECKBOX = "state_checkbox"

        fun newInstance(onboardingType: OnboardingType?): OnboardingAccessibilityServices {
            val fragment = OnboardingAccessibilityServices()
            val args = Bundle()
            args.putSerializable(ARGS_ONBOARDING_TYPE, onboardingType)
            fragment.arguments = args
            return fragment
        }
    }
}