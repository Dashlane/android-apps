package com.dashlane.pin.settings

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.compose.BackPressedDispatcherBridge
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PinSettingsActivity : DashlaneActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val menuProvider = BackPressedDispatcherBridge.getMenuProvider(this)
        addMenuProvider(menuProvider, this)

        setContent {
            DashlaneTheme {
                PinSettingsNavHost(
                    onSuccess = {
                        setResult(RESULT_OK, Intent())
                        finish()
                    },
                    onCancel = {
                        setResult(RESULT_CANCELED, Intent())
                        finish()
                    }
                )
            }
        }
    }
}
