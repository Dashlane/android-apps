package com.dashlane.authentication.sso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.api.NitroApi
import com.dashlane.nitro.api.endpoints.ConfirmLogin2Service
import com.dashlane.nitro.api.endpoints.RequestLogin2Service
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class NitroSsoInfoViewModel @Inject constructor(private val nitro: Nitro) : ViewModel() {

    private lateinit var nitroApi: NitroApi

    private var _nitroState = MutableStateFlow<NitroState>(NitroState.Default)
    val nitroState: StateFlow<NitroState> = _nitroState.asStateFlow()

    internal fun authenticate(nitroUrl: String, login: String) {
        viewModelScope.launch {
            _nitroState.emit(NitroState.Loading)
            nitroApi = nitro.authenticate(nitroUrl)
            _nitroState.emit(
                NitroState.Init(
                    nitroApi.endpoints.requestLogin2Service.execute(
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

            val confirmLoginResponseData = nitroApi.endpoints.confirmLogin2Service.execute(
                ConfirmLogin2Service.Request(teamUuid, domainName, samlResponse)
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