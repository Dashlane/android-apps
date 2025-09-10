package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PublicApiKeyStep(override val code: String) : TrackingLog.Enum {
    COPY_KEY(
        code = "copy_key",
    ),
    CREATE_KEY(
        code = "create_key",
    ),
    DOWNLOAD_KEY(
        code = "download_key",
    ),
    GENERATE_KEY(
        code = "generate_key",
    ),
    KEY_GENERATED(
        code = "key_generated",
    ),
    KEY_GENERATION_FAILED(
        code = "key_generation_failed",
    ),
}