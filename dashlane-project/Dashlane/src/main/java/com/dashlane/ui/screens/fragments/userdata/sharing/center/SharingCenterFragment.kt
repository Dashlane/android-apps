package com.dashlane.ui.screens.fragments.userdata.sharing.center

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.fragment.app.viewModels
import com.dashlane.R
import com.dashlane.design.theme.DashlaneTheme
import com.dashlane.featureflipping.FeatureFlip
import com.dashlane.featureflipping.UserFeaturesChecker
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.sharingcenter.SharingCenterScreen
import com.dashlane.ui.activities.fragments.AbstractContentFragment
import com.dashlane.ui.activities.fragments.list.wrapper.ItemWrapperProvider
import com.dashlane.util.setCurrentPageView
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class SharingCenterFragment : AbstractContentFragment() {
    private val viewModel by viewModels<SharingCenterViewModel>()

    @Inject
    lateinit var itemWrapperProvider: ItemWrapperProvider

    @Inject
    lateinit var userFeaturesChecker: UserFeaturesChecker

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        setCurrentPageView(AnyPage.SHARING_LIST)

        return if (userFeaturesChecker.has(FeatureFlip.NEW_SHARING_CENTER_COMPOSE)) {
            ComposeView(requireContext()).apply {
                setContent {
                    setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
                    DashlaneTheme {
                        SharingCenterScreen(
                            goToNewShare = {
                                navigator.goToNewShare()
                            },
                            goToSharedCollection = {
                                navigator.goToCollectionDetails(
                                    collectionId = it,
                                    sharedCollection = true,
                                )
                            },
                            goToItemDetail = { id, type ->
                                navigator.goToItem(
                                    uid = id,
                                    type = type,
                                )
                            },
                        )
                    }
                }
            }
        } else {
            val view: View = inflater.inflate(R.layout.fragment_sharing_center_data_list, container, false)
            SharingCenterViewProxy(this, view, viewModel, itemWrapperProvider)
            view
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        if (userFeaturesChecker.has(FeatureFlip.NEW_SHARING_CENTER_COMPOSE)) {
            
        } else {
            @Suppress("DEPRECATION")
            super.onCreateOptionsMenu(menu, inflater)
        }
    }
}