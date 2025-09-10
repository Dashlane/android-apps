package com.dashlane.limitations

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.DialogFragment
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.Navigator
import com.dashlane.ui.R
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class PasswordLimitBottomSheetFragment : BottomSheetDialogFragment() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var passwordLimitationLogger: PasswordLimitationLogger

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(DialogFragment.STYLE_NO_FRAME, R.style.Theme_Dashlane_Transparent_Cancelable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setContent {
                DashlaneTheme {
                    PasswordLimitBottomSheetScreen(
                        onUpgradeClicked = {
                            passwordLimitationLogger.upgradeFromBottomSheetPaywall()
                            navigator.goToOffers()
                        },
                        onNotNowClicked = {
                            passwordLimitationLogger.dismissBottomSheetPaywall()
                            dismiss()
                        },
                    )
                }
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        passwordLimitationLogger.logPasswordLimitDisplayed()
        return super.onCreateDialog(savedInstanceState)
    }

    override fun onCancel(dialog: DialogInterface) {
        super.onCancel(dialog)

        passwordLimitationLogger.cancelBottomSheetPaywall()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        requireActivity().finish()
    }
}