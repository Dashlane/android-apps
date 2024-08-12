package com.dashlane.autofill.core

import com.dashlane.autofill.phishing.AutofillPhishingLogger
import com.dashlane.autofill.phishing.PhishingAttemptLevel
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
import com.dashlane.hermes.generated.events.anonymous.AutofillSuggestAnonymous
import com.dashlane.hermes.generated.events.user.AutofillAccept
import com.dashlane.hermes.generated.events.user.AutofillDismiss
import com.dashlane.hermes.generated.events.user.AutofillSetting
import com.dashlane.hermes.generated.events.user.AutofillSuggest
import javax.inject.Inject

class AutofillPhishingLoggerImpl @Inject constructor(
    private val logRepository: LogRepository,
) : AutofillPhishingLogger {
    override fun logSettingChanged(isEnabled: Boolean) {
        val disableSetting = if (isEnabled) {
            DisableSetting(
                durationSetting = AutofillDurationSetting.UNTIL_TURNED_BACK_OFF,
                configuration = AutofillConfiguration.PHISHING_ALERTS_ENABLED,
                scope = AutofillScope.GLOBAL
            )
        } else {
            DisableSetting(
                durationSetting = AutofillDurationSetting.UNTIL_TURNED_BACK_ON,
                configuration = AutofillConfiguration.PHISHING_ALERTS_DISABLED,
                scope = AutofillScope.GLOBAL

            )
        }
        logRepository.queueEvent(
            AutofillSetting(
                isBulk = false,
                disableSetting = disableSetting,
            )
        )
    }

    override fun onSuggestAutoFillRiskToNone(isNativeApp: Boolean, packageName: String) {
        val domainType = if (isNativeApp) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        val domain = Domain(
            id = Sha256Hash.of(packageName),
            type = domainType,
        )

        logRepository.queueEvent(
            AutofillSuggest(
                isNativeApp = isNativeApp,
                phishingRisk = PhishingRisk.NONE,
            )
        )
        logRepository.queueEvent(
            AutofillSuggestAnonymous(
                isNativeApp = isNativeApp,
                domain = domain,
                phishingRisk = PhishingRisk.NONE,
            )
        )
    }

    override fun onAcceptAutoFillRisk(
        isNativeApp: Boolean,
        packageName: String,
        phishingAttemptLevel: PhishingAttemptLevel,
    ) {
        val domainType = if (isNativeApp) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        val domain = Domain(
            id = Sha256Hash.of(packageName),
            type = domainType,
        )
        val phishingRisk = when (phishingAttemptLevel) {
            PhishingAttemptLevel.NONE -> PhishingRisk.NONE
            PhishingAttemptLevel.MODERATE -> PhishingRisk.MODERATE
            PhishingAttemptLevel.HIGH -> PhishingRisk.HIGH
        }

        logRepository.queueEvent(
            AutofillAccept(
                dataTypeList = listOf(ItemType.CREDENTIAL),
                webcardOptionSelected = WebcardSaveOptions.TRUST_AND_AUTOFILL,
                phishingRisk = phishingRisk,
            )
        )
        logRepository.queueEvent(
            AutofillAcceptAnonymous(
                webcardOptionSelected = WebcardSaveOptions.TRUST_AND_AUTOFILL,
                domain = domain,
                phishingRisk = phishingRisk,
            )
        )
    }

    override fun onDismissAutoFill(isNativeApp: Boolean, packageName: String, trust: Boolean) {
        val domainType = if (isNativeApp) {
            DomainType.APP
        } else {
            DomainType.WEB
        }
        val domain = Domain(
            id = Sha256Hash.of(packageName),
            type = domainType,
        )
        val dismissType = if (trust) {
            DismissType.TRUST
        } else {
            DismissType.DO_NOT_TRUST
        }

        logRepository.queueEvent(
            AutofillDismiss(
                dismissType = dismissType,
            )
        )
        logRepository.queueEvent(
            AutofillDismissAnonymous(
                dismissType = dismissType,
                domain = domain,
                isNativeApp = isNativeApp,
            )
        )
    }
}