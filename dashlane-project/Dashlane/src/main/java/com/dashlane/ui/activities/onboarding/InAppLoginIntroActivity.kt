package com.dashlane.ui.activities.onboarding

import android.app.Activity
import android.os.Bundle
import androidx.navigation.navArgs
import com.dashlane.R
import com.dashlane.navigation.Navigator
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.ui.activities.onboarding.logger.InAppLoginIntroLogger
import com.dashlane.ui.activities.onboarding.logger.InAppLoginIntroLoggerImpl
import com.dashlane.useractivity.log.inject.UserActivityComponent
import com.dashlane.useractivity.log.usage.UsageLogCode131
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject



@AndroidEntryPoint
class InAppLoginIntroActivity : DashlaneActivity() {

    private lateinit var presenter: Presenter

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_intro)
        presenter = Presenter(
            navArgs<InAppLoginIntroActivityArgs>().value.origin,
            navigator,
            globalPreferencesManager
        )
        val logger = InAppLoginIntroLoggerImpl(UserActivityComponent(this).currentSessionUsageLogRepository)
        presenter.logger = logger
        presenter.setView(IntroScreenViewProxy(this))
        logger.takeIf { savedInstanceState == null }?.logShowInAppLoginScreen()
    }

    override fun onBackPressed() {
        
    }

    private class Presenter(
        private val origin: String?,
        private val navigator: Navigator,
        private val globalPreferencesManager: GlobalPreferencesManager
    ) : BasePresenter<IntroScreenContract.DataProvider,
        IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

        lateinit var logger: InAppLoginIntroLogger

        override fun onViewChanged() {
            super.onViewChanged()
            view.apply {
                setImageResource(R.drawable.autologin_intro_icon)
                setTitle(R.string.most_used_password_autologin_intro_title)
                setDescription(R.string.most_used_password_autologin_intro_text)
                setNegativeButton(R.string.most_used_password_autologin_intro_negative_button)
                setPositiveButton(R.string.most_used_password_autologin_positive_button)
            }
        }

        override fun onClickPositiveButton() {
            activity?.applicationContext?.let {
                AutoFillNotificationCreator.cancelAutofillNotificationWorkers(it)
            }
            globalPreferencesManager.saveActivatedAutofillOnce()
            logger.logActivateAutofill()
            activity?.setResult(Activity.RESULT_OK)

            navigator.goToInAppLogin(origin)
            activity?.finish()
        }

        override fun onClickNegativeButton() {
            logger.logSkip(UsageLogCode131.Type.AUTOFILL_ACTIVATION_PROMPT)
            activity?.setResult(Activity.RESULT_CANCELED)
            activity?.finish()
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }
}