package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType



object PersonalInfosDataTypeFilter : DataTypeFilter {
    override val dataTypes = arrayOf(
        SyncObjectType.IDENTITY,
        SyncObjectType.EMAIL,
        SyncObjectType.PHONE,
        SyncObjectType.ADDRESS,
        SyncObjectType.COMPANY,
        SyncObjectType.PERSONAL_WEBSITE
    )
}