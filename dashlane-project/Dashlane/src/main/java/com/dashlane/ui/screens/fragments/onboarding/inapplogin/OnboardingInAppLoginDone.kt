package com.dashlane.ui.screens.fragments.onboarding.inapplogin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.dashlane.R
import com.dashlane.notification.creator.AutoFillNotificationCreator.Companion.cancelAutofillNotificationWorkers
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.fragments.BaseUiFragment
import com.dashlane.ui.screens.activities.onboarding.inapplogin.OnboardingType
import com.dashlane.util.getSerializableCompat
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class OnboardingInAppLoginDone : BaseUiFragment() {

    @Inject
    lateinit var globalPreferenceManager: GlobalPreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val frameLayout = FrameLayout(requireContext())
        val v = inflater.inflate(R.layout.activity_intro, frameLayout, true)
        val args = arguments
        val presenter = Presenter(
            activityOrigin = args?.getString(ARGS_ACTIVITY_ORIGIN),
            onboardingType = args?.getSerializableCompat(ARGS_ONBOARDING_TYPE) ?: OnboardingType.AUTO_FILL_API,
            globalPreferenceManager = globalPreferenceManager
        )
        presenter.setView(IntroScreenViewProxy(v))
        return frameLayout
    }

    internal class Presenter(
        private val activityOrigin: String?,
        private val onboardingType: OnboardingType,
        private val globalPreferenceManager: GlobalPreferencesManager
    ) : BasePresenter<IntroScreenContract.DataProvider?, IntroScreenContract.ViewProxy?>(),
        IntroScreenContract.Presenter {
        private val logger = OnboardingInAppLoginLogger.invoke()
        override fun onViewChanged() {
            super.onViewChanged()
            val view = view
            view.setImageResource(R.drawable.ic_modal_done)

            if (onboardingType == OnboardingType.AUTO_FILL_API) {
                view.setTitle(R.string.onboarding_in_app_login_done_title)
                view.setDescription(R.string.onboarding_in_app_login_done_subtitle)
            } else {
                view.setTitle(R.string.onboarding_accessibility_done_title)
                view.setDescription(R.string.onboarding_accessibility_done_subtitle)
            }
            view.setPositiveButton(R.string.onboarding_in_app_login_done_positive_button)
        }

        override fun onClickPositiveButton() {
            val context = context
            
            if (context != null && onboardingType == OnboardingType.AUTO_FILL_API) {
                cancelAutofillNotificationWorkers(context)
                globalPreferenceManager.saveActivatedAutofillOnce()
            }
            logger.logDismissSuccessScreen(activityOrigin, onboardingType.usageLog95Type)
            activity!!.finish()
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickNegativeButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }

    companion object {
        private const val ARGS_ACTIVITY_ORIGIN = "args_activity_origin"
        private const val ARGS_ONBOARDING_TYPE = "args_onboarding_type"

        @JvmStatic
        fun newInstance(
            onboardingType: OnboardingType?,
            activityOrigin: String?
        ): OnboardingInAppLoginDone {
            val fragment = OnboardingInAppLoginDone()
            fragment.arguments = Bundle().apply {
                putSerializable(ARGS_ONBOARDING_TYPE, onboardingType)
                putString(ARGS_ACTIVITY_ORIGIN, activityOrigin)
            }
            return fragment
        }
    }
}