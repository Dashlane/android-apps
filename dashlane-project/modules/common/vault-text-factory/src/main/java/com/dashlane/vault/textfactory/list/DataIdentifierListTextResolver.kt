package com.dashlane.vault.textfactory.list

import com.dashlane.vault.summary.SummaryObject

interface DataIdentifierListTextResolver {
    fun getLine1(item: SummaryObject): StatusText
    fun getLine2(item: SummaryObject): StatusText
}