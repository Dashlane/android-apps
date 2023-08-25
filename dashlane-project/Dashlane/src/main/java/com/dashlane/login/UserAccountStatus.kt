package com.dashlane.login

enum class UserAccountStatus {
    YES,

    NO,

    NO_INVALID,

    NO_UNLIKELY,

    YES_OTP_NEWDEVICE,

    YES_OTP_LOGIN,

    ERROR_SERVER,

    ERROR_NETWORK,

    ERROR_PROCESSING;

    fun userExists(): Boolean {
        return this == YES ||
                this == YES_OTP_LOGIN ||
                this == YES_OTP_NEWDEVICE
    }
}