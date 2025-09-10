package com.dashlane.xml.model

import org.everit.json.schema.CombinedSchema
import org.everit.json.schema.ObjectSchema
import org.everit.json.schema.ReferenceSchema

data class TransactionDefinitions(
    val baseProperties: Map<String, TransactionType.Property>,
    val transactionTypes: Map<VaultModel, TransactionType>,
) {
    companion object {
        fun load(): TransactionDefinitions {
            val vaultModelRepository = VaultModelRepository.load()
            val schemas = vaultModelRepository.vaultModels.associate { it to vaultModelRepository.loadSchema(it) }
            val transactionTypes = schemas.mapValues { (_, schema) -> schema.toTransactionType() }
            val baseProperties = schemas.values.asSequence()
                .map {
                    val combinedSchema = (it as CombinedSchema).subschemas.filterIsInstance<CombinedSchema>().single()
                    check(CombinedSchema.ALL_CRITERION == combinedSchema.criterion)
                    val baseSchema =
                        (combinedSchema.subschemas.single() as ReferenceSchema).referredSchema as ObjectSchema
                    baseSchema.toPropertyMap()
                }
                .distinct() 
                .single()
            return TransactionDefinitions(
                baseProperties,
                transactionTypes,
            )
        }
    }
}