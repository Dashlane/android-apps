package com.dashlane.core.sync

import com.dashlane.common.logger.DeveloperInfoLogger
import com.dashlane.common.logger.DeveloperLogAction
import com.dashlane.common.logger.DeveloperLogMessage
import com.dashlane.session.SessionManager
import com.dashlane.storage.userdata.accessor.CredentialDataQuery
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.credentialFilter
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.storage.userdata.accessor.filter.vaultFilter
import com.dashlane.util.stackTraceToSafeString
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.asVaultItemOfClassOrNull
import com.dashlane.vault.model.isNotSemanticallyNull
import com.dashlane.vault.model.isSemanticallyNull
import com.dashlane.vault.util.PAYPAL_URL
import com.dashlane.vault.util.toAuthentifiant
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import javax.inject.Inject

class PaymentPaypalMigrationHelper @Inject constructor(
    private val credentialDataQuery: CredentialDataQuery,
    private val genericDataQuery: GenericDataQuery,
    private val vaultDataQuery: VaultDataQuery,
    private val dataSaver: DataSaver,
    private val sessionManager: SessionManager,
    private val developerInfoLogger: DeveloperInfoLogger
) {
    suspend fun migratePaymentPaypal() {
        runCatching {
            migratePaymentPaypalInternal()
        }.onFailure {
            developerInfoLogger.log(
                username = sessionManager.session?.username,
                action = DeveloperLogAction.PAYPAL_MIGRATION,
                message = DeveloperLogMessage.FAILURE,
                exceptionType = it.stackTraceToSafeString()
            )
        }
    }

    private suspend fun migratePaymentPaypalInternal() {
        val filter = genericFilter {
            specificDataType(SyncObjectType.PAYMENT_PAYPAL)
        }
        val paymentIds = genericDataQuery.queryAll(filter).map { it.id }

        if (paymentIds.isEmpty()) return

        val credentialFilter = credentialFilter {
            forDomain(PAYPAL_URL)
        }
        val credentialIds = credentialDataQuery.queryAll(credentialFilter).map { it.id }
        val credentials = vaultDataQuery.queryAll(
            vaultFilter {
                specificUid(credentialIds)
            }
        ).mapNotNull { it.asVaultItemOfClassOrNull(SyncObject.Authentifiant::class.java) }

        val payments = vaultDataQuery.queryAll(
            vaultFilter {
                specificUid(paymentIds)
            }
        ).mapNotNull { it.asVaultItemOfClassOrNull(SyncObject.PaymentPaypal::class.java) }

        val result: List<VaultItem<SyncObject.Authentifiant>> = payments.mapNotNull { payment ->
            val login = payment.syncObject.login
            val password = payment.syncObject.password

            if (password.isNotSemanticallyNull()) {
                val match = credentials.find {
                    it.syncObject.password == password &&
                        (login.isSemanticallyNull() || it.syncObject.login == login)
                }
                if (match == null) payment else null
            } else {
                null
            }?.toAuthentifiant()
        }

        dataSaver.save(
            payments.map { it.copy(syncState = SyncState.DELETED) } + result
        )
        developerInfoLogger.log(
            username = sessionManager.session?.username,
            action = DeveloperLogAction.PAYPAL_MIGRATION,
            message = DeveloperLogMessage.SUCCESS
        )
    }
}