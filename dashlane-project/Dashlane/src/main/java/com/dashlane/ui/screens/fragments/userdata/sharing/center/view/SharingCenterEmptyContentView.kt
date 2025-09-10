package com.dashlane.ui.screens.fragments.userdata.sharing.center.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import com.dashlane.design.component.compat.view.ComposeViewWithPreview
import com.dashlane.design.component.compat.view.observableProperty
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.screens.fragments.userdata.sharing.center.ui.SharingCenterEmptyContent

class SharingCenterEmptyContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ComposeViewWithPreview(context, attrs, defStyleAttr) {
    var showAddLogin: Boolean by observableProperty(false)

    var onAddLoginClick: () -> Unit by observableProperty {
        
    }

    @Composable
    override fun CreateContent() {
        DashlaneTheme {
            SharingCenterEmptyContent(
                showAddLogin = showAddLogin,
                onAddLoginClick = onAddLoginClick,
            )
        }
    }
}