package com.dashlane.masterpassword

import com.dashlane.account.UserAccountStorage
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.UserDatabaseRepository
import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.user.UserAccountInfo
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class ChangeMasterPasswordFeatureAccessChecker @Inject constructor(
    private val sessionManager: SessionManager,
    private val userAccountStorage: UserAccountStorage,
    private val userDatabaseRepository: UserDatabaseRepository,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) {
    @JvmOverloads
    fun canAccessFeature(fromMigrationToMasterPasswordUser: Boolean = false): Boolean {
        return when {
            !canChangeMasterPassword() -> false
            
            fromMigrationToMasterPasswordUser -> true
            
            else -> teamSpaceAccessorProvider.get()?.isSsoUser == false
        }
    }

    private fun canChangeMasterPassword(): Boolean {
        val session = sessionManager.session ?: return false
        
        val accountType = userAccountStorage[session.username]?.accountType
        return accountType != UserAccountInfo.AccountType.InvisibleMasterPassword &&
            userDatabaseRepository.isRacletteDatabaseAccessibleLegacy(session)
    }
}