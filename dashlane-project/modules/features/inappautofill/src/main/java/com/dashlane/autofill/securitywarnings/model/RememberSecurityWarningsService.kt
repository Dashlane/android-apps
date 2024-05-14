package com.dashlane.autofill.securitywarnings.model

import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant
import com.dashlane.core.helpers.SignatureVerification

interface RememberSecurityWarningsService {
    fun remember(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun isItemSourceRemembered(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun isSourceRemembered(unlockedAuthentifiant: UnlockedAuthentifiant, verification: SignatureVerification): Boolean
    fun forgetAll()
}
