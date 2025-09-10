package com.dashlane.importlogins

import com.dashlane.mvvm.State
import com.dashlane.udf.Event

sealed class ImportLoginsState : State {
    data class View(val showAddLoginManually: Boolean) : ImportLoginsState(), State.View

    sealed class SideEffect : ImportLoginsState(), State.SideEffect {
        data object GoToCsvFileImportIntro : SideEffect()

        data object GoToDashFileImportIntro : SideEffect()

        data object GoToAddSingleLogin : SideEffect()
    }
}

sealed class ImportLoginsEvent : Event {
    data object GoToCsvFileImportClick : ImportLoginsEvent()

    data object GoToDashFileImportClick : ImportLoginsEvent()

    data object GoToAddSingleLoginClick : ImportLoginsEvent()
}