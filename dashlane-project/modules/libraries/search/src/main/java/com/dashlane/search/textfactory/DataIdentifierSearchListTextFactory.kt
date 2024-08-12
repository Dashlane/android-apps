package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.vault.textfactory.list.StatusText

interface DataIdentifierSearchListTextFactory {

    fun getLine2FromField(field: SearchField<*>): StatusText?
}