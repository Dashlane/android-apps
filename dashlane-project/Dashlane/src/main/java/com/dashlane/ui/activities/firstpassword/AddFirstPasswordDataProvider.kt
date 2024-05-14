package com.dashlane.ui.activities.firstpassword

import com.dashlane.csvimport.csvimport.ImportAuthentifiantHelper
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
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
    private val dataSaver: DataSaver,
    private val authentifiantHelper: ImportAuthentifiantHelper,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>,
    private val currentTeamSpaceUiFilter: CurrentTeamSpaceUiFilter
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
        val teamSpaceAccessor = teamSpaceAccessorProvider.get()
        
        val teamId = if (teamSpaceAccessor?.canChangeTeamspace == true) {
            currentTeamSpaceUiFilter.currentFilter.teamSpace.teamId
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
        return dataSaver.save(vaultItem)
    }
}