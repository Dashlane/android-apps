package com.dashlane.passwordanalysis

import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.loginForUi
import com.dashlane.vault.model.navigationUrl
import com.dashlane.vault.model.titleForListNormalized
import com.dashlane.xml.domain.SyncObject

fun VaultItem<SyncObject.Authentifiant>.toAnalyzedAuthentifiant() =
    AnalyzedAuthentifiant(
        checked = syncObject.checked == true,
        password = syncObject.password?.toString().orEmpty(),
        navigationUrl = navigationUrl,
        titleForListNormalized = titleForListNormalized,
        loginForUi = loginForUi,
        item = this,
    )

data class AnalyzedAuthentifiant(
    val checked: Boolean,
    val password: String,
    val navigationUrl: String?,
    val titleForListNormalized: String?,
    val loginForUi: String?,
    
    
    val item: VaultItem<SyncObject.Authentifiant>,
)