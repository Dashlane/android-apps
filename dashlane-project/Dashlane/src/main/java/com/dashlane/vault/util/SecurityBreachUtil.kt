package com.dashlane.vault.util

import com.dashlane.similarpassword.SimilarPassword
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.leakedPasswordsSet
import com.dashlane.xml.domain.SyncObject

object SecurityBreachUtil {
    fun List<VaultItem<SyncObject>>.isCompromised(similarPassword: SimilarPassword, password: String) =
        any {
            (it.syncObject as? SyncObject.SecurityBreach)?.leakedPasswordsSet?.any { leakPassword ->
                similarPassword.areSimilar(password, leakPassword)
            } ?: false
        }
}
