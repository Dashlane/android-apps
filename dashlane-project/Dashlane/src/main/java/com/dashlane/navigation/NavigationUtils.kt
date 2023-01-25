package com.dashlane.navigation

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import com.dashlane.analytics.metrics.time.SpentTimeOnViewManager
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.events.clearLastEvent
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.LoginActivity
import com.dashlane.login.LoginActivity.ALLOW_SKIP_EMAIL
import com.dashlane.security.DashlaneIntent
import com.dashlane.ui.fragments.BaseDialogFragment
import com.dashlane.util.DevUtil
import com.dashlane.util.clearTask
import com.dashlane.util.clearTop
import com.dashlane.util.getBaseActivity
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object NavigationUtils {

    private val originArguments = setOf("origin", "sender", "param_sender")

    @JvmStatic
    fun matchDestination(destination: NavDestination, destIds: Array<Int>?): Boolean {
        destIds ?: return false
        var currentDestination = destination
        while (destIds.all { it != currentDestination.id } && currentDestination.parent != null) {
            currentDestination = currentDestination.parent!!
        }
        return destIds.any { it == currentDestination.id }
    }

    fun matchDestination(currentBackStack: NavBackStackEntry?, action: NavDirections): Boolean {
        val destination = currentBackStack?.destination ?: return false
        val newDestination = destination.getAction(action.actionId)?.destinationId ?: return false
        val arguments = currentBackStack.arguments
        return when {
            arguments?.size() != action.arguments.size() -> false
            matchDestination(destination, arrayOf(newDestination)) ->
                arguments.keySet().filterNot { it in originArguments }.all { key ->
                    arguments[key] == action.arguments[key]
                }
            else -> false
        }
    }

    fun hideDialogs(activity: FragmentActivity) = activity.apply {
        
        val fragments = supportFragmentManager.fragments
        for (fragment in fragments) {
            if (fragment is BaseDialogFragment && fragment.showsDialog) {
                fragment.dismiss()
            }
        }
    }

    @JvmStatic
    fun logoutAndCallLoginScreen(context: Context, allowSkipEmail: Boolean = false) {
        val userSupportFileLogger = SingletonProvider.getUserSupportFileLogger()
        userSupportFileLogger.add("Lock action Logout")
        val intent = if (context is Activity) {
            context.intent
        } else {
            null
        }
        logoutAndCallLoginScreen(context, intent, allowSkipEmail)
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun logoutAndCallLoginScreen(context: Context, originalIntent: Intent?, allowSkipEmail: Boolean) {
        val sessionManager = SingletonProvider.getSessionManager()
        val appContext = context.applicationContext
        
        
        
        
        
        val contextIsLogin = context is LoginActivity
        val baseContext = context.getBaseActivity() 
        if (baseContext is Activity && !contextIsLogin) {
            SpentTimeOnViewManager.getInstance().leaveView(baseContext.localClassName)
            baseContext.finish()
        }

        SingletonProvider.getAppEvents().clearLastEvent<UnlockEvent>()
        GlobalScope.launch(Dispatchers.Main) {
            sessionManager.session?.let { sessionManager.destroySession(it, true) }
            val loginIntent = DashlaneIntent.newInstance(appContext, LoginActivity::class.java)
            if (contextIsLogin) loginIntent.clearTop() else loginIntent.clearTask()
            if (originalIntent != null && originalIntent.hasExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN)) {
                loginIntent.putExtra(
                    NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN,
                    originalIntent.getBooleanExtra(NavigationConstants.LOGIN_CALLED_FROM_INAPP_LOGIN, false)
                )
            }

            if (allowSkipEmail) {
                loginIntent.putExtra(ALLOW_SKIP_EMAIL, true)
            }
            DevUtil.startActivityOrDefaultErrorMessage(context, loginIntent)
        }
    }
}
