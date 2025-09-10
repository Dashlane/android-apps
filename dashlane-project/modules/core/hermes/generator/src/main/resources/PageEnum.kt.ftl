package com.dashlane.hermes.generated.definitions

import com.dashlane.hermes.TrackingLog
import javax.annotation.processing.Generated

@Generated
enum class ${className}(override val code: String, override val priority: Boolean) : TrackingLog.PageEnum {
    <#list pagesEntries as entry>
    ${entry.name}(
        code = "${entry.code}",
        priority = false,
    ),
    </#list>

    <#list priorityPagesEntries as entry>
    ${entry.name}(
        code = "${entry.code}",
        priority = true,
    ),
    </#list>
}