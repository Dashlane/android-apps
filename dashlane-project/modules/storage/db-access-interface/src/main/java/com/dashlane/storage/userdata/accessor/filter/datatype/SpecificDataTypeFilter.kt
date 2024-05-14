package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType

class SpecificDataTypeFilter(override vararg val dataTypes: SyncObjectType) : DataTypeFilter