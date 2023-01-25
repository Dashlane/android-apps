package com.dashlane.login.pages.enforce2fa

import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.Auth2faSettingsService
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.EMAIL_TOKEN
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.SSO
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_DEVICE_REGISTRATION
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_LOGIN
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.is2FAEnforced
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.userfeatures.UserFeaturesChecker
import com.dashlane.util.userfeatures.UserFeaturesChecker.FeatureFlip
import javax.inject.Inject



class HasEnforced2FaLimitUseCaseImpl @Inject constructor(
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>,
    private val auth2faSettingsService: Auth2faSettingsService,
    private val userFeaturesChecker: UserFeaturesChecker
) : HasEnforced2faLimitUseCase {
    private val hasFeature: Boolean
        get() = userFeaturesChecker.has(FeatureFlip.ENFORCED_2FA_POLICY)

    override suspend fun invoke(session: Session, hasTotpSetupFallback: Boolean?): Boolean {
        return if (hasFeature && teamspaceAccessorProvider.get().is2FAEnforced()) {
            try {
                !hasTotpSetup(session)
            } catch (e: DashlaneApiException) {
                
                hasTotpSetupFallback == false
            }
        } else {
            false
        }
    }

    @Throws(DashlaneApiException::class)
    private suspend fun hasTotpSetup(session: Session): Boolean {
        val response = auth2faSettingsService.execute(userAuthorization = session.authorization)
        return when (response.data.type) {
            TOTP_LOGIN -> true
            TOTP_DEVICE_REGISTRATION -> true
            EMAIL_TOKEN -> false
            SSO -> false
        }
    }
}
