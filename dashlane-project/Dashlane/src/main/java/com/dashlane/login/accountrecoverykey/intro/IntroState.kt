package com.dashlane.login.accountrecoverykey.intro

sealed class IntroState {
    object Initial : IntroState()

    object Loading : IntroState()
    object GoToToken : IntroState()
    object GoToTOTP : IntroState()
    data class GoToARK(val authTicket: String) : IntroState()
    object Error : IntroState()
}