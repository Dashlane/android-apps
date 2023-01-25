package com.dashlane.autofill.api.securitywarnings

import com.dashlane.autofill.api.unlockfill.UnlockedAuthentifiant
import com.dashlane.core.helpers.SignatureVerification
import com.dashlane.core.helpers.SignatureVerification.Incorrect
import com.dashlane.core.helpers.SignatureVerification.UnknownWithSignature
import com.dashlane.hermes.generated.definitions.Domain



interface AutofillSecurityWarningsLogger {
    fun onDisplayedIncorrectWarning(
        security: Incorrect,
        fill: UnlockedAuthentifiant
    )

    fun onDisplayedMismatchWarning(
        security: UnknownWithSignature,
        fill: UnlockedAuthentifiant
    )

    fun onDisplayedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant
    )

    fun onApprovedIncorrectWarning(
        security: Incorrect,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    )

    fun onApprovedMismatchWarning(
        security: UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    )

    fun onApprovedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        neverShowAgain: Boolean,
        domain: Domain
    )

    fun onDeclinedIncorrectWarning(
        security: Incorrect,
        fill: UnlockedAuthentifiant,
        domain: Domain
    )

    fun onDeclinedMismatchWarning(
        security: UnknownWithSignature,
        fill: UnlockedAuthentifiant,
        domain: Domain
    )

    fun onDeclinedUnknownWarning(
        security: SignatureVerification,
        fill: UnlockedAuthentifiant,
        domain: Domain
    )
}
