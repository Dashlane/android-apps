package com.dashlane.authentication.sso

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.api.NitroApi
import com.dashlane.nitro.api.endpoints.ConfirmLoginService
import com.dashlane.nitro.api.endpoints.RequestLoginService
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

    internal fun authenticate(nitroUrl: String, domainName: String) {
        viewModelScope.launch {
            _nitroState.emit(NitroState.Loading)
            nitroApi = nitro.authenticate(nitroUrl)
            _nitroState.emit(
                NitroState.Init(
                    nitroApi.endpoints.requestLoginService.execute(
                        RequestLoginService.Request(domainName = domainName)
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
        samlResponse: String,
        email: String,
        domainName: String
    ) {
        viewModelScope.launch {
            _nitroState.emit(
                NitroState.Loading
            )

            val confirmLoginResponseData = nitroApi.endpoints.confirmLoginService.execute(
                ConfirmLoginService.Request(domainName, samlResponse)
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

        data class Init(val data: RequestLoginService.Data) : NitroState()

        object Ready : NitroState()

        object Loading : NitroState()

        data class LoginResult(val result: GetSsoInfoResult) : NitroState()
    }
}