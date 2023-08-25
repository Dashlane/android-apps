package com.dashlane.login.pages.enforce2fa

import com.dashlane.session.Session

interface HasEnforced2faLimitUseCase {
    suspend operator fun invoke(session: Session, hasTotpSetupFallback: Boolean?): Boolean
}