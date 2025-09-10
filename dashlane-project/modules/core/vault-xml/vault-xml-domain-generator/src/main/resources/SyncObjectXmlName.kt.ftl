package com.dashlane.xml.domain

import javax.annotation.processing.Generated

@Generated
object SyncObjectXmlName {
    <#list transactionTypes as transactionType><#lt>
    const val ${transactionType.enumName} = "${transactionType.xmlName}"
    <#list transactionType.innerTypes as innerType>
    const val ${innerType.enumName} = "${innerType.xmlName}"
    </#list>
    </#list>
}
