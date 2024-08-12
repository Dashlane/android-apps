package com.dashlane.item.passwordhistory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.viewModels
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.fragment.findNavController
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.passwordhistory.PasswordHistoryViewModel.PasswordHistoryState
import com.dashlane.util.clipboard.ClipboardCopy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordHistoryFragment : Fragment() {
    val viewModel by viewModels<PasswordHistoryViewModel>()

    @Inject
    lateinit var clipboardCopy: ClipboardCopy

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        val uid = PasswordHistoryFragmentArgs.fromBundle(requireArguments()).uid
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                val uiState by viewModel.historyState.collectAsStateWithLifecycle()

                LaunchedEffect(key1 = uiState) {
                    when (uiState) {
                        PasswordHistoryState.Init -> viewModel.reloadForItemUid(uid = uid)
                        PasswordHistoryState.Error -> finishWithResult(error = true)
                        PasswordHistoryState.Success -> finishWithResult(error = false)
                        is PasswordHistoryState.Loaded -> Unit
                    }
                }

                DashlaneTheme {
                    PasswordHistoryScreen(
                        state = uiState,
                        onRevertClick = { selectedPassword ->
                            viewModel.restorePassword(
                                vaultItemUid = uid,
                                selectedPasswordHistory = selectedPassword
                            )
                            true
                        },
                        onCopyClick = {
                            clipboardCopy.copyToClipboard(
                                data = it.password,
                                sensitiveData = true,
                                feedback = R.string.feedback_copy_password
                            )
                            true
                        },
                    )
                }
            }
        }
    }

    private fun finishWithResult(error: Boolean) {
        setFragmentResult(PASSWORD_HISTORY_RESULT, bundleOf(FINISHED_WITH_ERROR_EXTRA to error))
        findNavController().popBackStack()
    }

    companion object {
        const val FINISHED_WITH_ERROR_EXTRA: String = "finished_with_error"
        const val PASSWORD_HISTORY_RESULT = "password_history_result"
    }
}