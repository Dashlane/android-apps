package com.dashlane.login.root

import com.dashlane.authentication.RegisteredUserDevice
import com.dashlane.lock.LockEvent
import com.dashlane.lock.LockSetting
import com.dashlane.login.LoginStrategy
import com.dashlane.login.sso.MigrationToSsoMemberInfo
import com.dashlane.mvvm.State
import com.dashlane.user.UserAccountInfo

sealed class LoginState : State {

    sealed class View : LoginState(), State.View {
        abstract val lockSetting: LockSetting?
        abstract val leaveAfterSuccess: Boolean?

        data class Initial(
            override val lockSetting: LockSetting? = null,
            override val leaveAfterSuccess: Boolean? = null,
        ) : View()

        data class Remote(
            override val lockSetting: LockSetting,
            override val leaveAfterSuccess: Boolean,
            val email: String?,
            val allowSkipEmail: Boolean,
        ) : View()

        data class Local(
            override val lockSetting: LockSetting,
            override val leaveAfterSuccess: Boolean,
            val registeredUserDevice: RegisteredUserDevice,
            val userAccountInfo: UserAccountInfo,
            val startDestination: LocalLoginDestination,
        ) : View()
    }

    sealed class SideEffect : LoginState(), State.SideEffect {
        sealed class RemoteSuccess : SideEffect() {
            data class Finish(
                val strategy: LoginStrategy.Strategy?,
                val lockSetting: LockSetting?,
                val migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
                val ukiRequiresMonobucketConfirmation: Boolean,
            ) : RemoteSuccess()

            data class CreateAccount(val email: String, val skipEmailIfPrefilled: Boolean) : RemoteSuccess()
            data object EndOfLife : RemoteSuccess()
            data class FinishWithSuccess(val unlockReason: LockEvent.Unlock.Reason?) : RemoteSuccess()
        }

        sealed class LocalSuccess : SideEffect() {
            data class Success(
                val strategy: LoginStrategy.Strategy?,
                val lockSetting: LockSetting?,
                val migrationToSsoMemberInfo: MigrationToSsoMemberInfo?,
                val ukiRequiresMonobucketConfirmation: Boolean,
            ) : LocalSuccess()

            data class ChangeAccount(val email: String?, val lockSetting: LockSetting?) : LocalSuccess()
            data class Cancel(val lockSetting: LockSetting?) : LocalSuccess()
            data class Logout(val email: String?, val isMPLess: Boolean) : LocalSuccess()
        }
    }
}