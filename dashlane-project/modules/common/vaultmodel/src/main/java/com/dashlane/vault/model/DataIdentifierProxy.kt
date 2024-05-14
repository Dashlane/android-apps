package com.dashlane.vault.model

import com.dashlane.xml.domain.SyncObjectType

interface DataIdentifierProxy {
    fun getEquivalentSyncObjectType(): SyncObjectType
}