package com.dashlane.mvvm

interface State {
    interface View : State
    interface SideEffect : State
}
