package com.dashlane.masterpassword

import com.dashlane.teamspaces.manager.TeamspaceAccessor
import com.dashlane.teamspaces.manager.isSsoUser
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class ChangeMasterPasswordFeatureAccessChecker @Inject constructor(
    private val masterPasswordChanger: MasterPasswordChanger,
    private val teamspaceAccessorProvider: OptionalProvider<TeamspaceAccessor>
) {
    @JvmOverloads
    fun canAccessFeature(fromMigrationToMasterPasswordUser: Boolean = false): Boolean {
        return when {
            !masterPasswordChanger.canChangeMasterPassword -> false
            
            fromMigrationToMasterPasswordUser -> true
            
            else -> teamspaceAccessorProvider.get()?.isSsoUser == false
        }
    }
}