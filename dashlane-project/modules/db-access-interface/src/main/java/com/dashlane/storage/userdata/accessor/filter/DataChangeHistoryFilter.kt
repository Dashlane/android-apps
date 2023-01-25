package com.dashlane.storage.userdata.accessor.filter

import com.dashlane.xml.domain.SyncObjectType



data class DataChangeHistoryFilter(
    var objectType: SyncObjectType,
    var objectUid: String
)