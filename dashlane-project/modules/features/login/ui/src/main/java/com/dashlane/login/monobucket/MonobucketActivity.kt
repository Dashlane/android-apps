package com.dashlane.login.monobucket

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.navigation.LoginIntentFactory
import com.dashlane.navigation.NavigationConstants.EXTRA_MONOBUCKET_UNREGISTRATION
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.ui.common.compose.components.basescreen.scaffoldBackgroundAndInsets
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MonobucketActivity : DashlaneActivity() {
    @Inject
    lateinit var loginIntentFactory: LoginIntentFactory

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        val goToSettings = {
            val intent = loginIntentFactory.createLoginBiometricSetupIntent(this)
            startActivity(intent)
            finish()
        }

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.scaffoldBackgroundAndInsets(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate,
                ) { contentPadding ->
                    MonobucketScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        goToPremium = {
                            startActivity(loginIntentFactory.createOfferIntent(this))
                        },
                        hasSync = {
                            goToSettings()
                        },
                        confirmUnregisterDevice = {
                            
                            intent.putExtra(EXTRA_MONOBUCKET_UNREGISTRATION, true)
                            goToSettings()
                        },
                        logout = {
                            startActivity(loginIntentFactory.createLoginActivityIntent(this))
                            finish()
                        },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_BUCKET_OWNER = "bucket_owner"
    }
}