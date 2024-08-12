package com.dashlane.login.pages.secrettransfer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginIntents
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginSecretTransferFragment : Fragment() {

    val args: LoginSecretTransferFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val email: String? = args.email
        val startDestination: String = args.startDestination ?: LoginSecretTransferNavigation.qrCodeDestination

        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    LoginSecretTransferNavigation(
                        startDestination = startDestination,
                        email = email,
                        onSuccess = {
                            activity?.run {
                                startActivity(LoginIntents.createProgressActivityIntent(this))
                                finish()
                            }
                        },
                        onCancel = {
                            this@LoginSecretTransferFragment.findNavController().popBackStack()
                        }
                    )
                }
            }
        }
    }
}