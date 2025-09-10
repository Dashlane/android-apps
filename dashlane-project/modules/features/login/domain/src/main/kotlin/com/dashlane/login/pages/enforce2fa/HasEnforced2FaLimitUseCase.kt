package com.dashlane.login.pages.enforce2fa

import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.EMAIL_TOKEN
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.SSO
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_DEVICE_REGISTRATION
import com.dashlane.server.api.endpoints.authentication.AuthSecurityType.TOTP_LOGIN
import com.dashlane.server.api.endpoints.authentication.Get2FAStatusService
import com.dashlane.server.api.exceptions.DashlaneApiException
import com.dashlane.session.Session
import com.dashlane.session.authorization
import com.dashlane.teamspaces.manager.is2FAEnforced
import javax.inject.Inject

class HasEnforced2FaLimitUseCase @Inject constructor(
    private val accountStatusRepository: AccountStatusRepository,
    private val get2FAStatusService: Get2FAStatusService,
) {
    suspend operator fun invoke(session: Session, hasTotpSetupFallback: Boolean?): Boolean {
        val accountStatus = accountStatusRepository[session] ?: accountStatusRepository.reloadStatus(session)
        return if (accountStatus.is2FAEnforced() == true) {
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
        val response = get2FAStatusService.execute(userAuthorization = session.authorization)
        return when (response.data.type) {
            TOTP_LOGIN -> false
            TOTP_DEVICE_REGISTRATION -> false
            SSO -> false
            EMAIL_TOKEN -> true
        }
    }
}