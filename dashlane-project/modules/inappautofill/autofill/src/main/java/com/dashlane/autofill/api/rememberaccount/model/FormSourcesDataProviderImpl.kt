package com.dashlane.autofill.api.rememberaccount.model

import com.dashlane.autofill.api.rememberaccount.AutofillApiRememberedAccountToaster
import com.dashlane.autofill.api.rememberaccount.services.FormSourceAuthentifiantLinker
import com.dashlane.autofill.api.util.formSourceIdentifier
import com.dashlane.autofill.formdetector.model.ApplicationFormSource
import com.dashlane.autofill.formdetector.model.AutoFillFormSource
import com.dashlane.autofill.formdetector.model.WebDomainFormSource
import com.dashlane.util.userfeatures.UserFeaturesChecker
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton



@Singleton
class FormSourcesDataProviderImpl @Inject constructor(
    @Named("ApplicationLinkedPreference") private val applicationFormSourcePrefLinker: FormSourceAuthentifiantLinker,
    @Named("WebDomainLinkedPreference") private val webDomainFormSourcePrefLinker: FormSourceAuthentifiantLinker,
    @Named("ApplicationDataLinked") private val applicationFormSourceLinker: FormSourceAuthentifiantLinker,
    @Named("WebDomainDataLinked") private val webDomainFormSourceLinker: FormSourceAuthentifiantLinker,
    private val autofillApiRememberedAccountToaster: AutofillApiRememberedAccountToaster,
    private val userFeaturesChecker: UserFeaturesChecker
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

    override suspend fun getAllLinkedFormSources(): List<RememberedFormSource> {
        return lock.withLock {
            val app = applicationFormSourcePrefLinker.allLinked()
                .map {
                    RememberedFormSource(ApplicationFormSource(it.first), it.second)
                }
            val web = webDomainFormSourcePrefLinker.allLinked()
                .map {
                    RememberedFormSource(WebDomainFormSource("", it.first), it.second)
                }
            app + web
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

    private fun AutoFillFormSource.getDataLinker(): FormSourceAuthentifiantLinker {
        return if (userFeaturesChecker.has(UserFeaturesChecker.FeatureFlip.LINKED_WEBSITES_IN_CONTEXT)) {
            when (this) {
                is ApplicationFormSource -> applicationFormSourceLinker
                is WebDomainFormSource -> webDomainFormSourceLinker
            }
        } else {
            when (this) {
                is ApplicationFormSource -> applicationFormSourcePrefLinker
                is WebDomainFormSource -> webDomainFormSourcePrefLinker
            }
        }
    }
}
