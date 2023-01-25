package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType



object CredentialsDataTypeFilter : DataTypeFilter {
    override val dataTypes = arrayOf(SyncObjectType.AUTHENTIFIANT)
}