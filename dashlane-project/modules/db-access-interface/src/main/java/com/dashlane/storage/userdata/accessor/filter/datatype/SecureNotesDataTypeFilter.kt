package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType



object SecureNotesDataTypeFilter : DataTypeFilter {
    override val dataTypes = arrayOf(SyncObjectType.SECURE_NOTE)
}