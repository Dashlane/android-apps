package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.ActivateTotpErrorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EnableTotpActivationNoConnectionFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivateTotpErrorBinding.inflate(inflater, container, false).apply {
        setup(
            titleResId = R.string.enable_totp_activation_no_connection_title,
            descriptionResId = R.string.enable_totp_activation_no_connection_description,
            positiveButtonResId = R.string.enable_totp_activation_no_connection_cta_positive,
            onClickPositiveButton = { findNavController().popBackStack() },
            negativeButtonResId = R.string.enable_totp_activation_no_connection_cta_negative,
            onClickNegativeButton = { requireActivity().finish() }
        )
    }.root
}
