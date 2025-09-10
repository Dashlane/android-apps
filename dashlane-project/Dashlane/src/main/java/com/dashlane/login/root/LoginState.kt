package com.dashlane.login.root

import com.dashlane.data.login.MigrationToSsoMemberInfo
import com.dashlane.data.login.RegisteredUserDevice
import com.dashlane.lock.LockSetting
import com.dashlane.login.LocalLoginDestination
import com.dashlane.login.LoginStrategy
import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

sealed class LoginState : State {
    sealed class View : LoginState(), State.View {
        abstract val lockSetting: LockSetting
        abstract val leaveAfterSuccess: Boolean

        data class Initial(
            override val lockSetting: LockSetting,
            override val leaveAfterSuccess: Boolean,
            val allowSkipEmail: Boolean,
        ) : View()

        data class Remote(
            override val lockSetting: LockSetting,
            override val leaveAfterSuccess: Boolean,
            val allowSkipEmail: Boolean,
            val email: String?,
        ) : View()

        data class Local(
            override val lockSetting: LockSetting,
            override val leaveAfterSuccess: Boolean,
            val userAccountInfo: UserAccountInfo,
            val startDestination: LocalLoginDestination,
        ) : View()
    }

    sealed class SideEffect : LoginState(), State.SideEffect {
        data class ShowSnackBar(val message: String) : SideEffect()

        sealed class RemoteSuccess : SideEffect() {
            data class Finish(
                val email: String?,
                val strategy: LoginStrategy.Strategy?,
                val lockSetting: LockSetting,
                val migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
                val ukiRequiresMonobucketConfirmation: Boolean,
            ) : RemoteSuccess()

            data class CreateAccount(val email: String, val skipEmailIfPrefilled: Boolean) : RemoteSuccess()

            data object EndOfLife : RemoteSuccess()

            data object FinishWithSuccess : RemoteSuccess()
        }

        sealed class LocalSuccess : SideEffect() {
            data class Success(
                val email: String?,
                val strategy: LoginStrategy.Strategy?,
                val lockSetting: LockSetting,
                val migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
                val ukiRequiresMonobucketConfirmation: Boolean,
            ) : LocalSuccess()

            data class Cancel(val lockSetting: LockSetting) : LocalSuccess()
        }

        data class GoToARK(
            val registeredUserDevice: RegisteredUserDevice,
            val accountType: UserAccountInfo.AccountType,
            val authTicket: String?,
        ) : SideEffect()
    }
}