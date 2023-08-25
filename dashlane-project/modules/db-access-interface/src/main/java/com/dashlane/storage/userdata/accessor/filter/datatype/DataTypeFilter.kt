package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType

interface DataTypeFilter {

    val dataTypes: Array<out SyncObjectType>

    fun has(dataType: SyncObjectType) = dataType in dataTypes
}