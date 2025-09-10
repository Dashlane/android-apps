package com.dashlane.premium.current

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.premium.current.ui.CurrentPlanScreen
import com.dashlane.ui.activities.DashlaneActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CurrentPlanActivity : DashlaneActivity() {
    private val viewModel: CurrentPlanViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneTheme {
                CurrentPlanScreen(
                    viewModel = viewModel,
                    onCloseWithoutAction = ::onCloseWithoutAction,
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isFinishing) {
            viewModel.onDestroy()
        }
    }

    private fun onCloseWithoutAction() {
        finish()
    }
}