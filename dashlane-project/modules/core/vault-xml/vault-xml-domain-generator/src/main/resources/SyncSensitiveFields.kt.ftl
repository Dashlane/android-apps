package com.dashlane.xml.domain

<#list sensitiveTypes as sensitiveType>
import com.dashlane.xml.domain.properties.${sensitiveType.className}
</#list>
import javax.annotation.processing.Generated

/**
 * Sets of names of fields which are sensitive. These values should not be resident in memory unless displayed on screen.
 */
@Generated
object SyncSensitiveFields {
    <#list sensitiveTypes as sensitiveType>
    val ${sensitiveType.fieldName} = setOf(
        <#list sensitiveType.sensitiveFields as sensitiveField>
        ${sensitiveType.className}.${sensitiveField}<#sep>,
        </#list>

    )
    </#list>
}
