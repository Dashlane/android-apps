package com.dashlane.limitations

import android.app.Activity
import android.os.Bundle
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.authentication.twofa.Use2faSettingStateHolder
import com.dashlane.login.pages.enforce2fa.HasEnforced2FaLimitUseCase
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusService
import com.dashlane.session.SessionManager
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.utils.coroutines.inject.qualifiers.MainCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class Enforce2faLimiter(
    private val applicationCoroutineScope: CoroutineScope,
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val enforced2faLimitUseCase: HasEnforced2FaLimitUseCase,
    private val use2faSettingState: Use2faSettingStateHolder,
    private val loginIntentFactory: LoginIntentFactory,
) : AbstractActivityLifecycleListener() {
    @Inject
    constructor(
        @ApplicationCoroutineScope
        applicationCoroutineScope: CoroutineScope,
        @MainCoroutineDispatcher
        mainCoroutineDispatcher: CoroutineDispatcher,
        sessionManager: SessionManager,
        accountStatusRepository: AccountStatusRepository,
        get2FAStatusService: Get2FAStatusService,
        use2faSettingState: Use2faSettingStateHolder,
        loginIntentFactory: LoginIntentFactory,
    ) : this(
        applicationCoroutineScope = applicationCoroutineScope,
        mainCoroutineDispatcher = mainCoroutineDispatcher,
        sessionManager = sessionManager,
        enforced2faLimitUseCase = HasEnforced2FaLimitUseCase(
            accountStatusRepository = accountStatusRepository,
            get2FAStatusService = get2FAStatusService,
        ),
        use2faSettingState = use2faSettingState,
        loginIntentFactory = loginIntentFactory,
    )

    private var shouldCheckLimit = false
    private var hasEnforce2faLimit: Boolean? = null
    private var redirectionDone = false

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        redirectionDone = false
        checkLimit(activity)
        listenTo2FAChanges()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        sessionManager.session ?: return
        if (shouldCheckLimit) checkLimit(activity)
        mayShowIntro(activity)
    }

    fun setRedirectionDone(redirectionDone: Boolean) {
        this.redirectionDone = redirectionDone
    }

    private fun checkLimit(activity: Activity) {
        
        val session = sessionManager.session ?: return
        applicationCoroutineScope.launch(mainCoroutineDispatcher) {
            hasEnforce2faLimit = enforced2faLimitUseCase(
                session = session,
                hasTotpSetupFallback = null,
            )
            if (!activity.isFinishing) mayShowIntro(activity)
        }
        shouldCheckLimit = false
    }

    private fun listenTo2FAChanges() {
        use2faSettingState.use2faSettingStateFlow
            .onEach {
                if (it.enabled && it.checked == hasEnforce2faLimit) {
                    shouldCheckLimit = true
                    this.redirectionDone = false
                }
            }
            .launchIn(applicationCoroutineScope)
    }

    private fun mayShowIntro(activity: Activity) {
        if (hasEnforce2faLimit != true || redirectionDone || activity !is HomeActivity) return
        val intent = loginIntentFactory.createEnforce2faLimitActivityIntent(activity, clearTask = false)
        activity.startActivity(intent)
    }
}