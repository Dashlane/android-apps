package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ${className}(override val code: String) : TrackingLog.Enum {
    <#list entries as entry>
    ${entry.name}(
        code = "${entry.code}",
    ),
    </#list>
}