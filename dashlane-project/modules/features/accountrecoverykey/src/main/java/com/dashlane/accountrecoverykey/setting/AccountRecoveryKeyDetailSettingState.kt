package com.dashlane.accountrecoverykey.setting

import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

sealed class AccountRecoveryKeyDetailSettingState : State {
    data class View(
        val enabled: Boolean = false,
        val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
        val accountRecoveryKey: String? = null,
        val isLoading: Boolean = false,
        val isDialogDisplayed: Boolean = false,
    ) : AccountRecoveryKeyDetailSettingState(), State.View

    sealed class SideEffect : AccountRecoveryKeyDetailSettingState(), State.SideEffect {
        data object GoToIntro : SideEffect()
    }
}
