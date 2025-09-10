package com.dashlane.autofill.phishing.cleaner

import com.dashlane.autofill.dagger.PhishdroidFile
import com.dashlane.common.logger.DeveloperLogAction
import com.dashlane.common.logger.DeveloperLogMessage
import com.dashlane.common.logger.Logs
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class AntiPhishingFilesCleaner @Inject constructor(
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    @PhishdroidFile private val phishdroidDir: File,
) {
    suspend fun cleanPhishdroidFiles(): Boolean {
        return withContext(ioDispatcher) {
            if (!phishdroidDir.exists()) {
                return@withContext true
            }
                tag = "[PhishDroid]",
                message = "${phishdroidDir.name} exists, cleaning up content.",
            )
            val deletionResult = phishdroidDir.deleteRecursively()

            logPhishdroidCleanup(deletionResult)
            return@withContext deletionResult
        }
    }

    private fun logPhishdroidCleanup(success: Boolean) {
        
        
            tag = "[PhishDroidCleanup]",
            message = if (success) DeveloperLogMessage.SUCCESS.value else DeveloperLogMessage.FAILURE.value,
            customLogs = listOf(
                Logs.Kibana(
                    action = DeveloperLogAction.PHISH_DROID_CLEANUP,
                    username = null,
                ),
            ),
        )
    }
}