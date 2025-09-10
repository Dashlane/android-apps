package com.dashlane.xml.domain

import com.dashlane.xml.XmlData
import com.dashlane.xml.asCollectionValueOrNull
import com.dashlane.xml.asObjectOrNull
<#list allXmlTypes as transactionType>
import com.dashlane.xml.domain.SyncObject.${transactionType.enclosedClassName}
</#list>
import com.dashlane.xml.toCollectionNode
import com.dashlane.xml.toObjectNode
import javax.annotation.processing.Generated

<#list allXmlTypes as transactionType>
    <#if transactionType.toObject>
@Generated
fun XmlData.as${transactionType.className}OrNull(): ${transactionType.className}? =
    asObjectOrNull(SyncObjectXmlName.${transactionType.enumName}, ::${transactionType.className})

@Generated
fun ${transactionType.className}.toObjectNode(): XmlData.ObjectNode =
    data.toObjectNode(SyncObjectXmlName.${transactionType.enumName})
    <#else>
@Generated
fun XmlData.as${transactionType.className}OrNull(): ${transactionType.className}? =
    asCollectionValueOrNull()?.let { ${transactionType.className}(it) }

@Generated
fun ${transactionType.className}.toCollectionNode(): XmlData.CollectionNode =
    data.toCollectionNode()
    </#if>
<#sep>

</#list>
