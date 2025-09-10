package com.dashlane.autofill.actionssources.view

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ActionsSourcesActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                ActionsSourcesScreen(
                    viewModel = hiltViewModel(),
                    onNavigateUp = {
                        finish()
                    },
                )
            }
        }
    }
}