package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.vault.util.SyncObjectTypeUtils
import com.dashlane.xml.domain.SyncObjectType



object ShareableDataTypeFilter : DataTypeFilter {
    override val dataTypes = SyncObjectTypeUtils.SHAREABLE.toTypedArray()
}