package com.dashlane.autofill.changepassword.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import com.dashlane.autofill.askchangepassword.AskChangePasswordBottomSheetScreen
import com.dashlane.design.theme.DashlaneTheme
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class AskChangePasswordDialogFragment(
    private val credentialThumbnailUrl: String?,
    private val credentialTitle: String,
    private val credentialLogin: String,
    private val listener: Actions,
) : BottomSheetDialogFragment() {
    interface Actions {
        fun changePassword()

        fun dismiss()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    AskChangePasswordBottomSheetScreen(
                        credentialThumbnailUrl = credentialThumbnailUrl,
                        credentialTitle = credentialTitle,
                        credentialLogin = credentialLogin,
                        onChangePasswordClick = ::onAskChangePasswordConfirmed,
                        onDismissOrCancel = ::onAskChangePasswordCancelled,
                    )
                }
            }
        }
    }

    private fun onAskChangePasswordConfirmed() {
        listener.changePassword()
        dismiss()
    }

    private fun onAskChangePasswordCancelled() {
        listener.dismiss()
        dismiss()
    }
}