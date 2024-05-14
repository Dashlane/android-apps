package com.dashlane.ui.activities.fragments.vault.list

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.dashlane.databinding.FragmentVaultListBinding
import com.dashlane.navigation.Navigator
import com.dashlane.ui.activities.fragments.vault.Filter
import com.dashlane.ui.activities.fragments.vault.VaultViewModel
import com.dashlane.vault.VaultItemLogger
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VaultListFragment : Fragment() {

    @Inject
    lateinit var navigator: Navigator

    @Inject
    lateinit var vaultItemLogger: VaultItemLogger

    private val vaultListViewModel by viewModels<VaultListViewModel>()
    private val vaultViewModel: VaultViewModel by viewModels(ownerProducer = { requireParentFragment() })

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val binding = FragmentVaultListBinding.inflate(inflater, container, false)
        VaultListViewProxy(vaultViewModel, vaultItemLogger, vaultListViewModel, this, binding, navigator)
        return binding.root
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