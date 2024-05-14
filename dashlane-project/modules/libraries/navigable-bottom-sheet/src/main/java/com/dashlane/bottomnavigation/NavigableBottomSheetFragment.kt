package com.dashlane.bottomnavigation

import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

interface NavigableBottomSheetFragment {
    fun getNavHostFragment(): NavHostFragment? {
        val fragment = this as? Fragment ?: return null

        return fragment.parentFragment as? NavHostFragment
    }
}