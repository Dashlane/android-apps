package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.EnableTotpStepContainerBinding
import com.dashlane.activatetotp.databinding.EnableTotpStepRecoveryInfoContentBinding

internal class EnableTotpRecoveryInfoFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val args = EnableTotpRecoveryInfoFragmentArgs.fromBundle(requireArguments())
        val containerBinding = EnableTotpStepContainerBinding.inflate(inflater, container, false)

        containerBinding.setup(
            stepNumber = 2,
            titleResId = R.string.enable_totp_recovery_info_title,
            descriptionResId = R.string.enable_totp_recovery_info_description,
            positiveButtonResId = R.string.enable_totp_recovery_info_cta,
            onClickPositiveButton = {
                findNavController().navigate(
                    EnableTotpRecoveryInfoFragmentDirections.goToAddPhone(
                        totpLogin = args.totpLogin
                    )
                )
            }
        )

        EnableTotpStepRecoveryInfoContentBinding.inflate(inflater, containerBinding.content, true)

        return containerBinding.root
    }
}