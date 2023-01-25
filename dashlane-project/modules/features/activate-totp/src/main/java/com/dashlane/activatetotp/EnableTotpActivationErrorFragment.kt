package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.activatetotp.databinding.ActivateTotpErrorBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EnableTotpActivationErrorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivateTotpErrorBinding.inflate(inflater, container, false).apply {
        setup(
            titleResId = R.string.enable_totp_activation_error_title,
            descriptionResId = R.string.enable_totp_activation_error_description,
            positiveButtonResId = R.string.enable_totp_activation_error_cta,
            onClickPositiveButton = { requireActivity().finish() }
        )
    }.root
}
