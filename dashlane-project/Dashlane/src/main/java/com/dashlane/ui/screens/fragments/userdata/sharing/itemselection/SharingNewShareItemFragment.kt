package com.dashlane.ui.screens.fragments.userdata.sharing.itemselection

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingNewShareItemFragment : AbstractContentFragment() {
    
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
        SharingNewShareItemViewProxy(lifecycle, view, viewModel, type)

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