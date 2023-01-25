package com.dashlane.activatetotp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dashlane.activatetotp.databinding.EnableTotpStepContainerBinding
import com.dashlane.activatetotp.databinding.EnableTotpStepSelectFrequencyContentBinding
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.util.setCurrentPageView

internal class EnableTotpSelectFrequencyFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val containerBinding = EnableTotpStepContainerBinding.inflate(inflater, container, false)

        val contentBinding = EnableTotpStepSelectFrequencyContentBinding.inflate(
            inflater,
            containerBinding.content,
            true
        )

        containerBinding.setup(
            stepNumber = 1,
            titleResId = R.string.enable_totp_select_frequency_title,
            descriptionResId = R.string.enable_totp_select_frequency_description,
            positiveButtonResId = R.string.enable_totp_select_frequency_cta,
            onClickPositiveButton = {
                findNavController().navigate(
                    EnableTotpSelectFrequencyFragmentDirections.goToRecoveryInfo(
                        totpLogin = contentBinding.optionEveryTime.isChecked
                    )
                )
            }
        )

        return containerBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setCurrentPageView(AnyPage.SETTINGS_SECURITY_TWO_FACTOR_AUTHENTICATION_ENABLE_SECURITY_LEVEL)
    }
}