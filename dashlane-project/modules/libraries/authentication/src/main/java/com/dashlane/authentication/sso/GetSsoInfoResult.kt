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
        data object Unknown : Error()

        @Parcelize
        data object CannotOpenServiceProvider : Error()

        @Parcelize
        data object UnauthorizedNavigation : Error()

        @Parcelize
        data object SamlResponseNotFound : Error()
    }
}
