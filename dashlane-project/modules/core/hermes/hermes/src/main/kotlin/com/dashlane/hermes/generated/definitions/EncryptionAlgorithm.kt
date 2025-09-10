package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class EncryptionAlgorithm(override val code: String) : TrackingLog.Enum {
    OTHER(
        code = "other",
    ),
    SHA1(
        code = "sha1",
    ),
    SHA256(
        code = "sha256",
    ),
    SHA512(
        code = "sha512",
    ),
}