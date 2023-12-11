package com.dashlane.accountrecoverykey

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuProvider
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AccountRecoveryKeyFragment : AbstractContentFragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val menuProvider = object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                val consumed = if (menuItem.itemId == android.R.id.home) {
                    activity?.onBackPressedDispatcher?.onBackPressed() 
                    true
                } else {
                    false
                }
                return consumed
            }
        }

        activity?.addMenuProvider(menuProvider, viewLifecycleOwner)

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    AccountRecoveryKeySettingsNavigation()
                }
            }
        }
    }
}
