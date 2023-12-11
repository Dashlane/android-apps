package com.dashlane.ui.activities.fragments.list.wrapper

import com.dashlane.item.subview.quickaction.QuickActionProvider
import com.dashlane.navigation.Navigator
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.TeamspaceManagerRepository
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.adapters.text.factory.DataIdentifierListTextResolver
import com.dashlane.util.clipboard.vault.VaultItemCopyService
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject

class ItemWrapperProvider @Inject constructor(
    private val vaultItemCopyService: VaultItemCopyService,
    private val navigator: Navigator,
    private val dataIdentifierListTextResolver: DataIdentifierListTextResolver,
    private val sessionManager: SessionManager,
    private val teamspaceRepository: TeamspaceManagerRepository,
    private val quickActionProvider: QuickActionProvider
) {
    operator fun invoke(item: SummaryObject, container: ItemListContext): VaultItemWrapper<out SummaryObject>? {
        return getItemWrapper(item, container)
    }

    private fun getItemWrapper(item: SummaryObject, container: ItemListContext): VaultItemWrapper<out SummaryObject>? {
        return when (item) {
            is SummaryObject.Address,
            is SummaryObject.BankStatement,
            is SummaryObject.Company,
            is SummaryObject.FiscalStatement,
            is SummaryObject.Email,
            is SummaryObject.PaymentPaypal,
            is SummaryObject.PersonalWebsite,
            is SummaryObject.Phone,
            is SummaryObject.Identity,
            is SummaryObject.SocialSecurityStatement,
            is SummaryObject.SecureFileInfo,
            is SummaryObject.Authentifiant,
            is SummaryObject.DriverLicence,
            is SummaryObject.IdCard,
            is SummaryObject.Passkey,
            is SummaryObject.SecureNote,
            is SummaryObject.Passport -> DefaultVaultItemWrapper(
                vaultItemCopyService = vaultItemCopyService,
                quickActionProvider = quickActionProvider,
                summaryObject = item,
                itemListContext = container,
                dataIdentifierListTextResolver = dataIdentifierListTextResolver,
                navigator = navigator,
                sessionManager = sessionManager,
                teamspaceRepository = teamspaceRepository
            )
            is SummaryObject.PaymentCreditCard -> PaymentCreditCardWrapper(
                vaultItemCopyService = vaultItemCopyService,
                quickActionProvider = quickActionProvider,
                summaryObject = item,
                itemListContext = container,
                dataIdentifierListTextResolver = dataIdentifierListTextResolver,
                navigator = navigator,
                sessionManager = sessionManager,
                teamspaceRepository = teamspaceRepository
            )
            else -> null
        }
    }

    fun getAuthentifiantItemWrapper(
        item: SummaryObject.Authentifiant,
        container: ItemListContext
    ): DefaultVaultItemWrapper<SummaryObject.Authentifiant> {
        return DefaultVaultItemWrapper(
            vaultItemCopyService = vaultItemCopyService,
            quickActionProvider = quickActionProvider,
            summaryObject = item,
            itemListContext = container,
            navigator = navigator,
            dataIdentifierListTextResolver = dataIdentifierListTextResolver,
            sessionManager = sessionManager,
            teamspaceRepository = teamspaceRepository
        )
    }
}