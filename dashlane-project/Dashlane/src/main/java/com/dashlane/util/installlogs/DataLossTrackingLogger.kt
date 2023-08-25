package com.dashlane.util.installlogs

class DataLossTrackingLogger {

    enum class Reason(val code: Int) {
        CREATE_ACCOUNT_RESET(1),
        PASSWORD_RESET_BROADCAST(2),
        PASSWORD_OK_UKI_INVALID(3),
        PASSWORD_CHANGED(4),
        USER_DATA_OBSOLETE_OTHER(5),
        USER_DATA_OBSOLETE_YES_OTP_LOGIN(6),
        ACCESS_KEY_UNKNOWN(7)
    }
}