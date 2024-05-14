package com.dashlane.login.pages.secrettransfer.choosetype

sealed class ChooseTypeState {
    abstract val data: ChooseTypeData

    data class Initial(override val data: ChooseTypeData) : ChooseTypeState()
    data class GoToUniversal(override val data: ChooseTypeData) : ChooseTypeState()
    data class GoToQR(override val data: ChooseTypeData) : ChooseTypeState()
    data class GoToHelp(override val data: ChooseTypeData) : ChooseTypeState()
}

data class ChooseTypeData(
    val email: String? = null
)
