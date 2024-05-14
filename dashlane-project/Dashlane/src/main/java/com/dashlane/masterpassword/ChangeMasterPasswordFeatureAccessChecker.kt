package com.dashlane.masterpassword

import com.dashlane.teamspaces.manager.TeamSpaceAccessor
import com.dashlane.util.inject.OptionalProvider
import javax.inject.Inject

class ChangeMasterPasswordFeatureAccessChecker @Inject constructor(
    private val masterPasswordChanger: MasterPasswordChanger,
    private val teamSpaceAccessorProvider: OptionalProvider<TeamSpaceAccessor>
) {
    @JvmOverloads
    fun canAccessFeature(fromMigrationToMasterPasswordUser: Boolean = false): Boolean {
        return when {
            !masterPasswordChanger.canChangeMasterPassword -> false
            
            fromMigrationToMasterPasswordUser -> true
            
            else -> teamSpaceAccessorProvider.get()?.isSsoUser == false
        }
    }
}