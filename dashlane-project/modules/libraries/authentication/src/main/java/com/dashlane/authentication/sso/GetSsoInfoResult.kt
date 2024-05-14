package com.dashlane.authentication.sso

import android.os.Parcelable
import com.dashlane.authentication.sso.utils.UserSsoInfo
import kotlinx.parcelize.Parcelize

sealed class GetSsoInfoResult : Parcelable {
    @Parcelize
    data class Success(
        val login: String,
        val userSsoInfo: UserSsoInfo
    ) : GetSsoInfoResult()

    sealed class Error : GetSsoInfoResult() {
        @Parcelize
        object CannotOpenServiceProvider : Error()

        @Parcelize
        object UnauthorizedNavigation : Error()

        @Parcelize
        object SamlResponseNotFound : Error()
    }
}
