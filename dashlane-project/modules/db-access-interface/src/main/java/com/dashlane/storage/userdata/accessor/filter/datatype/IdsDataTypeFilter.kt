package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType



object IdsDataTypeFilter : DataTypeFilter {
    override val dataTypes = arrayOf(
        SyncObjectType.ID_CARD,
        SyncObjectType.PASSPORT,
        SyncObjectType.SOCIAL_SECURITY_STATEMENT,
        SyncObjectType.DRIVER_LICENCE,
        SyncObjectType.FISCAL_STATEMENT
    )
}