package com.dashlane.ui.activities.onboarding

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.navigation.Navigator
import com.dashlane.notification.creator.AutoFillNotificationCreator
import com.dashlane.preference.GlobalPreferencesManager
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.ui.activities.intro.IntroScreenViewProxy
import com.dashlane.util.setCurrentPageView
import com.skocken.presentation.presenter.BasePresenter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class InAppLoginIntroActivity : DashlaneActivity() {

    private lateinit var presenter: Presenter

    @Inject
    lateinit var globalPreferencesManager: GlobalPreferencesManager

    @Inject
    lateinit var autoFillNotificationCreator: AutoFillNotificationCreator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_ASK_AUTOFILL_ACTIVATION)
        setContentView(R.layout.activity_intro)
        presenter = Presenter(
            navigator,
            globalPreferencesManager,
            autoFillNotificationCreator
        )
        presenter.setView(IntroScreenViewProxy(this))
    }

    @Deprecated("Deprecated in Java")
    @SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        
    }

    private class Presenter(
        private val navigator: Navigator,
        private val globalPreferencesManager: GlobalPreferencesManager,
        private val autoFillNotificationCreator: AutoFillNotificationCreator
    ) : BasePresenter<IntroScreenContract.DataProvider,
        IntroScreenContract.ViewProxy>(),
        IntroScreenContract.Presenter {

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
            autoFillNotificationCreator.cancelAutofillNotificationWorkers()
            globalPreferencesManager.saveActivatedAutofillOnce()
            activity?.setResult(Activity.RESULT_OK)

            navigator.goToInAppLogin()
            activity?.finish()
        }

        override fun onClickNegativeButton() {
            activity?.setResult(Activity.RESULT_CANCELED)
            activity?.finish()
        }

        override fun onClickNeutralButton() {
            
        }

        override fun onClickLink(position: Int, label: Int) {
            
        }
    }
}