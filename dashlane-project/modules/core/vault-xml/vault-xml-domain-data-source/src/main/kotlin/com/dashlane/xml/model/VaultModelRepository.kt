package com.dashlane.xml.model

import org.everit.json.schema.FormatValidator
import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject
import java.io.File
import java.net.URI
import java.util.Base64
import java.util.Optional

class VaultModelRepository(
    private val modelsJsonMap: Map<String, JSONObject>,
    private val definitionsJsonMap: Map<String, JSONObject>,
) {
    val vaultModels: List<VaultModel>
        get() = modelsJsonMap.keys.map { VaultModel(it) }

    private val modelsJsonByUri
        get() = modelsJsonMap.mapKeys {
                (key, _) ->
            URI.create("https://dashlane.gitlab.dashlane.com/teams/code/dashlaneplatform/personal-data-model/schemas/vault/$key")
        }

    private val definitionsJsonByUri
        get() = definitionsJsonMap.mapKeys { (_, json) -> URI.create(json.getString("\$id")) }

    fun loadSchema(vaultModel: VaultModel): Schema =
        createSchemaLoaderBuilder()
            .addFormatValidator(BooleanFormatValidator)
            .addFormatValidator(MonthFormatValidator)
            .addFormatValidator(YearFormatValidator)
            .addFormatValidator(LocalDateFormatValidator)
            .addFormatValidator(Base64FormatValidator)
            .addFormatValidator(JsonFormatValidator)
            .addFormatValidator(CountryFormatValidator)
            .schemaJson(modelsJsonMap[vaultModel.name])
            .useDefaults(false)
            .build()
            .load()
            .build()

    private fun createSchemaLoaderBuilder() =
        (modelsJsonByUri + definitionsJsonByUri).toList()
            .fold(SchemaLoader.builder()) { builder, (uri, json) ->
                builder.registerSchemaByURI(
                    uri,
                    json,
                )
            }

    companion object {
        fun load(): VaultModelRepository {
            val models = getResourceDirectoryFiles("schemas/vault").filter { it.endsWith(".json") }
            val modelsJsonMap =
                models.associate { File(it).nameWithoutExtension to getResourceJson("schemas/vault/$it") }
                    .filterNot { (_, json) -> json.optBoolean("deprecated", false) }
            val coreDefinitions = getResourceDirectoryFiles("schemas/core")
            val coreDefinitionsJsonMap =
                coreDefinitions.associateWith { getResourceJson("schemas/core/$it") }
            val enumDefinitions = getResourceDirectoryFiles("schemas/enums")
            val enumDefinitionsJsonMap =
                enumDefinitions.associateWith { getResourceJson("schemas/enums/$it") }
            return VaultModelRepository(
                modelsJsonMap,
                coreDefinitionsJsonMap + enumDefinitionsJsonMap,
            )
        }

        fun loadJsonObject(jsonObject: JSONObject): VaultModelRepository {
            val coreDefinitions = getResourceDirectoryFiles("schemas/core")
            val coreDefinitionsJsonMap =
                coreDefinitions.associateWith { getResourceJson("schemas/core/$it") }
            val enumDefinitions = getResourceDirectoryFiles("schemas/enums")
            val enumDefinitionsJsonMap =
                enumDefinitions.associateWith { getResourceJson("schemas/enums/$it") }
            return VaultModelRepository(
                mapOf("CustomJsonObject" to jsonObject),
                coreDefinitionsJsonMap + enumDefinitionsJsonMap,
            )
        }
    }

    internal object BooleanFormatValidator : FormatValidator {
        override fun formatName(): String = "boolean"

        override fun validate(subject: String?): Optional<String> = Optional.empty()
    }

    internal object MonthFormatValidator : FormatValidator {
        override fun formatName(): String = "month"

        override fun validate(subject: String?): Optional<String> = Optional.empty()
    }

    internal object YearFormatValidator : FormatValidator {
        override fun formatName(): String = "year"

        override fun validate(subject: String?): Optional<String> = Optional.empty()
    }

    internal object LocalDateFormatValidator : FormatValidator {
        private val regex = Regex("""\d{4,10}-\d{1,2}-\d{1,2}""")

        override fun formatName(): String =
            "local-date"

        override fun validate(subject: String): Optional<String> =
            if (regex.matchEntire(subject) == null) Optional.of("$subject isn't a valid local date") else Optional.empty()
    }

    internal object Base64FormatValidator : FormatValidator {
        override fun formatName(): String =
            "base64"

        override fun validate(subject: String): Optional<String> =
            runCatching { Base64.getDecoder().decode(subject) }
                .fold(
                    onSuccess = { Optional.empty() },
                    onFailure = { Optional.of("Not a valid base64 string. ${it.message}") },
                )
    }

    internal object JsonFormatValidator : FormatValidator {
        override fun formatName(): String = "json"

        override fun validate(subject: String): Optional<String> = Optional.empty()
    }

    internal object CountryFormatValidator : FormatValidator {
        override fun formatName(): String = "country"

        override fun validate(subject: String): Optional<String> = Optional.empty()
    }
}