package com.dashlane.premium.paywall.trialended

import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.dashlane.design.iconography.IconTokens
import com.dashlane.frozenaccount.FrozenStateManager
import com.dashlane.help.HelpCenterLink
import com.dashlane.preference.UserPreferencesManager
import com.dashlane.premium.R
import com.dashlane.ui.activities.intro.DescriptionItem
import com.dashlane.ui.activities.intro.LinkItem
import com.dashlane.utils.coroutines.inject.qualifiers.IoCoroutineDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.time.Clock
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class TrialEndedViewModel @Inject constructor(
    private val frozenStateManager: FrozenStateManager,
    private val userPreferencesManager: UserPreferencesManager,
    @IoCoroutineDispatcher private val ioDispatcher: CoroutineDispatcher,
    private val clock: Clock
) : ViewModel() {

    private val _trialState = MutableStateFlow<FreeTrialScreenState>(FreeTrialScreenState.Init)
    val trialState = _trialState.asStateFlow()

    fun reload() {
        flow<FreeTrialScreenState> {
            emit(
                if (frozenStateManager.isAccountFrozen) {
                    FreeTrialScreenState.Loaded(
                        titleResId = R.string.trial_ended_user_title_for_frozen_account,
                        titleHeaderResId = R.string.trial_ended_user_title_header_for_frozen_account,
                        descriptionItems = getUpgradeBenefits(),
                        linkItems = listOf(
                            LinkItem.ExternalLinkItem(
                                linkResId = R.string.frozen_account_paywall_read_only_redirection,
                                link = HelpCenterLink.ARTICLE_ABOUT_FREE_PLAN_CHANGES.uri.toString()
                            )
                        ),
                        primaryButtonResId = R.string.frozen_account_paywall_cta_see_plans,
                        secondaryButtonResId = R.string.frozen_account_paywall_cta_close
                    )
                } else {
                    FreeTrialScreenState.Loaded(
                        titleResId = R.string.trial_ended_user_title,
                        titleHeaderResId = R.string.trial_ended_user_title_header,
                        descriptionItems = getUpgradeBenefits(),
                        primaryButtonResId = R.string.frozen_account_paywall_cta_see_plans,
                        secondaryButtonResId = R.string.frozen_account_paywall_cta_close
                    )
                }
            )
        }
            .flowOn(ioDispatcher)
            .onEach { state ->
                onLoaded()
                _trialState.emit(state)
            }
            .launchIn(viewModelScope)
    }

    private fun getUpgradeBenefits() = listOf(
        DescriptionItem(
            imageIconToken = IconTokens.itemLoginOutlined,
            titleResId = R.string.frozen_account_paywall_benefit_unlimited_logins_and_passkeys
        ),
        DescriptionItem(
            imageIconToken = IconTokens.featureAuthenticatorOutlined,
            titleResId = R.string.frozen_account_paywall_benefit_unlimited_devices
        ),
        DescriptionItem(
            imageIconToken = IconTokens.featureDarkWebMonitoringOutlined,
            titleResId = R.string.frozen_account_paywall_benefit_dark_web_monitoring_and_vpn
        )
    )

    private fun onLoaded() {
        userPreferencesManager.trialEndedAnnouncementTimestamp = Instant.now(clock).toEpochMilli()
    }
}

sealed class FreeTrialScreenState {
    data object Init : FreeTrialScreenState()

    data class Loaded(
        val descriptionItems: List<DescriptionItem>,
        @StringRes val titleResId: Int,
        @StringRes val titleHeaderResId: Int,
        val linkItems: List<LinkItem> = listOf(),
        @StringRes val primaryButtonResId: Int,
        @StringRes val secondaryButtonResId: Int,
    ) : FreeTrialScreenState()
}
