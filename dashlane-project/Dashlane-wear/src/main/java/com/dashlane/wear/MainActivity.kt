package com.dashlane.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import androidx.wear.remote.interactions.RemoteActivityHelper
import com.dashlane.navigation.popUpToTop
import com.dashlane.wear.WearApplicationDestination.LoginDestination
import com.dashlane.wear.WearApplicationDestination.VaultDestination
import com.dashlane.wear.login.wearLoginNavigation
import com.dashlane.wear.ui.DashlaneWearTheme
import com.dashlane.wear.vault.wearVaultNavigation
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.serialization.Serializable
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var remoteActivityHelper: RemoteActivityHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContent {
            DashlaneWearTheme {
                val navController = rememberSwipeDismissableNavController()
                SwipeDismissableNavHost(
                    startDestination = LoginDestination.destination,
                    navController = navController,
                ) {
                    wearLoginNavigation(
                        route = LoginDestination.destination,
                        navHostController = navController,
                        onLoginSuccess = {
                            navController.navigate(VaultDestination.destination) {
                                popUpToTop(
                                    navController,
                                )
                            }
                        },
                    )
                    wearVaultNavigation(
                        route = VaultDestination.destination,
                        navHostController = navController,
                        onLogout = {
                            navController.navigate(LoginDestination.destination) {
                                
                                popUpTo(navController.graph.id)
                            }
                        },
                        onOpenMobileActivity = { mobileIntent ->
                            remoteActivityHelper.startRemoteActivity(targetIntent = mobileIntent, targetNodeId = null)
                        },
                    )
                }
            }
        }
    }
}



sealed interface WearApplicationDestination {
    @Serializable
    data object LoginDestination : WearApplicationDestination {
        val destination: String
            get() = "login"
    }

    @Serializable
    data object VaultDestination : WearApplicationDestination {
        val destination: String
            get() = "vault"
    }
}