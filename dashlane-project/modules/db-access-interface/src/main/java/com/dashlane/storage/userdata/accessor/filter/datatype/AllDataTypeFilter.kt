package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType



object AllDataTypeFilter : DataTypeFilter {
    override val dataTypes = SyncObjectType.values()
}