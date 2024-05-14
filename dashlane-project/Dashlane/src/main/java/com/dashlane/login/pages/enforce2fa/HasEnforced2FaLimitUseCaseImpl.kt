package com.dashlane.login.pages.enforce2fa

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.EMAIL_TOKEN
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.SSO
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_DEVICE_REGISTRATION
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_LOGIN
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class HasEnforced2FaLimitUseCaseImpl @Inject constructor(
    private val teamSpaceAccessor: OptionalProvider<TeamSpaceAccessor>,
    private val auth2faSettingsService: Auth2faSettingsService
) : HasEnforced2faLimitUseCase {

    override suspend fun invoke(session: Session, hasTotpSetupFallback: Boolean?): Boolean {
        return if (teamSpaceAccessor.get()?.is2FAEnforced == true) {
            try {
                shouldEnforce2FA(session)
            } catch (e: DashlaneApiException) {
                
                hasTotpSetupFallback == false
            }
        } else {
            false
        }
    }

    @Throws(DashlaneApiException::class)
    private suspend fun shouldEnforce2FA(session: Session): Boolean {
        val response = auth2faSettingsService.execute(userAuthorization = session.authorization)
        return when (response.data.type) {
            TOTP_LOGIN -> false
            TOTP_DEVICE_REGISTRATION -> false
            SSO -> false
            EMAIL_TOKEN -> true
        }
    }
}
