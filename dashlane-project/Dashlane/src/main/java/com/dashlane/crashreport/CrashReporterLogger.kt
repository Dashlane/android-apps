package com.dashlane.crashreport

import com.dashlane.useractivity.log.install.InstallLogRepository
import com.dashlane.useractivity.log.install.InstallLogCode17
import com.dashlane.usersupportreporter.UserSupportFileLogger
import javax.inject.Inject



class CrashReporterLogger @Inject constructor(
    private val installLogRepository: InstallLogRepository,
    private val userSupportFileLogger: UserSupportFileLogger
) {

    fun onCrashHappened() {
        userSupportFileLogger.add("Crash java")
        installLogRepository.enqueue(InstallLogCode17(type = "java", subStep = "70"))
    }
}