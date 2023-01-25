package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.dashlane.activatetotp.databinding.FragmentEnableTotpCompleteBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
internal class EnableTotpCompleteFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = FragmentEnableTotpCompleteBinding.inflate(inflater, container, false).apply {
        buttonPositive.setOnClickListener {
            requireContext().packageManager?.getLaunchIntentForPackage("com.dashlane.authenticator")
                ?.let { startActivity(it) }
            requireActivity().finish()
        }
        buttonNegative.setOnClickListener { requireActivity().finish() }
    }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_SECURITY_TWO_FACTOR_AUTHENTICATION_ENABLE_SUCCESS_CONFIRMATION)
    }
}