package com.dashlane.limitations

import android.app.Activity
import android.os.Bundle
import com.dashlane.login.LoginIntents.createEnforce2faLimitActivityIntent
import com.dashlane.login.pages.enforce2fa.HasEnforced2FaLimitUseCaseImpl
import com.dashlane.login.pages.enforce2fa.HasEnforced2faLimitUseCase
import com.dashlane.login.pages.password.LoginPasswordDataProvider
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.ui.AbstractActivityLifecycleListener
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.screens.settings.Use2faSettingStateHolder
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.inject.qualifiers.GlobalCoroutineScope
import com.dashlane.util.inject.qualifiers.MainCoroutineDispatcher
import com.dashlane.util.userfeatures.UserFeaturesChecker
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch



@Singleton
class Enforce2faLimiter(
    private val globalCoroutineScope: CoroutineScope,
    private val mainCoroutineDispatcher: CoroutineDispatcher,
    private val sessionManager: SessionManager,
    private val enforced2faLimitUseCase: HasEnforced2faLimitUseCase,
    private val use2faSettingState: Use2faSettingStateHolder
) : AbstractActivityLifecycleListener() {

    @Inject
    constructor(
        @GlobalCoroutineScope
        globalCoroutineScope: CoroutineScope,
        @MainCoroutineDispatcher
        mainCoroutineDispatcher: CoroutineDispatcher,
        sessionManager: SessionManager,
        teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
        auth2faSettingsService: Auth2faSettingsService,
        userFeaturesChecker: UserFeaturesChecker,
        use2faSettingState: Use2faSettingStateHolder
    ) : this(
        globalCoroutineScope = globalCoroutineScope,
        mainCoroutineDispatcher = mainCoroutineDispatcher,
        sessionManager = sessionManager,
        enforced2faLimitUseCase = HasEnforced2FaLimitUseCaseImpl(
            teamspaceAccessorProvider = teamspaceAccessorProvider,
            auth2faSettingsService = auth2faSettingsService,
            userFeaturesChecker = userFeaturesChecker
        ),
        use2faSettingState = use2faSettingState
    )

    var isFirstLogin = false
    private var shouldCheckLimit = false
    private var hasEnforce2faLimit: Boolean? = null
    private var redirectionDone = false

    override fun onFirstLoggedInActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
        super.onFirstLoggedInActivityCreated(activity, savedInstanceState)
        redirectionDone = false
        if (isFirstLogin) return
        checkLimit(activity)
        listenTo2FAChanges()
    }

    override fun onActivityResumed(activity: Activity) {
        super.onActivityResumed(activity)
        sessionManager.session ?: return
        if (isFirstLogin) return
        if (shouldCheckLimit) checkLimit(activity)
        mayShowIntro(activity)
    }

    fun setRedirectionDone(redirectionDone: Boolean) {
        this.redirectionDone = redirectionDone
    }

    private fun checkLimit(activity: Activity) {
        
        val session = sessionManager.session ?: return
        globalCoroutineScope.launch(mainCoroutineDispatcher) {
            hasEnforce2faLimit = enforced2faLimitUseCase(
                session = session,
                hasTotpSetupFallback = null
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
            .launchIn(globalCoroutineScope)
    }

    private fun mayShowIntro(activity: Activity) {
        if (hasEnforce2faLimit != true || redirectionDone || activity !is HomeActivity) return
        val intent = createEnforce2faLimitActivityIntent(activity = activity, clearTask = false)
        activity.startActivity(intent)
    }
}
