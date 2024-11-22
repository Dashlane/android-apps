package com.dashlane.changemasterpassword.warning

import androidx.annotation.StringRes
import com.dashlane.changemasterpassword.R
import com.dashlane.mvvm.State

sealed class ChangeMasterPasswordWarningState : State {
    data class View(
        @StringRes val title: Int? = null,
        @StringRes val description: Int? = null,
        @StringRes val infoBox: Int = R.string.login_account_recovery_key_change_mp_warning,
        @StringRes val primaryButtonText: Int = R.string.change_mp_warning_desktop_positive_button,
        @StringRes val secondaryButtonText: Int = R.string.change_mp_warning_desktop_negative_button,
    ) : ChangeMasterPasswordWarningState(), State.View

    sealed class SideEffect : ChangeMasterPasswordWarningState(), State.SideEffect {
        data object Cancel : SideEffect()
        data object GoToChangeMP : SideEffect()
    }
}
