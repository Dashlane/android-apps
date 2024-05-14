package com.dashlane.accountrecoverykey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.navigation.fragment.navArgs
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountRecoveryKeyFragment : AbstractContentFragment() {

    val args: AccountRecoveryKeyFragmentArgs by navArgs()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val startDestination: String = args.startDestination ?: AccountRecoveryKeySettingsNavigation.detailSettingDestination
        val userCanExitFlow: Boolean = args.userCanExitFlow

        activity?.let {
            val menuProvider = BackPressedDispatcherBridge.getMenuProvider(it)
            it.addMenuProvider(menuProvider, viewLifecycleOwner)
        }

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    AccountRecoveryKeySettingsNavigation(
                        startDestination = startDestination,
                        userCanExitFlow = userCanExitFlow
                    )
                }
            }
        }
    }
}
