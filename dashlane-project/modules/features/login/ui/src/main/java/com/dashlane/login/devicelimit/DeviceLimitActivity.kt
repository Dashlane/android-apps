package com.dashlane.login.devicelimit

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.data.login.Device
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.NavigationConstants.EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class DeviceLimitActivity : DashlaneActivity() {
    @Inject
    lateinit var loginIntentFactory: LoginIntentFactory

    companion object {
        const val EXTRA_DEVICES = "extra_devices"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val goToSettings = { selectedDevices: List<Device>? ->
            val intent = loginIntentFactory.createLoginBiometricSetupIntent(this)
            selectedDevices?.let { intent.putExtra(EXTRA_DEVICE_SYNC_LIMIT_UNREGISTRATION, it.toTypedArray()) }
            startActivity(intent)
            finish()
        }

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.scaffoldBackgroundAndInsets(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    DeviceLimitScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        goToPremium = { startActivity(loginIntentFactory.createOfferIntent(this)) },
                        hasSync = { goToSettings(null) },
                        confirmUnregisterDevice = { selectedDevices -> goToSettings(selectedDevices) },
                        logout = {
                            startActivity(loginIntentFactory.createLoginActivityIntent(this))
                            finish()
                        },
                    )
                }
            }
        }
    }
}