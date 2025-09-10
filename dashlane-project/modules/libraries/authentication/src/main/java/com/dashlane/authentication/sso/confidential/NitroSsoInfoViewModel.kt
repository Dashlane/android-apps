package com.dashlane.authentication.sso.confidential

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.authentication.sso.GetSsoInfoResult
import com.dashlane.authentication.sso.utils.UserSsoInfo
import com.dashlane.nitro.Nitro
import com.dashlane.nitro.hilt.NitroLegacy
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service.Request.DomainName
import com.dashlane.server.api.endpoints.authentication.ConfirmLogin2Service.Request.TeamUuid
import com.dashlane.server.api.endpoints.authentication.RequestLogin2Service
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NitroSsoInfoViewModel @Inject constructor(
    @NitroLegacy private val nitroLegacy: Nitro,
) : ViewModel() {
    private var _nitroState = MutableStateFlow<NitroState>(NitroState.Default)
    val nitroState: StateFlow<NitroState> = _nitroState.asStateFlow()

    internal fun authenticate(nitroHost: String, login: String) {
        flow {
            emit(NitroState.Loading)
            nitroLegacy.authenticate(newNitroHost = nitroHost)
            val request = RequestLogin2Service.Request(login = login)
            val response = nitroLegacy.execute<RequestLogin2Service.Data> {
                endpoints.authentication.requestLogin2Service.execute(request)
            }
            emit(NitroState.Init(response.data))
        }
            .catch {
                emit(NitroState.LoginResult(GetSsoInfoResult.Error.Unknown))
            }
            .onEach { _nitroState.emit(it) }
            .launchIn(viewModelScope)
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
        domainName: String,
    ) {
        viewModelScope.launch {
            _nitroState.emit(NitroState.Loading)

            val confirmLoginResponseData = nitroLegacy.execute<ConfirmLogin2Service.Data> {
                endpoints.authentication.confirmLogin2Service.execute(
                    ConfirmLogin2Service.Request(
                        TeamUuid(teamUuid),
                        samlResponse,
                        DomainName(domainName),
                    ),
                )
            }.data

            _nitroState.emit(
                NitroState.LoginResult(
                    GetSsoInfoResult.Success(
                        email,
                        UserSsoInfo(
                            key = confirmLoginResponseData.userServiceProviderKey,
                            login = email,
                            ssoToken = confirmLoginResponseData.ssoToken,
                            exists = confirmLoginResponseData.exists,
                        ),
                    ),
                ),
            )
        }
    }

    sealed class NitroState {
        data object Default : NitroState()

        data class Init(val data: RequestLogin2Service.Data) : NitroState()

        data object Ready : NitroState()

        data object Loading : NitroState()

        data class LoginResult(val result: GetSsoInfoResult) : NitroState()
    }
}