package com.dashlane.xml.domain

<#list userContentTypes as userContentType>
import com.dashlane.xml.domain.properties.${userContentType.className}
</#list>
import com.dashlane.xml.domain.properties.SyncObjectXml
import javax.annotation.processing.Generated

/**
 * Sets of names of fields which are user editable. This notion is used to determine objects which are duplicates of
 * each other.
 */
@Generated
object SyncUserContentFields {
    val syncObject = setOf(
        <#list baseType.userContentFields as userContentField>
        SyncObjectXml.${userContentField}<#sep>,
        </#list>

    )

    <#list userContentTypes as userContentType>
    val ${userContentType.fieldName} = syncObject + setOf(
        <#list userContentType.userContentFields as userContentField>
        ${userContentType.className}.${userContentField}<#sep>,
        </#list>

    )
    </#list>
}
