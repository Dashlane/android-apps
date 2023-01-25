package com.dashlane.ui.screens.fragments.userdata.sharing.items

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SharingItemsForUserFragment : AbstractContentFragment() {
    private val viewModel by viewModels<SharingItemsForUserViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setCurrentPageView(AnyPage.SHARING_MEMBER_DETAILS)
        val view: View = inflater.inflate(R.layout.fragment_data_list, container, false)

        SharingItemsForUserViewProxy(this, view, viewModel)
        return view
    }
}