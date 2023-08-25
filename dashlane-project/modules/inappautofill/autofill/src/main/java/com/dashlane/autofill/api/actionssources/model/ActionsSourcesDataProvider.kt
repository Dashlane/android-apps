package com.dashlane.autofill.api.actionssources.model

import com.dashlane.autofill.api.actionssources.AutofillActionsSourcesLogger
import com.dashlane.autofill.api.internal.ApplicationFormSourceDeviceStatus
import com.dashlane.autofill.api.internal.AutofillFormSourcesStrings
import com.dashlane.autofill.api.pause.services.RemovePauseContract
import com.dashlane.autofill.api.util.formSourceIdentifier
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.core.helpers.PackageSignatureStatus
import java.util.Locale
import javax.inject.Inject

class ActionsSourcesDataProvider @Inject constructor(
    private val removePauseContract: RemovePauseContract,
    private val autofillFormSourcesStrings: AutofillFormSourcesStrings,
    private val applicationFormSourceDeviceStatus: ApplicationFormSourceDeviceStatus,
    private val actionsSourcesLogger: AutofillActionsSourcesLogger
) {

    private val items: MutableList<ActionedFormSource> = mutableListOf()

    suspend fun loadFormSources(): Result<List<ActionedFormSource>> {
        items.clear()
        return try {
            val allFormSource = getAllFormSource()
            actionsSourcesLogger.showList(allFormSource)
            items.addAll(allFormSource)
            Result.success(allFormSource)
        } catch (e: Exception) {
            Result.failure(ActionsSourcesError.LoadAllFormSources)
        }
    }

    fun selectFormSourceItem(index: Int): AutoFillFormSource {
        val autoFillFormSourceList = items.takeIf {
            index >= 0 && index < it.size
        } ?: throw IllegalStateException("select over invalid list")

        val autoFillFormSource = autoFillFormSourceList[index].autoFillFormSource

        actionsSourcesLogger.clickItem(autoFillFormSource, autoFillFormSourceList.size)

        return autoFillFormSource
    }

    internal suspend fun getAllFormSource(): List<ActionedFormSource> {
        val pauseFormSources = removePauseContract.getAllPausedFormSources().map { it.autoFillFormSource }
        return pauseFormSources.map {
            val icon = it.getIcon()
            if (icon is ActionedFormSourceIcon.IncorrectSignatureIcon) {
                ActionedFormSource(it, it.formSourceIdentifier, it.getKind(), it.getIcon())
            } else {
                ActionedFormSource(it, it.getTitle(), it.getKind(), it.getIcon())
            }
        }.filterNot {
            it.icon is ActionedFormSourceIcon.NotInstalledApplicationIcon
        }.sortedBy { it.title.lowercase(Locale.getDefault()) }.toList()
    }

    private fun AutoFillFormSource.getTitle(): String {
        return autofillFormSourcesStrings.getAutoFillFormSourceString(this)
    }

    private fun AutoFillFormSource.getKind(): String {
        return autofillFormSourcesStrings.getAutoFillFormSourceTypeString(this)
    }

    private fun AutoFillFormSource.getIcon(): ActionedFormSourceIcon {
        return when (this) {
            is ApplicationFormSource -> this.getIcon()
            is WebDomainFormSource -> ActionedFormSourceIcon.UrlIcon(this.webDomain)
        }
    }

    private fun ApplicationFormSource.getIcon(): ActionedFormSourceIcon {
        val applicationInfo = applicationFormSourceDeviceStatus.getApplicationInfo(this)
            ?: return ActionedFormSourceIcon.NotInstalledApplicationIcon()

        val signatureAssessment = applicationFormSourceDeviceStatus.getSignatureAssessment(this)

        if (signatureAssessment == PackageSignatureStatus.INCORRECT) {
            return ActionedFormSourceIcon.IncorrectSignatureIcon
        }

        return ActionedFormSourceIcon.InstalledApplicationIcon(applicationInfo)
    }
}

sealed class ActionsSourcesError : Exception() {
    object AllSelectItem : ActionsSourcesError()
    object LoadAllFormSources : ActionsSourcesError()
}
