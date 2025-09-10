package com.dashlane.login.pin

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.stringResource
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.ui.R
import com.dashlane.ui.BottomSheetHeightConfig
import com.dashlane.ui.common.compose.components.GenericErrorContent
import com.dashlane.ui.configureBottomSheet
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PinErrorBottomSheet(private val listener: Actions) : BottomSheetDialogFragment() {
    interface Actions {
        fun goToLogin()

        fun goToSupport()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    GenericErrorContent(
                        modifier = Modifier.fillMaxHeight(),
                        title = stringResource(id = R.string.passwordless_pin_error_title),
                        message = stringResource(id = R.string.passwordless_pin_error_description),
                        textPrimary = stringResource(id = R.string.passwordless_pin_error_primary_button),
                        textSecondary = stringResource(id = R.string.passwordless_pin_error_secondary_button),
                        onClickPrimary = { dismiss() },
                        onClickSecondary = listener::goToSupport,
                    )
                }
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as BottomSheetDialog).configureBottomSheet(BottomSheetHeightConfig(peekHeightRatio = 1f, expandedHeightRatio = 1f))
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        listener.goToLogin()
    }
}