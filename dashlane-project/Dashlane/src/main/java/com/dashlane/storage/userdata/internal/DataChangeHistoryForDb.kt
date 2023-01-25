package com.dashlane.storage.userdata.internal

import com.dashlane.vault.model.CommonDataIdentifierAttrs
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import java.io.Serializable
import java.time.Instant

data class DataChangeHistoryForDb(
    val dataIdentifier: CommonDataIdentifierAttrs = CommonDataIdentifierAttrsImpl(),
    val objectUID: String,
    val objectTypeId: Int = 0,
    val objectTitle: String? = null,
    val extraData: String? = null,
    val backupDate: Instant? = null
) : Serializable
