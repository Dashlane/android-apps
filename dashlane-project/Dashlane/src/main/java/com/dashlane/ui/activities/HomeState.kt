package com.dashlane.ui.activities

import com.dashlane.mvvm.State
import com.google.android.play.core.appupdate.AppUpdateInfo

sealed class HomeState : State {
    sealed class View : HomeState(), State.View {
        data object Initial : View()
    }

    sealed class SideEffect : HomeState(), State.SideEffect {
        data class InstallUpdate(val appUpdateInfo: AppUpdateInfo) : SideEffect()
    }
}