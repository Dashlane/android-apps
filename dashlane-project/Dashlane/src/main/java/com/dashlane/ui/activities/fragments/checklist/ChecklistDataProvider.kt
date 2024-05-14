package com.dashlane.ui.activities.fragments.checklist

import com.dashlane.R
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.Companion.KEY_GUIDED_ONBOARDING_DWM_OPT_IN
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.Companion.KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER
import com.dashlane.guidedonboarding.widgets.QuestionnaireAnswer.Companion.fromId
import com.dashlane.inapplogin.InAppLoginManager
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.storage.userdata.accessor.DataCounter
import com.dashlane.storage.userdata.accessor.filter.CounterFilter
import com.dashlane.storage.userdata.accessor.filter.CredentialFilter
import com.dashlane.storage.userdata.accessor.filter.lock.DefaultLockFilter
import com.dashlane.storage.userdata.accessor.filter.space.NoSpaceFilter
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.Companion.KEY_CHECKLIST_ADD_CREDENTIAL_ACKNOWLEDGED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.Companion.KEY_CHECKLIST_AUTOFILL_ACTIVATION_ACKNOWLEDGED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.Companion.KEY_CHECKLIST_DWM_ACKNOWLEDGED
import com.dashlane.ui.activities.fragments.checklist.ChecklistData.Companion.KEY_CHECKLIST_M2D_ACKNOWLEDGED
import java.time.Instant
import javax.inject.Inject

class ChecklistDataProvider @Inject constructor(
    private val userPreferencesManager: UserPreferencesManager,
    private val inAppLoginManager: InAppLoginManager,
    private val dataCounter: DataCounter,
    private val checklistHelper: ChecklistHelper
) : ChecklistDataProviderContract {

    override fun hasDarkWebMonitoring() =
        userPreferencesManager.getBoolean(KEY_GUIDED_ONBOARDING_DWM_OPT_IN, false)

    override fun hasActivatedAutofill() = inAppLoginManager.isEnableForApp()

    override fun hasAddedAccounts(): Boolean {
        val count: Int = dataCounter.count(
            CounterFilter(
                CredentialFilter(),
                NoSpaceFilter,
                DefaultLockFilter
            )
        )
        return count > 0
    }

    override fun hasFinishedM2D() = userPreferencesManager.hasFinishedM2D

    override fun hasCompletedAndAcknowledgedAddCredential() =
        userPreferencesManager.getBoolean(KEY_CHECKLIST_ADD_CREDENTIAL_ACKNOWLEDGED, false)

    override fun hasCompletedAndAcknowledgedAutofillActivation() =
        userPreferencesManager.getBoolean(KEY_CHECKLIST_AUTOFILL_ACTIVATION_ACKNOWLEDGED, false)

    override fun hasCompletedAndAcknowledgedM2D() =
        userPreferencesManager.getBoolean(KEY_CHECKLIST_M2D_ACKNOWLEDGED, false)

    override fun hasCompletedAndAcknowledgedDarkWebAlerts() =
        userPreferencesManager.getBoolean(KEY_CHECKLIST_DWM_ACKNOWLEDGED, false)

    override fun saveAddCredentialCompletedAndAcknowledged() =
        userPreferencesManager.putBoolean(KEY_CHECKLIST_ADD_CREDENTIAL_ACKNOWLEDGED, true)

    override fun saveAutofillActiationCompletedAndAcknowledgedAutofill() =
        userPreferencesManager.putBoolean(KEY_CHECKLIST_AUTOFILL_ACTIVATION_ACKNOWLEDGED, true)

    override fun saveM2DCompletedAndAcknowledged() =
        userPreferencesManager.putBoolean(KEY_CHECKLIST_M2D_ACKNOWLEDGED, true)

    override fun saveDarkWebMonitoringCompletedAndAcknowledged() =
        userPreferencesManager.putBoolean(KEY_CHECKLIST_DWM_ACKNOWLEDGED, true)

    override fun getGuidedOnBoardingAction(): Int {
        val answer = fromId(
            userPreferencesManager.getInt(
                KEY_GUIDED_PASSWORD_ONBOARDING_Q2_ANSWER,
                -1
            )
        )
        return answer?.action ?: R.string.get_started_step_secure_vault_title
    }

    override fun hasSeenChecklist(): Boolean = checklistHelper.hasSeenChecklist()

    override fun setChecklistSeen() {
        checklistHelper.setChecklistSeen()
    }

    override fun setChecklistDismissed() {
        checklistHelper.setChecklistDismissed()
    }

    override fun getAccountCreationDate(): Instant {
        return userPreferencesManager.accountCreationDate
    }
}
