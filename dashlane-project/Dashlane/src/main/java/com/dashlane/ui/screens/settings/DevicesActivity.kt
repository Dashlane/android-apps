package com.dashlane.ui.screens.settings

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.fragment.compose.AndroidFragment
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.TopBarScaffold
import com.dashlane.ui.common.compose.components.TopBarState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DevicesActivity : DashlaneActivity() {
    override var requireUserUnlock: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        setContent {
            DashlaneTheme {
                var topBarState by rememberSaveable { mutableStateOf(TopBarState(title = getString(R.string.action_bar_title_devices))) }

                TopBarScaffold(
                    topBarState = topBarState,
                    back = { finish() },
                ) { contentPadding ->
                    AndroidFragment<DevicesFragment>(
                        modifier = Modifier.padding(contentPadding),
                    )
                }
            }
        }
    }
}