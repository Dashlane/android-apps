package com.dashlane.accountrecoverykey.activation.intro

import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

data class AccountRecoveryKeyActivationIntroState(
    val accountType: UserAccountInfo.AccountType = UserAccountInfo.AccountType.MasterPassword,
    val showSkipAlertDialog: Boolean = false,
) : State.View
