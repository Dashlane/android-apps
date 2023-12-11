package com.dashlane.navigation

import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import com.dashlane.ui.fragments.BaseDialogFragment

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
}
