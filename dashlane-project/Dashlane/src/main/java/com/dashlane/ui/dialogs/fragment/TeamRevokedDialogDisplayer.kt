package com.dashlane.ui.dialogs.fragment

import android.content.Context
import android.os.Bundle
import android.text.format.DateUtils
import androidx.fragment.app.FragmentManager
import com.dashlane.R
import com.dashlane.accountstatus.AccountStatus
import com.dashlane.accountstatus.premiumstatus.endDate
import com.dashlane.preference.ConstantsPrefs
import com.dashlane.preference.PreferencesManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.server.api.endpoints.premium.PremiumStatus
import com.dashlane.session.SessionManager
import com.dashlane.ui.activities.HomeActivity
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment
import com.dashlane.util.inject.OptionalProvider
import com.dashlane.utils.coroutines.inject.qualifiers.ApplicationCoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference
import java.time.Instant
import javax.inject.Inject

class TeamRevokedDialogDisplayer @Inject constructor(
    private val accountStatusProvider: OptionalProvider<AccountStatus>,
    private val sessionManager: SessionManager,
    private val preferencesManager: PreferencesManager,
    @ApplicationCoroutineScope private val coroutineScope: CoroutineScope
) {

    companion object {
        const val ARG_SPACE_REVOKED_ID = "ARG_SPACE_REVOKED_ID"
        private const val REVOKED_DETECTOR_DELAY_MILLIS = 500L
    }

    private val userPreferencesManager: UserPreferencesManager
        get() = preferencesManager[sessionManager.session?.username]

    private val premiumStatus: PremiumStatus?
        get() = accountStatusProvider.get()?.premiumStatus

    fun onStatusChanged(homeActivityRef: WeakReference<HomeActivity>) {
        coroutineScope.launch {
            delay(REVOKED_DETECTOR_DELAY_MILLIS)
            
            val revokedSpaceId = userPreferencesManager.getString(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR) ?: return@launch
            val homeActivity = homeActivityRef.get() ?: return@launch
            if (!homeActivity.isResume) {
                return@launch
            }
            val fragmentManager = homeActivity.supportFragmentManager
            showIfNecessary(revokedSpaceId, homeActivity, fragmentManager)
        }
    }

    private fun showIfNecessary(revokedSpaceId: String, context: Context, fragmentManager: FragmentManager) {
        val premiumStatus = this.premiumStatus ?: return
        val revokedTeam = premiumStatus.b2bStatus?.pastTeams?.find { it.teamId.toString() == revokedSpaceId }

        
        if (revokedTeam == null) {
            userPreferencesManager.remove(ConstantsPrefs.NEED_POPUP_SPACE_REVOKED_FOR)
            return 
        }
        val args = Bundle()
        args.putString(ARG_SPACE_REVOKED_ID, revokedSpaceId)
        val title = context.getString(R.string.space_revoked_popup_title, revokedTeam.teamName)
        val datePremiumOver = DateUtils.formatDateTime(
            context,
            (premiumStatus.endDate ?: Instant.now()).toEpochMilli(),
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