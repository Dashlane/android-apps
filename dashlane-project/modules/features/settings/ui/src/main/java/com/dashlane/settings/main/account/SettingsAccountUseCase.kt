package com.dashlane.settings.main.account

import android.content.Context
import com.dashlane.accountstatus.AccountStatusRepository
import com.dashlane.accountstatus.premiumstatus.isFamilyAdmin
import com.dashlane.accountstatus.premiumstatus.isFamilyPlan
import com.dashlane.accountstatus.premiumstatus.utils.PlansUtils
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.premium.enddate.EndDateFormatter
import com.dashlane.premium.enddate.FormattedEndDateProviderImpl
import com.dashlane.session.SessionManager
import com.dashlane.settings.Account
import com.dashlane.settings.SettingsError
import com.dashlane.settings.SettingsEvent
import com.dashlane.settings.SettingsItemData
import com.dashlane.settings.main.account.SettingsAccountState.View
import com.dashlane.settings.ui.R
import com.dashlane.teamspaces.manager.getCurrentTeam
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.time.Clock


@Suppress("kotlin:S3776", "kotlin:S107", "LongMethod")
internal fun loadAccountSettings(
    @ApplicationContext context: Context,
    sessionManager: SessionManager,
    accountStatusRepository: AccountStatusRepository,
    frozenStateManager: FrozenStateManager,
    endDateFormatter: EndDateFormatter,
    clock: Clock,
    state: View.Initial,
): Flow<SettingsEvent<Account>> {
    return flow {

        val accountSettings = mutableListOf<SettingsItemData>()

        val session = sessionManager.session ?: run {
            emit(SettingsEvent.Error(SettingsError.Generic))
            return@flow
        }

        val accountStatus = accountStatusRepository[session]
        val isInTeam = accountStatus?.getCurrentTeam() != null
        val isFamilyInvitee: Boolean = accountStatus?.premiumStatus?.let { status -> status.isFamilyPlan && !status.isFamilyAdmin } == true

        accountSettings.add(
            SettingsItemData.Click(
                id = Account.Plan,
                title = accountStatus?.premiumStatus
                    ?.let { PlansUtils.getTitle(context, it) }
                    ?: context.getString(R.string.plan_action_bar_title, context.getString(R.string.plan_free_action_bar_title)),
                description = when {
                    frozenStateManager.isAccountFrozen -> context.getString(R.string.frozen_state_subtitle)
                    else -> {
                        accountStatus?.let {
                            endDateFormatter.getLabel(FormattedEndDateProviderImpl(it, clock))
                        } ?: context.getString(R.string.no_date_plan_subtitle)
                    }
                },
                enabled = true,
                visible = !isInTeam && !isFamilyInvitee,
            ),
        )

        accountSettings.add(
            SettingsItemData.Click(
                id = Account.AccountSummary,
                title = context.getString(R.string.account_status_settings_title),
                description = context.getString(R.string.account_status_settings_description),
                enabled = true,
            ),
        )

        emit(SettingsEvent.SettingsLoaded(accountSettings))
    }
}