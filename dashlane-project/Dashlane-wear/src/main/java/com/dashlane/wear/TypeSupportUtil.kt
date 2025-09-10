package com.dashlane.wear

import com.dashlane.xml.domain.SyncObjectType.AUTHENTIFIANT
import com.dashlane.xml.domain.SyncObjectType.PASSKEY

object TypeSupportUtil {
    val supportedVaultItemTypes = listOf(AUTHENTIFIANT, PASSKEY)
}