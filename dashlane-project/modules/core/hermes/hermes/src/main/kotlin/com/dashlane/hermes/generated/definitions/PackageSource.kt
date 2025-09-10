package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class PackageSource(override val code: String) : TrackingLog.Enum {
    CARBON(
        code = "carbon",
    ),
    GRAPHENE(
        code = "graphene",
    ),
}