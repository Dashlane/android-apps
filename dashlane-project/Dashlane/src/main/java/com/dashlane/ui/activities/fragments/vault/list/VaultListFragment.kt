package com.dashlane.ui.activities.fragments.vault.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.Fragment
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.home.vaultlist.setVaultListContent
import com.dashlane.navigation.Navigator
import com.dashlane.vault.Filter
import com.dashlane.vault.VaultItemLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VaultListFragment : Fragment() {
    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var vaultItemLogger: VaultItemLogger

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setVaultListContent()
    }

    companion object {
        const val EXTRA_FILTER = "extra_filter"

        fun newInstance(filter: Filter) = VaultListFragment().apply {
            val bundle = Bundle()
            bundle.putParcelable(EXTRA_FILTER, filter)
            arguments = bundle
        }
    }
}