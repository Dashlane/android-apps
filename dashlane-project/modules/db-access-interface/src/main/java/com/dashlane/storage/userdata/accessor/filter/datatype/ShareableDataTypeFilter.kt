package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType
import com.dashlane.xml.domain.SyncObjectTypeUtils.SHAREABLE

object ShareableDataTypeFilter : DataTypeFilter {
    override val dataTypes = SHAREABLE.toTypedArray()
}