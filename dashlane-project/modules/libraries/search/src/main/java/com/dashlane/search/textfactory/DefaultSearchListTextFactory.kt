package com.dashlane.search.textfactory

import com.dashlane.search.SearchField
import com.dashlane.vault.textfactory.list.StatusText

class DefaultSearchListTextFactory : DataIdentifierSearchListTextFactory {

    override fun getLine2FromField(field: SearchField<*>): StatusText? = null
}