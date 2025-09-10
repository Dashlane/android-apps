package com.dashlane.xml.domain

<#list conflictDuplicateTypes as conflictDuplicateType>
import com.dashlane.xml.domain.properties.${conflictDuplicateType.className}
</#list>
import javax.annotation.processing.Generated

/**
 * Sets of names of fields which should trigger the duplication of the item when there is a sync conflict on the it.
 */
@Generated
object SyncConflictDuplicates {
    <#list conflictDuplicateTypes as conflictDuplicateType>
    val ${conflictDuplicateType.fieldName} = setOf(
        <#list conflictDuplicateType.conflictingFields as conflictingField>
        ${conflictDuplicateType.className}.${conflictingField}<#sep>,
        </#list>

    )
    </#list>
}
