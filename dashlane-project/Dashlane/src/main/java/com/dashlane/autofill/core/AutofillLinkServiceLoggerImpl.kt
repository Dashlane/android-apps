package com.dashlane.autofill.core

import com.dashlane.autofill.api.rememberaccount.view.AutofillLinkServiceLogger
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.Action
import com.dashlane.hermes.generated.definitions.AnyPage
import com.dashlane.hermes.generated.definitions.BrowseComponent
import com.dashlane.hermes.generated.definitions.CallToAction.DO_NOT_LINK_WEBSITE
import com.dashlane.hermes.generated.definitions.CallToAction.LINK_WEBSITE
import com.dashlane.hermes.generated.definitions.Field
import com.dashlane.hermes.generated.definitions.ItemId
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.Space
import com.dashlane.hermes.generated.events.anonymous.UpdateCredentialAnonymous
import com.dashlane.hermes.generated.events.user.CallToAction
import com.dashlane.hermes.generated.events.user.UpdateVaultItem
import com.dashlane.useractivity.hermes.TrackingLogUtils
import javax.inject.Inject

class AutofillLinkServiceLoggerImpl @Inject constructor(
    private val logRepository: LogRepository
) : AutofillLinkServiceLogger {
    override fun logShowLinkPage() {
        logRepository.queuePageView(BrowseComponent.OS_AUTOFILL, AnyPage.AUTOFILL_NOTIFICATION_LINK_DOMAIN)
    }

    override fun logLinkServiceAccepted(
        itemId: String,
        space: Space,
        itemUrl: String?,
        autoFillFormSource: AutoFillFormSource
    ) {
        logRepository.queueEvent(
            CallToAction(false, listOf(LINK_WEBSITE, DO_NOT_LINK_WEBSITE), LINK_WEBSITE)
        )
        logRepository.queueEvent(
            UpdateVaultItem(
                action = Action.EDIT,
                fieldsEdited = listOf(Field.ASSOCIATED_WEBSITES_LIST),
                itemId = ItemId(id = itemId),
                itemType = ItemType.CREDENTIAL,
                space = space
            )
        )
        var linkedWebsite: String? = null
        var linkedApp: String? = null
        when (autoFillFormSource) {
            is ApplicationFormSource -> linkedApp = autoFillFormSource.packageName
            is WebDomainFormSource -> linkedWebsite = autoFillFormSource.webDomain
        }
        logRepository.queueEvent(
            UpdateCredentialAnonymous(
                action = Action.EDIT,
                fieldList = listOf(Field.ASSOCIATED_WEBSITES_LIST),
                domain = TrackingLogUtils.createWebDomainForLog(itemUrl.orEmpty()),
                space = space,
                associatedWebsitesAddedList = linkedWebsite?.let { listOf(Sha256Hash.of(it)) },
                associatedAppsAddedList = linkedApp?.let { listOf(Sha256Hash.of(it)) }
            )
        )
    }

    override fun logLinkServiceRefused() {
        logRepository.queueEvent(
            CallToAction(false, listOf(LINK_WEBSITE, DO_NOT_LINK_WEBSITE), DO_NOT_LINK_WEBSITE)
        )
    }

    override fun logLinkServiceCancel() {
        logRepository.queueEvent(CallToAction(hasChosenNoAction = true, callToActionList = null, chosenAction = null))
    }
}