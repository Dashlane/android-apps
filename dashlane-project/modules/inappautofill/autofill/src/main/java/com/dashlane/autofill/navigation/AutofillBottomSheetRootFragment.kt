package com.dashlane.autofill.navigation

import android.os.Bundle
import androidx.fragment.app.Fragment
import com.dashlane.bottomnavigation.delegatenavigation.DelegateNavigationBottomSheetFragment

class AutofillBottomSheetRootFragment :
    DelegateNavigationBottomSheetFragment,
    Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        delegateNavigation()
    }
}