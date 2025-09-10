package com.dashlane.autofill.core

import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.autofill.util.formSourceIdentifier
import com.dashlane.hermes.LogRepository
import com.dashlane.hermes.Sha256Hash
import com.dashlane.hermes.generated.definitions.AutofillConfiguration
import com.dashlane.hermes.generated.definitions.AutofillDurationSetting
import com.dashlane.hermes.generated.definitions.AutofillScope
import com.dashlane.hermes.generated.definitions.DisableSetting
import com.dashlane.hermes.generated.definitions.DismissType
import com.dashlane.hermes.generated.definitions.Domain
import com.dashlane.hermes.generated.definitions.DomainType
import com.dashlane.hermes.generated.definitions.ItemType
import com.dashlane.hermes.generated.definitions.PhishingRisk
import com.dashlane.hermes.generated.definitions.WebcardSaveOptions
import com.dashlane.hermes.generated.events.anonymous.AutofillAcceptAnonymous
import com.dashlane.hermes.generated.events.anonymous.AutofillDismissAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.AutofillSetting
import com.dashlane.inappautofill.AutofillPhishingLogger
import javax.inject.Inject

class AutofillPhishingLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : AutofillPhishingLogger {
    override fun logSettingChanged(isEnabled: Boolean) {
        val disableSetting = if (isEnabled) {
            DisableSetting(
                durationSetting = AutofillDurationSetting.UNTIL_TURNED_BACK_OFF,
                configuration = AutofillConfiguration.PHISHING_ALERTS_ENABLED,
                scope = AutofillScope.GLOBAL,
            )
        } else {
            DisableSetting(
                durationSetting = AutofillDurationSetting.UNTIL_TURNED_BACK_ON,
                configuration = AutofillConfiguration.PHISHING_ALERTS_DISABLED,
                scope = AutofillScope.GLOBAL,
            )
        }
        logRepository.queueEvent(
            AutofillSetting(
                isBulk = false,
                disableSetting = disableSetting,
            ),
        )
    }

    override fun onAcceptAutoFillRisk(
        autoFillFormSource: AutoFillFormSource,
        phishingRisk: PhishingRisk,
    ) {
        logRepository.queueEvent(
            AutofillAccept(
                dataTypeList = listOf(ItemType.CREDENTIAL),
                webcardOptionSelected = WebcardSaveOptions.TRUST_AND_AUTOFILL,
                phishingRisk = phishingRisk,
            ),
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(
                webcardOptionSelected = WebcardSaveOptions.TRUST_AND_AUTOFILL,
                domain = autoFillFormSource.getDomainForLog(),
                phishingRisk = phishingRisk,
            ),
        )
    }

    override fun onDismissAutoFill(autoFillFormSource: AutoFillFormSource, trust: Boolean) {
        val dismissType = if (trust) {
            DismissType.TRUST
        } else {
            DismissType.DO_NOT_TRUST
        }

        logRepository.queueEvent(
            AutofillDismiss(
                dismissType = dismissType,
            ),
        )

        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = dismissType,
                domain = autoFillFormSource.getDomainForLog(),
                isNativeApp = autoFillFormSource.isNativeApp(),
            ),
        )
    }

    private fun AutoFillFormSource.getDomainType() = when (this) {
        is ApplicationFormSource -> DomainType.APP
        is WebDomainFormSource -> DomainType.WEB
    }

    private fun AutoFillFormSource.getDomainForLog(): Domain {
        return Domain(
            id = Sha256Hash.of(formSourceIdentifier),
            type = getDomainType(),
        )
    }

    private fun AutoFillFormSource.isNativeApp(): Boolean =
        this is ApplicationFormSource
}