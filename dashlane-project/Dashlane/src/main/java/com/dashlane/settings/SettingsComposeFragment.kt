package com.dashlane.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.hilt.navigation.compose.hiltViewModel
import com.dashlane.R
import com.dashlane.accountsummary.AccountSummaryActivity
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.item.subview.action.LoginOpener
import com.dashlane.lock.LockEvent.Unlock.Reason
import com.dashlane.lock.LockNavigationHelper
import com.dashlane.lock.LockPrompt
import com.dashlane.navigation.Navigator
import com.dashlane.premium.current.CurrentPlanActivity
import com.dashlane.secrettransfer.view.SecretTransferActivity
import com.dashlane.settings.main.SettingsMainScreen
import com.dashlane.settings.main.account.SettingsAccountState
import com.dashlane.settings.main.advanced.SettingsAdvancedState
import com.dashlane.settings.main.misc.MainMiscToast
import com.dashlane.settings.main.misc.SettingsMainMiscState
import com.dashlane.ui.activities.debug.DebugActivity
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.IntentFactory
import com.dashlane.util.Toaster
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

private const val UNLOCK_EVENT_CODE = 474

@AndroidEntryPoint
class SettingsComposeFragment : AbstractContentFragment() {
    @Inject
    lateinit var lockNavigationHelper: LockNavigationHelper

    @Inject
    lateinit var toaster: Toaster

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                DashlaneTheme {
                    val menuHost: MenuHost = requireActivity()
                    val menuProvider = remember {
                        object : MenuProvider {
                            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                                menuInflater.inflate(R.menu.settings_menu, menu)
                            }

                            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                                return when (menuItem.itemId) {
                                    R.id.menu_search -> {
                                        navigator.goToSearchCompose()
                                        true
                                    }
                                    else -> false
                                }
                            }
                        }
                    }
                    LaunchedEffect(key1 = menuProvider) {
                        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner)
                    }

                    SettingsMainScreen(
                        accountViewModel = hiltViewModel(),
                        miscViewModel = hiltViewModel(),
                        advancedViewModel = hiltViewModel(),
                        accountSideEffect = { activity?.checkAccountSideEffect(it) },
                        advancedSideEffect = { activity?.checkAdvancedSideEffect(lockNavigationHelper, it) },
                        mainMiscSideEffect = { activity?.checkMainMiscSideEffect(navigator, toaster, it) },
                    )
                }
            }
        }
    }
}

internal fun Activity.checkAccountSideEffect(sideEffect: SettingsAccountState.SideEffect) {
    when (sideEffect) {
        SettingsAccountState.SideEffect.GoToAccountStatus -> {
            startActivity(Intent(this, AccountSummaryActivity::class.java))
        }

        SettingsAccountState.SideEffect.GoToCurrentPlan -> {
            startActivity(Intent(this, CurrentPlanActivity::class.java))
        }
    }
}

internal fun Activity.checkAdvancedSideEffect(
    lockNavigationHelper: LockNavigationHelper,
    sideEffect: SettingsAdvancedState.SideEffect,
) {
    when (sideEffect) {
        is SettingsAdvancedState.SideEffect.DisplayLock -> displayLock(
            lockNavigationHelper,
            sideEffect.forceMasterPassword,
        )

        SettingsAdvancedState.SideEffect.GoToGeneral -> {
            startActivity(Intent(this, SettingsGeneralActivity::class.java))
        }

        SettingsAdvancedState.SideEffect.GoToHelp -> {
            startActivity(Intent(this, SettingsHelpActivity::class.java))
        }

        SettingsAdvancedState.SideEffect.GoToSecretTransfer -> {
            startActivity(Intent(this, SecretTransferActivity::class.java))
        }

        SettingsAdvancedState.SideEffect.GoToSecurity -> {
            startActivity(Intent(this, SettingsSecurityActivity::class.java))
        }
    }
}

internal fun Activity.checkMainMiscSideEffect(
    navigator: Navigator,
    toaster: Toaster,
    sideEffect: SettingsMainMiscState.SideEffect,
) {
    when (sideEffect) {
        is SettingsMainMiscState.SideEffect.DisplayToast -> {
            val resId = when (val message = sideEffect.message) {
                MainMiscToast.Copy -> R.string.copied_
                is MainMiscToast.Error -> {
                    when (message.error) {
                        SettingsError.Generic -> null
                        SettingsError.MainMiscError.NoAppAvailable -> R.string.contact_system_administrator
                        SettingsError.Network -> R.string.network_failed_notification
                        SettingsError.Offline -> R.string.make_sure_you_have_internet_for_refferalid
                    }
                }
            }
            resId?.let { toaster.show(it, Toast.LENGTH_LONG) }
        }

        SettingsMainMiscState.SideEffect.GoToDebug -> {
            startActivity(Intent(this, DebugActivity::class.java))
        }

        is SettingsMainMiscState.SideEffect.GoToInviteFriend -> {
            startActivity(sideEffect.intent)
        }

        SettingsMainMiscState.SideEffect.GoToLogout -> {
            navigator.logoutAndCallLoginScreen(this)
        }

        SettingsMainMiscState.SideEffect.GoToSendLove -> {
            IntentFactory.sendMarketIntent(this)
        }

        is SettingsMainMiscState.SideEffect.GoToUrl -> {
            LoginOpener(this).show(url = sideEffect.url.toString(), packageNames = setOf(), listener = null)
        }
    }
}

internal fun Activity.displayLock(lockNavigationHelper: LockNavigationHelper, forceMasterPassword: Boolean) {
    lockNavigationHelper.showLockActivityForReason(
        context = this,
        reason = Reason.WithCode(requestCode = UNLOCK_EVENT_CODE, origin = Reason.WithCode.Origin.EDIT_SETTINGS),
        lockPrompt = LockPrompt.ForSettings(forceMasterPassword = forceMasterPassword),
    )
}