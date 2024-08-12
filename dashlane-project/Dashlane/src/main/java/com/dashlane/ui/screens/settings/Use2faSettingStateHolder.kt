package com.dashlane.ui.screens.settings

import com.dashlane.user.UserAccountInfo
import com.dashlane.account.UserAccountStorage
import com.dashlane.authentication.SecurityFeature
import com.dashlane.authentication.UserStorage
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.SessionManager
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Singleton
class Use2faSettingStateHolder @Inject constructor(
    private val sessionManager: SessionManager,
    private val userStorage: UserStorage,
    private val auth2faSettingsService: Auth2faSettingsService,
    private val userAccountStorage: UserAccountStorage
) : Use2faSettingStateRefresher {
    private val _use2faSettingStateFlow: MutableStateFlow<Use2faSettingState> =
        MutableStateFlow(Use2faSettingState.Unavailable)

    val use2faSettingStateFlow: StateFlow<Use2faSettingState> get() = _use2faSettingStateFlow

    override suspend fun refresh() {
        val session = sessionManager.session
        if (session == null) {
            _use2faSettingStateFlow.value = Use2faSettingState.Unavailable
            return
        }

        val accountType = userAccountStorage[session.username]?.accountType
        if (accountType is UserAccountInfo.AccountType.InvisibleMasterPassword) {
            _use2faSettingStateFlow.value = Use2faSettingState.Unavailable
            return
        }

        val localUse2faSettingState =
            userStorage.getUser(session.userId)?.securityFeatures?.toUse2faSettingState()
                ?: Use2faSettingState.Unavailable

        if (localUse2faSettingState != Use2faSettingState.Unavailable) {
            _use2faSettingStateFlow.value = Use2faSettingState.Loading
        }

        try {
            _use2faSettingStateFlow.value = auth2faSettingsService.execute(session.authorization)
                .data.type.toUse2faSettingState()
        } catch (_: DashlaneApiException) {
            _use2faSettingStateFlow.value = localUse2faSettingState
        }
    }
}

private fun Set<SecurityFeature>.toUse2faSettingState() = when {
    SecurityFeature.TOTP in this ||
        SecurityFeature.DUO in this ||
        SecurityFeature.U2F in this -> Use2faSettingState.Available(
        enabled = false,
        checked = true
    )

    SecurityFeature.EMAIL_TOKEN in this -> Use2faSettingState.Available(
        enabled = false,
        checked = false
    )

    else -> Use2faSettingState.Unavailable
}

private fun AuthSecurityType.toUse2faSettingState() = when (this) {
    AuthSecurityType.EMAIL_TOKEN -> Use2faSettingState.Available(
        enabled = true,
        checked = false
    )

    AuthSecurityType.TOTP_LOGIN,
    AuthSecurityType.TOTP_DEVICE_REGISTRATION -> Use2faSettingState.Available(
        enabled = true,
        checked = true
    )

    AuthSecurityType.SSO -> Use2faSettingState.Unavailable
}
