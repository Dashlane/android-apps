package com.dashlane.xml.domain

import javax.annotation.processing.Generated

@Generated
object SyncTransactionName {
    <#list transactionTypes as transactionType><#lt>
    const val ${transactionType.enumName} = "${transactionType.transactionName}"
    </#list>
}
