package com.dashlane.m2w

import androidx.lifecycle.SavedStateHandle
import com.dashlane.useractivity.log.usage.UsageLogCode94
import com.dashlane.useractivity.log.usage.UsageLogRepository
import javax.inject.Inject

internal class M2wConnectLoggerImpl @Inject constructor(
    private val usageLogRepository: UsageLogRepository?,
    savedStateHandle: SavedStateHandle
) : M2wConnectLogger {

    private val origin = savedStateHandle.get<String>(M2wIntentCoordinator.EXTRA_ORIGIN)

    override fun logLand() = log(UsageLogCode94.Action.SEE)
    override fun logBack() = log(UsageLogCode94.Action.RETURN)
    override fun logDone() = log(UsageLogCode94.Action.CLIC_DONE)
    override fun logExit() = log(UsageLogCode94.Action.EXIT)
    override fun logConfirmPopupShow() = log(UsageLogCode94.Action.POPUP_DISPLAYED)
    override fun logConfirmPopupYes() = log(UsageLogCode94.Action.POPUP_YES)
    override fun logConfirmPopupNo() = log(UsageLogCode94.Action.POPUP_NO)
    override fun logError() = log(UsageLogCode94.Action.ERROR)

    private fun log(action: UsageLogCode94.Action) {
        usageLogRepository?.enqueue(
            UsageLogCode94(
                originStr = origin,
                screen = UsageLogCode94.Screen.M2W_CONNECT,
                action = action
            )
        )
    }
}