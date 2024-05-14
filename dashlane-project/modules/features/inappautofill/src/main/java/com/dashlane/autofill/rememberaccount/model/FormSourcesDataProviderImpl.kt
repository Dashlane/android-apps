package com.dashlane.autofill.rememberaccount.model

import com.dashlane.autofill.rememberaccount.AutofillApiRememberedAccountToaster
import com.dashlane.autofill.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.autofill.util.formSourceIdentifier
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class FormSourcesDataProviderImpl @Inject constructor(
    @Named("ApplicationDataLinked") private val applicationFormSourceLinker: FormSourceAuthentifiantLinker,
    @Named("WebDomainDataLinked") private val webDomainFormSourceLinker: FormSourceAuthentifiantLinker,
    private val autofillApiRememberedAccountToaster: AutofillApiRememberedAccountToaster
) : FormSourcesDataProvider {
    private val lock = Mutex()

    override suspend fun isLinked(formSource: AutoFillFormSource, authentifiantId: String): Boolean {
        return lock.withLock {
            formSource.getDataLinker().isLinked(formSource.formSourceIdentifier, authentifiantId)
        }
    }

    override suspend fun link(formSource: AutoFillFormSource, authentifiantId: String) {
        lock.withLock {
            formSource.getDataLinker().link(formSource.formSourceIdentifier, authentifiantId).let { isLinked ->
                if (isLinked) {
                    autofillApiRememberedAccountToaster.onAccountRemembered()
                }
            }
        }
    }

    override suspend fun unlink(formSource: AutoFillFormSource, authentifiantId: String) {
        lock.withLock {
            formSource.getDataLinker().unlink(formSource.formSourceIdentifier, authentifiantId)
        }
    }

    override suspend fun getAllLinkedFormSourceAuthentifiantIds(autofillFormSource: AutoFillFormSource): List<String> {
        val filterContent = autofillFormSource.formSourceIdentifier to autofillFormSource.getDataLinker().allLinked()
        return lock.withLock {
            filterContent.second
                .filter {
                    it.first == filterContent.first
                }
                .map {
                    it.second
                }
        }
    }

    private fun AutoFillFormSource.getDataLinker(): FormSourceAuthentifiantLinker =
        when (this) {
            is ApplicationFormSource -> applicationFormSourceLinker
            is WebDomainFormSource -> webDomainFormSourceLinker
        }
}
