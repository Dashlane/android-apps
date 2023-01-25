package com.dashlane.login.pages.enforce2fa

import android.content.Intent
import com.dashlane.R
import com.dashlane.activatetotp.DownloadAuthenticatorAppIntroActivity
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.limitations.Enforce2faLimiter
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginIntents
import com.dashlane.security.DashlaneIntent
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.intro.IntroScreenContract
import com.dashlane.util.inject.qualifiers.ActivityLifecycleCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.skocken.presentation.presenter.BasePresenter
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class Enforce2faLimitPresenter @Inject constructor(
    @ActivityLifecycleCoroutineScope
    private val activityLifecycleCoroutineScope: CoroutineScope,
    @MainCoroutineDispatcher
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val hasEnforced2faLimitUseCase: HasEnforced2faLimitUseCase,
    private val appEvents: AppEvents,
    private val enforce2faLimiter: Enforce2faLimiter
) : BasePresenter<IntroScreenContract.DataProvider, IntroScreenContract.ViewProxy>(),
    IntroScreenContract.Presenter {

    private var redirectionDone = false

    fun onViewStarted() {
        
        activityLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            val session = sessionManager.session ?: return@launch
            if (!hasEnforced2faLimitUseCase(session = session, hasTotpSetupFallback = null)) {
                activity?.let { activity ->
                    activity.startActivity(LoginIntents.createSettingsActivityIntent(activity))
                    activity.finish()
                }
            }
        }
    }

    fun onViewResumed() {
        enforce2faLimiter.setRedirectionDone(true)
    }

    fun onViewPaused() {
        
        if (!redirectionDone) enforce2faLimiter.setRedirectionDone(false)
    }

    override fun onViewChanged() {
        super.onViewChanged()
        view.apply {
            setImageResource(imageResId = R.drawable.picto_authenticator)
            setTitle(R.string.login_dialog_enforce_twofa_limit_title)
            setDescription(R.string.login_dialog_enforce_twofa_limit_description)
            setPositiveButton(R.string.login_dialog_enforce_twofa_positive_button)
            setNegativeButton(R.string.login_dialog_enforce_twofa_negative_button)
        }
    }

    override fun onClickPositiveButton() {
        redirectionDone = true
        context?.startActivity(Intent(context, DownloadAuthenticatorAppIntroActivity::class.java))
    }

    override fun onClickNegativeButton() {
        activityLifecycleCoroutineScope.launch(mainCoroutineDispatcher) {
            sessionManager.session?.let {
                appEvents.clearLastEvent<UnlockEvent>()
                sessionManager.destroySession(it, false)
            }
        }
        val loginIntent = DashlaneIntent.newInstance(context, LoginActivity::class.java)
        context?.startActivity(loginIntent)
        (context as? Enforce2faLimitActivity)?.finishAffinity()
    }

    override fun onClickNeutralButton() {
        
    }

    override fun onClickLink(position: Int, label: Int) {
        
    }
}
