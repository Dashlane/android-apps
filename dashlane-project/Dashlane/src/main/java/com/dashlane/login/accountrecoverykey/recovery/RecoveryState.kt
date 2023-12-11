package com.dashlane.login.accountrecoverykey.recovery

import com.dashlane.login.LoginStrategy

sealed class RecoveryState {
    abstract val progress: Int

    data class Initial(override val progress: Int) : RecoveryState()
    data class Loading(override val progress: Int) : RecoveryState()
    data class Finish(override val progress: Int, val strategy: LoginStrategy.Strategy?) : RecoveryState()
    data class Error(override val progress: Int) : RecoveryState()
}