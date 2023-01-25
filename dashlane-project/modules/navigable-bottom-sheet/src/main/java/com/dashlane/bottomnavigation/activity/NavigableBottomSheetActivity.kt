package com.dashlane.bottomnavigation.activity

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import androidx.navigation.findNavController
import com.dashlane.bottomnavigation.NavigableBottomSheetDialogFragmentArgs
import com.dashlane.bottomnavigation.R



interface NavigableBottomSheetActivity {
    fun getBottomSheetNavigationGraphId(): Int
    fun getStartDestinationId(): Int = R.id.use_graph_start_destination
    fun getStartDestinationArgs(): Bundle? = null
    fun getConsumeBackPress(): Boolean = true

    fun configureBottomSheetDialogNavigation(bottomSheetDialogNavHostFragmentId: Int) {
        val fragmentActivity = this as? FragmentActivity ?: return
        val navController = fragmentActivity.findNavController(bottomSheetDialogNavHostFragmentId)

        val startDestinationArgs = NavigableBottomSheetDialogFragmentArgs(
            navigationGraphId = getBottomSheetNavigationGraphId(),
            startDestinationId = getStartDestinationId(),
            startDestinationArgs = getStartDestinationArgs(),
            consumeBackPress = getConsumeBackPress()
        ).toBundle()

        navController.setGraph(
            R.navigation.navigable_bottom_sheet_dialog_navigation,
            startDestinationArgs
        )
    }
}