package com.dashlane.user

private const val MASTER_PASSWORD = "master_password"
private const val INVISIBLE_MASTER_PASSWORD = "invisible_master_password"

data class UserAccountInfo(
    val username: String,
    val otp2: Boolean,
    val securitySettings: UserSecuritySettings? = null,
    val accessKey: String,
    val accountType: AccountType
) {
    val sso: Boolean get() = securitySettings?.isSso == true

    sealed class AccountType {

        object MasterPassword : AccountType()
        object InvisibleMasterPassword : AccountType()

        companion object {
            fun fromString(string: String?): AccountType {
                return when (string) {
                    MASTER_PASSWORD -> MasterPassword
                    INVISIBLE_MASTER_PASSWORD -> InvisibleMasterPassword
                    else -> throw IllegalStateException()
                }
            }
        }

        override fun toString(): String {
            return when (this) {
                is MasterPassword -> MASTER_PASSWORD
                is InvisibleMasterPassword -> INVISIBLE_MASTER_PASSWORD
            }
        }
    }
}
