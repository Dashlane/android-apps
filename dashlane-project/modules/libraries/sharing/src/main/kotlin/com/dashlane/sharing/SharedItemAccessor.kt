package com.dashlane.sharing

import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.GenericFilter
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.ShareableDataTypeFilter
import com.dashlane.storage.userdata.accessor.filter.uid.SpecificUidFilter
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.summary.SummaryObject

suspend fun VaultDataQuery.getSharableItem(uid: String): VaultItem<*>? = query(
    VaultFilter(
        dataTypeFilter = ShareableDataTypeFilter,
        uidFilter = SpecificUidFilter(uid),
    ).also { it.ignoreUserLock() },
)

fun GenericDataQuery.getSharableItem(uid: String): SummaryObject? = queryFirst(
    GenericFilter(
        dataTypeFilter = ShareableDataTypeFilter,
        uidFilter = SpecificUidFilter(uid),
    ).also { it.ignoreUserLock() },
)