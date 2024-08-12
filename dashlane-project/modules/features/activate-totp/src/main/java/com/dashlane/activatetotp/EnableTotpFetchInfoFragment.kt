package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.whenCreated
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.ActivateTotpLoadingBinding
import com.dashlane.hermes.generated.definitions.TwoFactorAuthenticationError
import com.dashlane.network.tools.authorization
import com.dashlane.server.api.endpoints.authentication.AuthTotpService
import com.dashlane.server.api.endpoints.authentication.exceptions.PhoneValidationFailedException
import com.dashlane.session.SessionManager
import com.dashlane.utils.coroutines.inject.qualifiers.DefaultCoroutineDispatcher
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
internal class EnableTotpFetchInfoFragment : Fragment() {
    @Inject
    internal lateinit var logger: ActivateTotpLogger

    private val viewModel by viewModels<EnableTotpFetchInfoViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val args = EnableTotpFetchInfoFragmentArgs.fromBundle(requireArguments())

        lifecycleScope.launch {
            lifecycle.whenCreated {
                try {
                    val data = viewModel.deferred.await()
                    findNavController().navigate(
                        EnableTotpFetchInfoFragmentDirections.goToDisplayRecoveryCodes(
                            recoveryCodes = data.recoveryKeys.joinToString(separator = "\n"),
                            serverKey = data.serverKey.takeIf { args.totpLogin },
                            otpAuthUrl = data.uri
                        )
                    )
                } catch (_: PhoneValidationFailedException) {
                    setFragmentResult(
                        EnableTotpAddPhoneFragment.REQUEST_PHONE_NUMBER_VALIDATION,
                        bundleOf(EnableTotpAddPhoneFragment.KEY_PHONE_NUMBER_ERROR to true)
                    )
                    logger.logActivationError(TwoFactorAuthenticationError.WRONG_PHONE_FORMAT_ERROR)
                    findNavController().popBackStack()
                } catch (_: Throwable) {
                    logger.logActivationError(TwoFactorAuthenticationError.UNKNOWN_ERROR)
                    findNavController().navigate(EnableTotpFetchInfoFragmentDirections.goToFetchInfoError())
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivateTotpLoadingBinding.inflate(inflater, container, false).apply {
        setup(messageResId = R.string.activate_totp_fetch_info_message)
    }.root
}

@HiltViewModel
internal class EnableTotpFetchInfoViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val sessionManager: SessionManager,
    private val authTotpService: AuthTotpService,
    @DefaultCoroutineDispatcher private val defaultDispatcher: CoroutineDispatcher
) : ViewModel() {
    private val _deferred = CompletableDeferred<AuthTotpService.Data>()
    val deferred: Deferred<AuthTotpService.Data> = _deferred

    init {
        viewModelScope.launch {
            runCatching { fetchInfo() }
                .onSuccess { _deferred.complete(it) }
                .onFailure { _deferred.completeExceptionally(it) }
        }
    }

    private suspend fun fetchInfo() = withContext(defaultDispatcher) {
        val args =
            EnableTotpFetchInfoFragmentArgs.fromSavedStateHandle(savedStateHandle)
        val session = requireNotNull(sessionManager.session)

        authTotpService.execute(
            userAuthorization = session.authorization,
            request = AuthTotpService.Request(
                country = AuthTotpService.Request.Country(args.country),
                phoneNumber = args.phoneNumber
            )
        ).data
    }
}