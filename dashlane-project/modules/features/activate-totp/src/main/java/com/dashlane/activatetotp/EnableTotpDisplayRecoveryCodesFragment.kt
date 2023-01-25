package com.dashlane.activatetotp

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.EnableTotpStepContainerBinding
import com.dashlane.activatetotp.databinding.EnableTotpStepDisplayRecoveryCodesContentBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.setCurrentPageView

internal class EnableTotpDisplayRecoveryCodesFragment : Fragment() {
    private var showConfirmationDialog = false

    private val confirmationDialog by lazy(LazyThreadSafetyMode.NONE) {
        DialogHelper().builder(requireContext(), R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
            .setTitle(R.string.enable_totp_display_recovery_codes_dialog_title)
            .setMessage(R.string.enable_totp_display_recovery_codes_dialog_message)
            .setPositiveButton(R.string.enable_totp_display_recovery_codes_dialog_cta_positive) { _, _ -> navigateToActivation() }
            .setNegativeButton(R.string.enable_totp_display_recovery_codes_dialog_cta_negative) { _, _ ->
            }
            .setCancelable(false)
            .setOnDismissListener { showConfirmationDialog = false }
            .create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showConfirmationDialog =
            savedInstanceState?.getBoolean(STATE_SHOW_CONFIRMATION_DIALOG, false)
                ?: false
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = EnableTotpDisplayRecoveryCodesFragmentArgs.fromBundle(requireArguments())
        val containerBinding = EnableTotpStepContainerBinding.inflate(inflater, container, false)

        containerBinding.setup(
            stepNumber = 3,
            titleResId = R.string.enable_totp_display_recovery_codes_title,
            positiveButtonResId = R.string.enable_totp_display_recovery_codes_cta_positive,
            negativeButtonResId = R.string.enable_totp_display_recovery_codes_cta_negative,
            onClickPositiveButton = {
                showConfirmationDialog = true
                val sendIntent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, args.recoveryCodes)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null)
                startActivity(shareIntent)
            },
            onClickNegativeButton = {
                navigateToActivation()
            }
        )

        val contentBinding = EnableTotpStepDisplayRecoveryCodesContentBinding.inflate(
            inflater,
            containerBinding.content,
            true
        )

        contentBinding.recoveryCodes.apply {
            text = args.recoveryCodes
            
            setOnLongClickListener {
                showConfirmationDialog = true
                false
            }
        }

        return containerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_SECURITY_TWO_FACTOR_AUTHENTICATION_ENABLE_BACKUP_CODES)
    }

    override fun onResume() {
        super.onResume()
        if (showConfirmationDialog) {
            confirmationDialog.show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(STATE_SHOW_CONFIRMATION_DIALOG, showConfirmationDialog)
    }

    private fun navigateToActivation() {
        val navController = findNavController()
        val args = EnableTotpDisplayRecoveryCodesFragmentArgs.fromBundle(requireArguments())

        navController.navigate(
            EnableTotpDisplayRecoveryCodesFragmentDirections.goToActivation(
                serverKey = args.serverKey,
                otpAuthUrl = args.otpAuthUrl
            )
        )
    }

    companion object {
        private const val STATE_SHOW_CONFIRMATION_DIALOG = "showConfirmationDialog"
    }
}