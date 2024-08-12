package com.dashlane.navigation

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavDestination
import androidx.navigation.NavDirections
import androidx.navigation.NavType
import com.dashlane.cryptography.ObfuscatedByteArray
import com.dashlane.cryptography.encodeUtf8ToObfuscated
import com.dashlane.ui.fragments.BaseDialogFragment
import com.dashlane.util.getSerializableCompat

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

class ObfuscatedByteArrayParamType : NavType<ObfuscatedByteArray>(isNullableAllowed = false) {
    override fun get(bundle: Bundle, key: String): ObfuscatedByteArray? {
        return bundle.getSerializableCompat(key, ObfuscatedByteArray::class.java)
    }

    override fun parseValue(value: String): ObfuscatedByteArray {
        return value.encodeUtf8ToObfuscated()
    }

    override fun put(bundle: Bundle, key: String, value: ObfuscatedByteArray) {
        bundle.putSerializable(key, value)
    }
}
