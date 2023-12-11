package com.dashlane.ui.dialogs.fragment

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.fragment.app.FragmentManager
import com.dashlane.R
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.AccountStatusRepository
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.teamspaces.manager.TeamspaceManager
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.util.inject.qualifiers.ApplicationCoroutineScope
import com.dashlane.vault.util.isSpaceJustRevoked
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import javax.inject.Inject

class TeamRevokedDialogDisplayer @Inject constructor(
    private val sessionManager: SessionManager,
    private val teamspaceManagerRepository: TeamspaceManagerRepository,
    private val accountStatusRepository: AccountStatusRepository,
    private val userPreferencesManager: UserPreferencesManager,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope
) : TeamspaceManager.Listener {

    companion object {
        const val ARG_SPACE_REVOKED_ID = "ARG_SPACE_REVOKED_ID"
        private const val REVOKED_DETECTOR_DELAY_MILLIS = 500L
    }

    private var homeActivityRef: WeakReference<HomeActivity>? = null

    private val session
        get() = sessionManager.session
    private val teamManager
        get() = session?.let { teamspaceManagerRepository.getTeamspaceManager(it) }

    override fun onStatusChanged(teamspace: Teamspace?, previousStatus: String?, newStatus: String?) {
        if (!teamspace.isSpaceJustRevoked(previousStatus, newStatus)) {
            return
        }
        
        coroutineScope.launch {
            delay(REVOKED_DETECTOR_DELAY_MILLIS)
            val homeActivity = homeActivityRef?.get() ?: return@launch
            if (!homeActivity.isResume) {
                return@launch
            }
            val fragmentManager = homeActivity.supportFragmentManager
            showIfNecessary(homeActivity, fragmentManager)
        }
    }

    override fun onChange(teamspace: Teamspace?) {
        
    }

    override fun onTeamspacesUpdate() {
        
    }

    fun listenUpcoming(homeActivity: HomeActivity) {
        stopUpcomingListener()
        homeActivityRef = WeakReference(homeActivity)
        teamManager?.subscribeListener(this)
    }

    fun stopUpcomingListener() {
        teamManager?.unSubscribeListeners(this)
        homeActivityRef = null
    }

    fun showIfNecessary(context: Context, fragmentManager: FragmentManager) {
        val currentSession = this.session ?: return
        val spaceId = userPreferencesManager.getString(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR) ?: return
        val teamspace = teamManager?.get(spaceId) ?: return
        val premiumStatus = accountStatusRepository.getPremiumStatus(currentSession)

        if (Teamspace.Status.REVOKED != teamspace.status || teamspace.shouldDelete()) {
            userPreferencesManager.remove(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR)
            return 
        }
        val args = Bundle()
        args.putString(ARG_SPACE_REVOKED_ID, spaceId)
        val title = context.getString(R.string.space_revoked_popup_title, teamspace.teamName)
        val datePremiumOver = DateUtils.formatDateTime(
            context,
            premiumStatus.expiryDate.toEpochMilli(),
            DateUtils.FORMAT_SHOW_DATE or
                DateUtils.FORMAT_SHOW_YEAR or
                DateUtils.FORMAT_NO_MONTH_DAY or
                DateUtils.FORMAT_ABBREV_ALL
        )
        val description = context.getString(R.string.space_revoked_popup_description, datePremiumOver)
        val dialog = NotificationDialogFragment.Builder().setArgs(args)
            .setTitle(title)
            .setMessage(description)
            .setPositiveButtonText(context, R.string.ok)
            .build(SpaceRevokedDialog())
        dialog.show(fragmentManager, null)
    }
}