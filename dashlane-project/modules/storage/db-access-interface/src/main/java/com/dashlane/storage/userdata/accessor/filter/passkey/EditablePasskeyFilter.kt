package com.dashlane.storage.userdata.accessor.filter.passkey

import com.dashlane.storage.userdata.accessor.filter.PasskeyFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.DataTypeFilter

interface EditablePasskeyFilter : DataTypeFilter {
    var passkeyFilter: PasskeyFilter

    fun onlyLocalPasskeys() {
        passkeyFilter = PasskeyFilter.LocalPasskeysFilter
    }
}