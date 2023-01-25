package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import com.dashlane.account.UserAccountStorage
import com.dashlane.account.UserSecuritySettings
import com.dashlane.activatetotp.databinding.ActivateTotpLoadingBinding
import com.dashlane.authenticator.UriParser
import com.dashlane.hermes.generated.definitions.TwoFactorAuthenticationError
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.AuthTotpActivationService
import com.dashlane.server.api.endpoints.authentication.AuthVerificationTotpService
import com.dashlane.server.api.exceptions.DashlaneApiIoException
import com.dashlane.session.SessionManager
import com.dashlane.ui.screens.settings.Use2faSettingStateRefresher
import com.dashlane.util.inject.qualifiers.DefaultCoroutineDispatcher
import com.dashlane.util.tryOrNull
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
internal class EnableTotpActivationFragment : Fragment() {
    @Inject
    lateinit var logger: ActivateTotpLogger

    private val viewModel by viewModels<EnableTotpActivationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        lifecycleScope.launch {
            lifecycle.whenCreated {
                findNavController().navigate(
                    try {
                        viewModel.deferred.await()
                        logger.logActivationComplete()
                        delay(2_000) 
                        EnableTotpActivationFragmentDirections.goToComplete()
                    } catch (_: DashlaneApiIoException) {
                        logger.logActivationError(TwoFactorAuthenticationError.USER_OFFLINE_ERROR)
                        EnableTotpActivationFragmentDirections.goToActivationNoConnection()
                    } catch (_: Throwable) {
                        logger.logActivationError(TwoFactorAuthenticationError.UNKNOWN_ERROR)
                        EnableTotpActivationFragmentDirections.goToActivationError()
                    }
                )
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivateTotpLoadingBinding.inflate(inflater, container, false).apply {
        setup(messageResId = R.string.enable_totp_activation_message_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.lifecycle.whenCreated {
                runCatching {
                    viewModel.deferred.await()
                    setup(
                        messageResId = R.string.enable_totp_activation_message_complete,
                        isSuccess = true
                    )
                }
            }
        }
    }.root
}

@HiltViewModel
internal class EnableTotpActivationViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val authVerificationTotpService: AuthVerificationTotpService,
    private val authTotpActivationService: AuthTotpActivationService,
    private val userAccountStorage: UserAccountStorage,
    private val use2faSettingStateRefresher: Use2faSettingStateRefresher,
    private val activateTotpServerKeyChanger: ActivateTotpServerKeyChanger,
    private val activateTotpAuthenticatorConnection: ActivateTotpAuthenticatorConnection,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _deferred = CompletableDeferred<Unit>()
    val deferred: Deferred<Unit> = _deferred

    init {
        viewModelScope.launch {
            runCatching { enableTotp() }
                .onSuccess { _deferred.complete(Unit) }
                .onFailure { _deferred.completeExceptionally(it) }
        }
    }

    private suspend fun enableTotp() = withContext(defaultDispatcher) {
        val session = requireNotNull(sessionManager.session) { "session == null" }

        val args = EnableTotpActivationFragmentArgs.fromSavedStateHandle(savedStateHandle)

        val otp = requireNotNull(UriParser.parse(args.otpAuthUrl.toUri())?.getPin()?.code) {
            "invalid otp"
        }

        val tokenSaved = tryOrNull {
            activateTotpAuthenticatorConnection.saveDashlaneTokenAsync(
                session.userId,
                args.otpAuthUrl
            ).await()
        } ?: false

        if (!tokenSaved) throw Exception("Cannot save token")

        val authTicket = authVerificationTotpService.execute(
            request = AuthVerificationTotpService.Request(
                login = session.userId,
                otp = otp,
                activationFlow = true
            )
        ).data.authTicket

        val serverKey = args.serverKey

        if (serverKey == null) {
            
            authTotpActivationService.execute(
                userAuthorization = session.authorization,
                request = AuthTotpActivationService.Request(
                    authTicket = AuthTotpActivationService.Request.AuthTicket(authTicket)
                )
            )

            userAccountStorage.saveSecuritySettings(
                session.username,
                UserSecuritySettings(isTotp = true)
            )
        } else {
            
            activateTotpServerKeyChanger.updateServerKey(
                newServerKey = serverKey,
                authTicket = authTicket
            )
        }

        runCatching { use2faSettingStateRefresher.refresh() }
    }
}