package com.dashlane.disabletotp.deactivation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dashlane.activatetotp.R
import com.dashlane.activatetotp.databinding.ActivateTotpLoadingBinding
import com.dashlane.activatetotp.setup
import com.dashlane.disabletotp.token.DisableTotpEnterTokenFragment
import com.dashlane.server.api.endpoints.authentication.exceptions.VerificationFailedException
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch

@AndroidEntryPoint
internal class DisableTotpDeactivationFragment : Fragment() {
    private val viewModel by viewModels<DisableTotpDeactivationViewModel>()

    val args: DisableTotpDeactivationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ActivateTotpLoadingBinding.inflate(inflater, container, false)

        binding.setup(messageResId = R.string.disable_totp_deactivation_message_loading)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.sharedFlow
                .flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
                .onStart { viewModel.disableTotp() }
                .collect { state ->
                    when (state) {
                        is DisableTotpDeactivationState.Error -> {
                            when (state.error) {
                                is VerificationFailedException -> {
                                    
                                    val bundleResult = when (args.isBackupCode) {
                                        true -> bundleOf(DisableTotpEnterTokenFragment.KEY_BACKUP_OTP_ERROR to true)
                                        false -> bundleOf(DisableTotpEnterTokenFragment.KEY_OTP_ERROR to true)
                                    }
                                    setFragmentResult(DisableTotpEnterTokenFragment.REQUEST_OTP_VALIDATION, bundleResult)
                                    findNavController().popBackStack()
                                }
                                else -> findNavController().navigate(DisableTotpDeactivationFragmentDirections.goToDeactivationError())
                            }
                        }
                        DisableTotpDeactivationState.Success -> {
                            binding.setup(messageResId = R.string.disable_totp_deactivation_message_complete, isSuccess = true)
                            delay(2000) 
                            requireActivity().finish()
                        }
                    }
                }
        }
        return binding.root
    }
}
