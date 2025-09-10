package com.dashlane.importlogins

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.importlogins.ui.ImportLoginsScreen
import com.dashlane.navigation.Navigator
import com.dashlane.ui.fragments.BaseUiFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImportLoginsFragment : BaseUiFragment() {
    @Inject
    lateinit var navigator: Navigator

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    ImportLoginsScreen(
                        viewModel = hiltViewModel(),
                        goToCsvFileImportIntro = ::goToCsvFileImportIntro,
                        goToDashFileImportIntro = ::goToDashFileImportIntro,
                        goToAddLoginManually = ::goToAddLoginManually,
                    )
                }
            }
        }
    }

    private fun goToCsvFileImportIntro() {
        navigator.goToCsvImportIntro()
    }

    private fun goToDashFileImportIntro() {
        
    }

    private fun goToAddLoginManually() {
        navigator.goToCredentialAddStep1()
    }
}