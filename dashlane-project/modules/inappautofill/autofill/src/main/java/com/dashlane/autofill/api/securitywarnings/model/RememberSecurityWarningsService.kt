package com.dashlane.autofill.api.securitywarnings.model

import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.core.helpers.SignatureVerification

interface RememberSecurityWarningsService {
    fun remember(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun isItemSourceRemembered(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun isSourceRemembered(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun forgetAll()
}
