package com.dashlane.bottomnavigation.delegatenavigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.dashlane.bottomnavigation.NavigableBottomSheetFragment

interface DelegateNavigationBottomSheetFragment : NavigableBottomSheetFragment {

    interface NavigationDelegate {
        fun delegatedNavigate(delegateNavigationBottomSheetFragment: DelegateNavigationBottomSheetFragment)
    }

    fun delegateNavigation() {
        runIfValidDelegatee { navigationDelegatee, navigateFromBottomSheetFragmentActivityViewModel ->
            if (!navigateFromBottomSheetFragmentActivityViewModel.navigated) {
                navigateFromBottomSheetFragmentActivityViewModel.navigated = true
                navigationDelegatee.delegatedNavigate(this)
            }
        }
    }

    private fun runIfValidDelegatee(
        block: ((NavigationDelegate, NavigateFromBottomSheetFragmentActivityViewModel) -> Unit)
    ) {
        val navHostFragment = getNavHostFragment() ?: return
        val fragmentActivity = navHostFragment.activity ?: return
        if (fragmentActivity !is NavigationDelegate) {
            return
        }
        val delegationOnceViewModel = ViewModelProvider(fragmentActivity).get(
            NavigateFromBottomSheetFragmentActivityViewModel::class.java
        )

        block.invoke(fragmentActivity, delegationOnceViewModel)
    }

    class NavigateFromBottomSheetFragmentActivityViewModel : ViewModel() {
        var navigated: Boolean = false
    }
}