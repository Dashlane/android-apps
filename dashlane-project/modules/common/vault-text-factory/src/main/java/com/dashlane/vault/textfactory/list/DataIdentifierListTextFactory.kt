package com.dashlane.vault.textfactory.list

import com.dashlane.vault.summary.SummaryObject

interface DataIdentifierListTextFactory<T : SummaryObject> {


    fun getTitle(item: T): StatusText

    fun getDescription(item: T, default: StatusText): StatusText
}
