package com.dashlane.util.installlogs

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode54



class DataLossTrackingLogger(private val installLogRepository: InstallLogRepository) {

    enum class Reason(val code: Int) {
        CREATE_ACCOUNT_RESET(1),
        PASSWORD_RESET_BROADCAST(2),
        PASSWORD_OK_UKI_INVALID(3),
        PASSWORD_CHANGED(4),
        USER_DATA_OBSOLETE_OTHER(5),
        USER_DATA_OBSOLETE_YES_OTP_LOGIN(6),
        ACCESS_KEY_UNKNOWN(7)
    }

    fun log(reason: Reason) {
        installLogRepository.enqueue(InstallLogCode54(subStep = reason.code.toString()))
    }
}