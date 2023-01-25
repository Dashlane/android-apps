package com.dashlane.ui.activities.firstpassword

import com.dashlane.csvimport.ImportAuthentifiantHelper
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.util.isValidEmail
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.getDefaultName
import com.dashlane.xml.domain.SyncObfuscatedValue
import com.dashlane.xml.domain.SyncObject
import com.skocken.presentation.provider.BaseDataProvider
import javax.inject.Inject

class AddFirstPasswordDataProvider @Inject constructor(
    private val sessionManager: SessionManager,
    private val mainDataAccessor: MainDataAccessor,
    private val authentifiantHelper: ImportAuthentifiantHelper,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>
) :
    BaseDataProvider<AddFirstPassword.Presenter>(),
    AddFirstPassword.DataProvider {

    override val sessionEmail: String?
        get() = sessionManager.session?.userId

    override fun createCredential(
        url: String,
        login: String,
        password: String
    ): VaultItem<SyncObject.Authentifiant> {
        val isValidEmail = login.isValidEmail()
        val teamspaceAccessor = teamspaceAccessorProvider.get()
        
        val teamId = if (teamspaceAccessor?.canChangeTeamspace() == true) {
            teamspaceAccessor.current?.teamId
        } else {
            null
        }
        return authentifiantHelper.newAuthentifiant(
            deprecatedUrl = url,
            email = login.takeIf { isValidEmail },
            login = login.takeUnless { isValidEmail },
            password = SyncObfuscatedValue(password),
            title = SyncObject.Authentifiant.getDefaultName(url),
            teamId = teamId
        )
    }

    override suspend fun saveCredential(vaultItem: VaultItem<SyncObject.Authentifiant>): Boolean {
        return mainDataAccessor.getDataSaver().save(vaultItem)
    }
}