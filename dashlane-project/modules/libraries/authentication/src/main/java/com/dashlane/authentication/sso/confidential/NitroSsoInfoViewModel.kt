package com.dashlane.authentication.sso.confidential

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.nitro.Nitro
import com.dashlane.server.api.NitroApi
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service.Request.DomainName
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service.Request.TeamUuid
import com.dashlane.server.api.endpoints.authentication.RequestLogin2Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NitroSsoInfoViewModel @Inject constructor(private val nitro: Nitro) : ViewModel() {

    private lateinit var nitroApi: NitroApi

    private var _nitroState = MutableStateFlow<NitroState>(NitroState.Default)
    val nitroState: StateFlow<NitroState> = _nitroState.asStateFlow()

    internal fun authenticate(nitroUrl: String, login: String) {
        viewModelScope.launch {
            _nitroState.emit(NitroState.Loading)
            nitroApi = nitro.authenticate(nitroUrl, false)
            _nitroState.emit(
                NitroState.Init(
                    nitroApi.endpoints.authentication.requestLogin2Service.execute(
                        RequestLogin2Service.Request(login = login)
                    ).data
                )
            )
        }
    }

    internal fun onWebviewReady() {
        viewModelScope.launch {
            _nitroState.emit(NitroState.Ready)
        }
    }

    fun confirmLogin(
        teamUuid: String,
        samlResponse: String,
        email: String,
        domainName: String
    ) {
        viewModelScope.launch {
            _nitroState.emit(
                NitroState.Loading
            )

            val confirmLoginResponseData =
                nitroApi.endpoints.authentication.confirmLogin2Service.execute(
                    ConfirmLogin2Service.Request(
                        TeamUuid(teamUuid),
                        samlResponse,
                        DomainName(domainName)
                    )
            ).data

            _nitroState.emit(
                NitroState.LoginResult(
                    GetSsoInfoResult.Success(
                        email,
                        UserSsoInfo(
                            key = confirmLoginResponseData.userServiceProviderKey,
                            login = email,
                            ssoToken = confirmLoginResponseData.ssoToken,
                            exists = confirmLoginResponseData.exists
                        )
                    )
                )
            )
        }
    }

    sealed class NitroState {
        object Default : NitroState()

        data class Init(val data: RequestLogin2Service.Data) : NitroState()

        object Ready : NitroState()

        object Loading : NitroState()

        data class LoginResult(val result: GetSsoInfoResult) : NitroState()
    }
}