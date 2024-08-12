package com.dashlane.item.v3.builders

import com.dashlane.item.v3.data.FormData
import com.dashlane.vault.summary.SummaryObject
import javax.inject.Inject
import javax.inject.Provider
import kotlin.reflect.KClass

class SpecializedFormBuilder @Inject constructor(
    private val credentialBuilder: Provider<CredentialBuilder>,
    private val secureNoteBuilder: Provider<SecureNoteBuilder>,
    private val creditCardBuilder: Provider<CreditCardBuilder>
) {
    fun get(clazz: KClass<out SummaryObject>): FormData.Builder = when (clazz) {
        SummaryObject.Authentifiant::class -> credentialBuilder.get()
        SummaryObject.SecureNote::class -> secureNoteBuilder.get()
        SummaryObject::PaymentCreditCard::class -> creditCardBuilder.get()
        else -> throw IllegalArgumentException("Builder not found for this SummaryObject type")
    }
}