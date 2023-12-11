package com.dashlane.teamspaces.manager

import android.content.Context
import android.widget.Toast
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.navigation.Navigator
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.util.Toaster
import com.dashlane.vault.util.isSpaceJustRevoked
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class RevokedDetector @Inject constructor(
    private val toaster: Toaster,
    @ApplicationContext private val context: Context,
    private val navigator: Lazy<Navigator>, 
    private val userAccountStorage: UserAccountStorage,
    private val sessionManager: SessionManager,
    private val userPreferencesManager: UserPreferencesManager
) : TeamspaceManager.Listener {

    override fun onStatusChanged(
        teamspace: Teamspace?,
        previousStatus: String?,
        newStatus: String?
    ) {
        if (!teamspace.isSpaceJustRevoked(previousStatus, newStatus)) {
            return
        }

        
        if (shouldLogoutIfRevokedFromSso(context, teamspace, toaster)) {
            navigator.get().logoutAndCallLoginScreen(context, false)
            return
        }
        userPreferencesManager.putString(
            ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR,
            teamspace!!.teamId
        )
    }

    override fun onChange(teamspace: Teamspace?) {
        
    }

    override fun onTeamspacesUpdate() {
        
    }

    private fun shouldLogoutIfRevokedFromSso(context: Context, teamspace: Teamspace?, toaster: Toaster): Boolean {
        val session = sessionManager.session ?: return false
        val userAccountInfo = userAccountStorage[session.username]
        if (userAccountInfo == null || !userAccountInfo.sso) {
            
            return false
        }
        toaster.show(
            context.getString(R.string.space_revoked_popup_title, teamspace!!.teamName),
            Toast.LENGTH_LONG
        )
        return true
    }
}
