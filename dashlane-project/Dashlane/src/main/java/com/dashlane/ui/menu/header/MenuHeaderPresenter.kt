package com.dashlane.ui.menu.header

import android.content.Context
import androidx.annotation.StringRes
import androidx.annotation.VisibleForTesting
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.AdvancedIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.EssentialsIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Family
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.FamilyPlus
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Free
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Legacy
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.PremiumIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.PremiumPlusIndividual
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Trial
import com.dashlane.premium.offer.common.model.UserBenefitStatus.Type.Unknown
import com.dashlane.teamspaces.manager.TeamspaceDrawableProvider
import com.dashlane.teamspaces.model.Teamspace
import com.dashlane.ui.drawable.CenterWrapperDrawable
import com.dashlane.ui.menu.MenuComponent
import com.dashlane.ui.menu.MenuDef
import com.dashlane.ui.menu.MenuItemProvider
import com.skocken.presentation.presenter.BaseItemPresenter



open class MenuHeaderPresenter internal constructor(
    private val injectedMenuComponent: MenuComponent? = null
) : BaseItemPresenter<MenuHeaderDef.IDataProvider?, MenuHeaderDef.IView?>(),
    MenuHeaderDef.IPresenter {

    private val menuComponent
        get() = injectedMenuComponent ?: MenuComponent(checkNotNull(context))
    private val menuPresenter
        get() = menuComponent.menuPresenter
    private val navigator
        get() = menuComponent.navigator

    init {
        this.setProvider(MenuHeaderDataProvider())
    }

    override fun updateView(
        context: Context,
        item: MenuDef.Item?
    ) {
        val teamspace = provider.currentTeamspace
        if (teamspace == null) {
            updateViewDefault(context)
        } else {
            updateViewTeamspace(teamspace)
        }
    }

    private fun updateViewDefault(context: Context) {
        val provider = provider
        val username = provider.userAlias
        val icon = getUserStatusDrawable(context)
        val view = view
        view.setTeamspaceSelectorVisible(false)
        view.setUpgradeVisible(canUpgrade())
        view.setStatus(getAccountStatusText(provider))
        view.setUsername(username)
        view.setIcon(icon)
    }

    private fun updateViewTeamspace(teamspace: Teamspace) {
        val view = view
        view.setSelectorIconUp(isModeTeamspace)
        view.setTeamspaceSelectorVisible(true)
        view.setUpgradeVisible(false)
        val drawable = geTeamSpaceDrawable(teamspace)
        view.setIcon(CenterWrapperDrawable(drawable))
        view.setTeamspaceName(teamspace.teamName)
    }

    private val isModeTeamspace: Boolean
        get() = menuPresenter.mode == MenuDef.Mode.TEAMSPACE

    override fun onHeaderTeamspaceSelectorClick() {
        if (menuPresenter.mode == MenuDef.Mode.DEFAULT) {
            menuPresenter.mode = MenuDef.Mode.TEAMSPACE
        } else {
            menuPresenter.mode = MenuDef.Mode.DEFAULT
        }
    }

    override fun onHeaderProfileClick() {
        if (!canUpgrade()) return
        navigator.goToCurrentPlan(MenuItemProvider.ORIGIN_MENU)
    }

    override fun onHeaderUpgradeClick() {
        navigator.goToOffers(MenuItemProvider.ORIGIN_MENU)
    }

    @VisibleForTesting
    @StringRes
    fun getAccountStatusText(provider: MenuHeaderDef.IDataProvider) = when (provider.statusType) {
        Legacy -> R.string.menu_user_profile_status_legacy
        Trial -> R.string.menu_user_profile_status_trial
        AdvancedIndividual -> R.string.plans_advanced_title
        EssentialsIndividual -> R.string.menu_user_profile_status_essentials
        PremiumIndividual -> R.string.menu_user_profile_status_premium
        PremiumPlusIndividual -> R.string.menu_user_profile_status_premium_plus
        is Family -> R.string.menu_user_profile_status_premium_family
        is FamilyPlus -> R.string.menu_user_profile_status_premium_plus_family
        Free, Unknown -> R.string.menu_user_profile_status_free
    }

    @VisibleForTesting
    open fun getUserStatusDrawable(context: Context?) = context?.let {
        ContextCompat.getDrawable(context, R.drawable.ic_menu_dashlane)
    }

    @VisibleForTesting
    open fun geTeamSpaceDrawable(teamspace: Teamspace) = context?.let { context ->
        TeamspaceDrawableProvider.getIcon(
            context = context,
            teamspace = teamspace,
            dimenResId = R.dimen.teamspace_icon_size_menu
        )
    }

    private fun canUpgrade() = when (val status = provider.statusType) {
        is Family -> status.isAdmin
        is FamilyPlus -> status.isAdmin
        else -> true
    }
}