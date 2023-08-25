package com.dashlane.ui.screens.settings.list.general.labs

import androidx.lifecycle.ViewModel
import com.dashlane.util.userfeatures.UserFeaturesChecker
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class DashlaneLabsViewModels @Inject constructor(
    private val featuresChecker: UserFeaturesChecker
) : ViewModel() {

    fun getLabsFeatureFlip(): List<DashlaneLabsItem> {
        return getLabsAvailableFeatureFlip()
            .filter { featuresChecker.has(it) }
            .map {
                DashlaneLabsItem(Pair(it, true))
            }
    }

    private fun getLabsAvailableFeatureFlip(): List<UserFeaturesChecker.FeatureFlip> {
        return UserFeaturesChecker.FeatureFlip.values()
            .toList()
            .filterNot { it == UserFeaturesChecker.FeatureFlip.DASHLANE_LABS }
    }
}