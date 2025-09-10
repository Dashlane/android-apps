package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class CryptoAlgorithm(override val code: String) : TrackingLog.Enum {
    ARGON2D(
        code = "argon2d",
    ),
    KWC3(
        code = "kwc3",
    ),
    KWC5(
        code = "kwc5",
    ),
    NO_DERIVATION(
        code = "no_derivation",
    ),
    PBKDF2(
        code = "pbkdf2",
    ),
}