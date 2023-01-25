package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.ActivateTotpErrorBinding

internal class EnableTotpFetchInfoErrorFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ActivateTotpErrorBinding.inflate(inflater, container, false).apply {
        setup(
            titleResId = R.string.enable_totp_fetch_info_error_title,
            descriptionResId = R.string.enable_totp_fetch_info_error_description,
            positiveButtonResId = R.string.enable_totp_fetch_info_error_cta_positive,
            negativeButtonResId = R.string.enable_totp_fetch_info_error_cta_negative,
            onClickPositiveButton = { findNavController().popBackStack() },
            onClickNegativeButton = { requireActivity().finish() }
        )
    }.root
}