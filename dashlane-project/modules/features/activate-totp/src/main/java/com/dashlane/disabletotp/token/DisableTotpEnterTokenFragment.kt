package com.dashlane.disabletotp.token

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResultListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.R
import com.dashlane.activatetotp.databinding.FragmentDisableTotpEnterTokenBinding
import com.dashlane.disabletotp.deactivation.DisableTotpDeactivationFragmentDirections
import com.dashlane.login.CodeInputViewHelper
import com.dashlane.ui.util.DialogHelper
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class DisableTotpEnterTokenFragment : Fragment() {

    private val viewModel by viewModels<TotpRecoveryCodeDialogViewModel>()
    private var dialogBuilder: TotpRecoveryCodeAlertDialogBuilder? = null
    private var tokenWidth = 0
    private var _binding: FragmentDisableTotpEnterTokenBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDisableTotpEnterTokenBinding.inflate(inflater, container, false)

        val minContentHeight = requireContext().resources.getDimensionPixelSize(R.dimen.size_480dp)

        
        binding.root.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            val height = bottom - top
            val hasEnoughHeight = height > minContentHeight
            binding.root.post { binding.space.isVisible = hasEnoughHeight }
        }

        val token = binding.token
        val buttonPositive = binding.buttonPositive

        CodeInputViewHelper.initialize(token, savedInstanceState?.getInt(STATE_TOKEN_WIDTH) ?: tokenWidth)

        token.doAfterTextChanged { text ->
            if (text.isNullOrEmpty()) return@doAfterTextChanged
            buttonPositive.isEnabled = text.length == 6
            binding.error.isVisible = false
        }

        token.setOnEditorActionListener { _, id, _ ->
            if (id == EditorInfo.IME_ACTION_NEXT) {
                buttonPositive.performClick()
            } else {
                false
            }
        }

        token.requestFocus()

        binding.link.setOnClickListener { show2FARecoveryCodeDialog() }

        buttonPositive.setOnClickListener {
            if (token.text?.length == 6) {
                next(otp = token.text.toString(), isBackupCode = false)
            }
        }

        setFragmentResultListener(REQUEST_OTP_VALIDATION) { _, bundle ->
            binding.error.isInvisible = !bundle.getBoolean(KEY_OTP_ERROR, false)
            if (bundle.getBoolean(KEY_BACKUP_OTP_ERROR, false)) {
                dialogBuilder?.apply {
                    getDialog()?.show()
                    setIsError(isError = true, errorMessage = getString(R.string.disable_totp_enter_token_recovery_dialog_backup_error))
                }
            }
        }

        lifecycleScope.launchWhenStarted {
            viewModel.sharedFlow.collect { state ->
                when (state) {
                    SmsRecoveryCodeDialogState.Error -> {
                        findNavController().navigate(DisableTotpDeactivationFragmentDirections.goToDeactivationError())
                    }
                    SmsRecoveryCodeDialogState.Success -> {
                        show2FARecoveryEnterCodeDialog(requireContext().getString(R.string.disable_totp_enter_token_recovery_dialog_phone_backup_description))
                    }
                }
            }
        }
        return binding.root
    }

    override fun onDestroyView() {
        tokenWidth = binding.token.width
        super.onDestroyView()
        _binding = null
    }

    override fun onDestroy() {
        dialogBuilder = null
        super.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        view?.let { FragmentDisableTotpEnterTokenBinding.bind(it) }
            ?.let { outState.putInt(STATE_TOKEN_WIDTH, it.token.width) }
    }

    private fun next(otp: String, isBackupCode: Boolean) {
        dialogBuilder?.getDialog()?.hide()
        findNavController()
            .navigate(DisableTotpEnterTokenFragmentDirections.goToDeactivation(otp, isBackupCode))
    }

    private fun show2FARecoveryCodeDialog() {
        DialogHelper()
            .builder(requireContext(), R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(R.string.disable_totp_enter_token_recovery_dialog_title)
            .setMessage(R.string.disable_totp_enter_token_recovery_dialog_choice_description)
            .setPositiveButton(R.string.disable_totp_enter_token_recovery_dialog_choice_button_positive) { _, _ ->
                show2FARecoveryEnterCodeDialog(requireContext().getString(R.string.disable_totp_enter_token_recovery_dialog_backup_description))
            }
            .setNegativeButton(R.string.disable_totp_enter_token_recovery_dialog_choice_button_negative) { _, _ ->
                show2FARecoveryPhoneCodeDialog()
            }
            .setCancelable(true)
            .show()
    }

    private fun show2FARecoveryPhoneCodeDialog() {
        DialogHelper()
            .builder(requireContext(), R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(R.string.disable_totp_enter_token_recovery_dialog_title)
            .setMessage(R.string.disable_totp_enter_token_recovery_dialog_phone_description)
            .setPositiveButton(R.string.disable_totp_enter_token_recovery_dialog_phone_button_positive) { _, _ -> viewModel.sendRecoveryBySms() }
            .setNegativeButton(R.string.disable_totp_enter_token_recovery_dialog_phone_button_negative) { _, _ -> }
            .setCancelable(true)
            .show()
    }

    private fun show2FARecoveryEnterCodeDialog(message: String) {
        dialogBuilder = TotpRecoveryCodeAlertDialogBuilder(requireActivity()).apply {
            create(
                title = getString(R.string.disable_totp_enter_token_recovery_dialog_title),
                message = message,
                hint = getString(R.string.disable_totp_enter_token_recovery_dialog_backup_hint),
                positiveText = getString(R.string.disable_totp_enter_token_recovery_dialog_backup_button_positive),
                positiveAction = { recoveryCode: String -> next(otp = recoveryCode, isBackupCode = true) },
                negativeText = getString(R.string.disable_totp_enter_token_recovery_dialog_backup_button_negative),
                negativeAction = { },
            )
        }
        dialogBuilder?.getDialog()?.apply {
            setOnDismissListener { binding.token.requestFocus() }
            show()
        }
    }

    companion object {
        private const val STATE_TOKEN_WIDTH = "token_width"
        const val REQUEST_OTP_VALIDATION = "otp_validation"
        const val KEY_OTP_ERROR = "otp_error"
        const val KEY_BACKUP_OTP_ERROR = "backup_otp_error"
    }
}
