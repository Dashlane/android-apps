package com.dashlane.login.monobucket

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.login.LoginIntents
import com.dashlane.login.progress.LoginSyncProgressActivity
import com.dashlane.premium.offer.list.view.OffersActivity
import com.dashlane.ui.activities.DashlaneActivity
import com.dashlane.util.startActivity
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonobucketActivity : DashlaneActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val goToSettings = {
            val intent = LoginIntents.createSettingsActivityIntent(this)
            startActivity(intent)
            finish()
        }

        setContent {
            DashlaneTheme {
                Scaffold(
                    modifier = Modifier.safeContentPadding(),
                    containerColor = DashlaneTheme.colors.backgroundAlternate
                ) { contentPadding ->
                    MonobucketScreen(
                        modifier = Modifier.padding(contentPadding),
                        viewModel = hiltViewModel(),
                        goToPremium = {
                            startActivity(Intent(this, OffersActivity::class.java))
                        },
                        hasSync = {
                            goToSettings()
                        },
                        confirmUnregisterDevice = {
                            
                            intent.putExtra(LoginSyncProgressActivity.EXTRA_MONOBUCKET_UNREGISTRATION, true)
                            goToSettings()
                        },
                        logout = {
                            startActivity(LoginIntents.createLoginActivityIntent(this))
                            finish()
                        }
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_BUCKET_OWNER = "bucket_owner"
    }
}
