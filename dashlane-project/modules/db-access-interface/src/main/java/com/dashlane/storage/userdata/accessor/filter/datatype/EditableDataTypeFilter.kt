package com.dashlane.storage.userdata.accessor.filter.datatype

import com.dashlane.xml.domain.SyncObjectType

interface EditableDataTypeFilter : DataTypeFilter {

    var dataTypeFilter: DataTypeFilter

    fun inAllDataType() {
        dataTypeFilter = AllDataTypeFilter
    }

    fun specificDataType(vararg dataTypes: SyncObjectType) {
        dataTypeFilter = SpecificDataTypeFilter(*dataTypes)
    }

    fun specificDataType(dataTypes: Collection<SyncObjectType>) {
        specificDataType(*dataTypes.toTypedArray())
    }
}