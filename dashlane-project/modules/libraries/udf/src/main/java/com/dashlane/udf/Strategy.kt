package com.dashlane.udf

import com.dashlane.mvvm.State
import kotlin.time.Duration

sealed class Strategy {
    data object ContinueOnNewState : Strategy()

    data object CancelOnNewState : Strategy()

    data object DropOnNewState : Strategy()

    data class Debounce(val time: Duration) : Strategy()
}