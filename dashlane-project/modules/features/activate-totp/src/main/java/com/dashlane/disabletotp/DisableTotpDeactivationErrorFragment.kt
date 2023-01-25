package com.dashlane.disabletotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.activatetotp.R
import com.dashlane.activatetotp.databinding.ActivateTotpErrorBinding
import com.dashlane.activatetotp.setup

internal class DisableTotpDeactivationErrorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = ActivateTotpErrorBinding.inflate(inflater, container, false).apply {
        setup(
            titleResId = R.string.disable_totp_error_title,
            descriptionResId = R.string.disable_totp_error_description,
            positiveButtonResId = R.string.disable_totp_error_cta,
            onClickPositiveButton = { requireActivity().finish() }
        )
    }.root
}
