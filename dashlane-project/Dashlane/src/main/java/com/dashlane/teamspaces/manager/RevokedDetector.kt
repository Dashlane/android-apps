package com.dashlane.teamspaces.manager

import android.content.Context
import android.widget.Toast
import com.dashlane.R
import com.dashlane.account.UserAccountStorage
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.currentTeam
import com.dashlane.accountstatus.premiumstatus.pastTeams
import com.dashlane.navigation.Navigator
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.CurrentTeam
import com.dashlane.server.api.endpoints.premium.PremiumStatus.B2bStatus.PastTeam
import com.dashlane.session.SessionManager
import com.dashlane.util.Toaster
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
) {

    fun onStatusChanged(
        newStatus: AccountStatus,
        oldStatus: AccountStatus?
    ) {
        val previousTeam = oldStatus?.premiumStatus?.currentTeam ?: return
        val pastTeams = newStatus.premiumStatus.pastTeams

        if (!isSpaceJustRevoked(previousTeam, pastTeams)) {
            
            
            
            return
        }

        
        if (shouldLogoutIfRevokedFromSso(context, previousTeam, toaster)) {
            navigator.get().logoutAndCallLoginScreen(context, false)
            return
        }
        userPreferencesManager.putString(
            ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR,
            previousTeam.teamId.toString()
        )
    }

    private fun shouldLogoutIfRevokedFromSso(context: Context, revokedTeam: CurrentTeam, toaster: Toaster): Boolean {
        val session = sessionManager.session ?: return false
        val userAccountInfo = userAccountStorage[session.username]
        if (userAccountInfo == null || !userAccountInfo.sso) {
            
            return false
        }
        toaster.show(
            context.getString(R.string.space_revoked_popup_title, revokedTeam.teamName),
            Toast.LENGTH_LONG
        )
        return true
    }

    private fun isSpaceJustRevoked(
        oldCurrentTeam: CurrentTeam?,
        pastTeams: List<PastTeam>?
    ): Boolean {
        return pastTeams?.map { it.teamId }?.contains(oldCurrentTeam?.teamId) == true
    }
}
