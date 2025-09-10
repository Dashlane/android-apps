package com.dashlane.autofill.securitywarnings

import com.dashlane.autofill.unlockfill.UnlockedAuthentifiant
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.core.helpers.SignatureVerification.Incorrect
import com.dashlane.core.helpers.SignatureVerification.UnknownWithSignature

interface AutofillSecurityWarningsLogger {
    fun onDisplayedIncorrectWarning(
        security: Incorrect,
        fill: UnlockedAuthentifiant,
    )

    fun onDisplayedMismatchWarning(
        security: UnknownWithSignature,
        fill: UnlockedAuthentifiant,
    )

    fun onDisplayedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
    )

    fun onApprovedWarning(
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
    )

    fun onDeclinedWarning(
        fill: UnlockedAuthentifiant,
    )
}