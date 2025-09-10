package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class EncryptionServicePlatformSelected(override val code: String) : TrackingLog.Enum {
    AMAZON_WEB_SERVICES(
        code = "amazon_web_services",
    ),
    AZURE(
        code = "azure",
    ),
}