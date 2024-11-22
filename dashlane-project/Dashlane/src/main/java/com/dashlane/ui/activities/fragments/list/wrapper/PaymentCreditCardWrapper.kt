package com.dashlane.ui.activities.fragments.list.wrapper

import com.dashlane.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.teamspaces.manager.TeamSpaceAccessorProvider
import com.dashlane.teamspaces.ui.CurrentTeamSpaceUiFilter
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject
import com.dashlane.vault.textfactory.list.DataIdentifierListTextResolver

class PaymentCreditCardWrapper(
    vaultItemCopyService: VaultItemCopyService,
    quickActionProvider: QuickActionProvider,
    summaryObject: SummaryObject.PaymentCreditCard,
    itemListContext: ItemListContext,
    dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    navigator: Navigator,
    teamSpaceAccessorProvider: TeamSpaceAccessorProvider,
    currentSpaceFilterRepository: CurrentTeamSpaceUiFilter
) : DefaultVaultItemWrapper<SummaryObject.PaymentCreditCard>(
    vaultItemCopyService,
    quickActionProvider,
    summaryObject,
    itemListContext,
    navigator,
    dataIdentifierListTextResolver,
    teamSpaceAccessorProvider,
    currentSpaceFilterRepository
)