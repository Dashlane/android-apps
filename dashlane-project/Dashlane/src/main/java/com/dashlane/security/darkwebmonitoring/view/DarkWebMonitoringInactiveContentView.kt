package com.dashlane.security.darkwebmonitoring.view

import android.content.Context
import android.util.AttributeSet
import androidx.compose.runtime.Composable
import com.dashlane.design.component.compat.view.ComposeViewWithPreview
import com.dashlane.design.component.compat.view.observableProperty
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.security.darkwebmonitoring.ui.DarkWebMonitoringInactiveContent

class DarkWebMonitoringInactiveContentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : ComposeViewWithPreview(context, attrs, defStyleAttr) {
    var onStartMonitoring: () -> Unit by observableProperty {
        
    }

    @Composable
    override fun CreateContent() {
        DashlaneTheme {
            DarkWebMonitoringInactiveContent(
                startMonitoring = onStartMonitoring,
            )
        }
    }
}