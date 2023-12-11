package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.vault.util.IdentityNameHolderService
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SharingNewShareItemFragment : AbstractContentFragment() {

    @Inject
    lateinit var itemWrapperProvider: ItemWrapperProvider

    @Inject
    lateinit var identityNameHolderService: IdentityNameHolderService

    
    private val viewModel by viewModels<NewShareItemViewModel>({ requireParentFragment() })

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_data_list, container, false)
        val rawDataType = requireArguments().getString(EXTRA_DATA_TYPE)
        val type = rawDataType
            ?.let { SyncObjectType.forXmlNameOrNull(it) }
            ?: throw IllegalArgumentException("Unsupported type $rawDataType")
        SharingNewShareItemViewProxy(
            lifecycle,
            view,
            viewModel,
            type,
            itemWrapperProvider,
            identityNameHolderService
        )

        return view
    }

    companion object {
        private const val EXTRA_DATA_TYPE = "extra_data_type"
        fun newInstance(dataType: SyncObjectType): SharingNewShareItemFragment {
            val fragment = SharingNewShareItemFragment()
            fragment.arguments =
                Bundle().also { it.putString(EXTRA_DATA_TYPE, dataType.xmlObjectName) }
            return fragment
        }
    }
}