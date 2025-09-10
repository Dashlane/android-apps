package com.dashlane.hermes.generator

import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import org.json.JSONTokener
import java.io.File
import java.net.URI

class LocalSchemaReader : SchemaReader {
    override fun readDefinitions(tracking: File): Map<URI, Definition> {
        val definitionsFolder = File(tracking, "definitions")
        val definitionFiles =
            definitionsFolder.walk().filter(File::isFile).filter { it.isJSON }.toList()
        return definitionFiles.fold(mapOf()) { acc, it ->
            val fileContent = it.readJSONObject()
            acc +
                fileContent.definitions(it)
                    .asMapOfStringToJSONObject()
                    .map { (key, definitionJSON) ->
                        try {
                            val fileId = fileContent.id(it)
                            val id = "$fileId#/definitions/$key"
                            readDefinition(definitionJSON, id)
                        } catch (e: JSONException) {
                            throw Exception("Unable to read $it#/definitions/$key.", e)
                        }
                    }
                    .associateBy { it.id }
        }
    }

    private fun readDefinition(
        jsonObject: JSONObject,
        id: String,
    ): Definition {
        val description =
            try {
                jsonObject.getString("description")
            } catch (e: JSONException) {
                "No description"
            }

        
        try {
            return readDefinition(jsonObject, id, description)
        } catch (e: JSONException) {
            println("Not a classic definition: $e")
        }

        
        try {
            return readAnyPage(jsonObject, id, description)
        } catch (e: JSONException) {
            println("Not AnyPage")
        }

        
        try {
            val ref = (jsonObject).ref(jsonObject)
            return Definition(
                id = URI(id),
                description = description,
                type =
                    Definition.Type.DirectReference(
                        ref = URI(ref),
                    ),
                deprecated = jsonObject.optBoolean("deprecated", false),
            )
        } catch (e: JSONException) {
            error("Does not recognize Definition $jsonObject")
        }
    }

    private fun readDefinition(
        jsonObject: JSONObject,
        id: String,
        description: String,
    ): Definition {
        val type =
            when (jsonObject.getString("type")) {
                "boolean" -> Definition.Type.Boolean
                "number" -> Definition.Type.Double
                "integer" -> Definition.Type.Int
                "string" -> {
                    val schemaEnum = jsonObject.optJSONArray("enum")
                    val schemaFormat = jsonObject.optString("format")
                    when {
                        schemaFormat == "uuid" ->
                            Definition.Type.Uuid
                        schemaFormat == "date" ->
                            Definition.Type.Date
                        schemaFormat == "date-time" ->
                            Definition.Type.DateTime
                        schemaFormat == "sha256" ->
                            Definition.Type.Sha256
                        schemaFormat == "item_id" ->
                            Definition.Type.ItemId
                        !schemaFormat.isNullOrEmpty() ->
                            error("Unsupported string format $schemaFormat")
                        schemaEnum != null ->
                            Definition.Type.StringEnum(schemaEnum.asStringSequence().toList())
                        else ->
                            Definition.Type.String
                    }
                }
                "array" -> {
                    val itemsSchema = jsonObject.getJSONObject("items").ref(jsonObject)
                    Definition.Type.List(URI(itemsSchema))
                }
                "object" -> {
                    val schemaProperties = jsonObject.getJSONObject("properties")
                    val schemaRequiredProperties =
                        jsonObject.optJSONArray("required")?.asStringSequence() ?: emptySequence()
                    val properties =
                        schemaProperties.asMapOfStringToJSONObject().toMap()
                            .mapValues { (key, schemaProperty) ->
                                Definition.Type.Object.Property(
                                    URI(schemaProperty.ref(jsonObject)),
                                    schemaRequiredProperties.contains(key),
                                )
                            }
                    Definition.Type.Object(properties)
                }
                else ->
                    error("Unsupported definition $jsonObject")
            }
        return Definition(
            id = URI(id),
            description = description,
            type = type,
            deprecated = jsonObject.optBoolean("deprecated", false),
        )
    }

    private fun readAnyPage(
        jsonObject: JSONObject,
        id: String,
        description: String,
    ): Definition {
        val oneOf = jsonObject.getJSONArray("oneOf")
        var pageRef = ""
        var priorityPageRef = ""
        oneOf.forEach { schemaProperty ->
            val ref = (schemaProperty as JSONObject).ref(jsonObject)
            when {
                ref.contains("/definitions/page") -> {
                    pageRef = ref
                }
                ref.contains("/definitions/priority_page") -> {
                    priorityPageRef = ref
                }
                else -> {
                    error("Unknown page reference in $jsonObject")
                }
            }
        }
        return Definition(
            id = URI(id),
            description = description,
            type =
                Definition.Type.PageEnum(
                    pagesRef = URI(pageRef),
                    priorityPagesRef = URI(priorityPageRef),
                ),
            deprecated = jsonObject.optBoolean("deprecated", false),
        )
    }

    override fun readEvents(tracking: File): List<Event> {
        val eventsFolder = File(tracking, "events")
        return readEvents(eventsFolder, Event.Category.USER) +
            readEvents(
                eventsFolder,
                Event.Category.ANONYMOUS,
            )
    }

    private fun readEvents(
        eventsFolder: File,
        category: Event.Category,
    ): List<Event> {
        val categoryEventsFolder = File(eventsFolder, category.folderName)
        val propertiesFolder = File(categoryEventsFolder, "properties")
        val eventFiles = propertiesFolder.walk().filter(File::isFile).filter { it.isJSON }.toList()
        val eventJSONs = eventFiles.map { it.readJSONObject() }
        return eventJSONs.map { readEvent(it, category) }
    }

    private fun readEvent(
        jsonObject: JSONObject,
        category: Event.Category,
    ): Event {
        if (jsonObject.getJSONObject("properties").getJSONObject("name")
                .getString("const") == "autofill_password_generate_show_more"
        ) {
            println(jsonObject)
            println("deprecated: ${jsonObject.optBoolean("deprecated", false)}")
        }
        return Event(
            id = URI(jsonObject.id(jsonObject)),
            description = jsonObject.getString("description"),
            type = jsonObject.getJSONObject("properties").getJSONObject("name").getString("const"),
            properties =
                (
                    jsonObject.getJSONObject("properties")
                        .asMapOfStringToJSONObject() - "name"
                ).mapValues { (key, propertyJSON) ->
                    Event.Property(
                        id = URI(propertyJSON.ref(jsonObject)),
                        required = jsonObject.optJSONArray("required")?.contains(key) ?: false,
                    )
                },
            category = category,
            priority = jsonObject.priority(),
            deprecated = jsonObject.optBoolean("deprecated", false),
        )
    }

    private fun JSONObject.id(context: Any) =
        try {
            getString("\$id").cleanBasePath()
        } catch (e: JSONException) {
            throw Exception("Id not found for $context.", e)
        }

    private fun JSONObject.ref(context: Any) =
        try {
            getString("\$ref").cleanBasePath()
        } catch (e: JSONException) {
            throw Exception("Ref not found for $context.", e)
        }

    private fun JSONObject.priority() = optJSONObject("metas").optBoolean("priority", false)

    private fun JSONObject.definitions(context: Any) =
        try {
            getJSONObject("definitions")
        } catch (e: JSONException) {
            throw Exception("Definitions not found for $context.", e)
        }

    private fun JSONObject.asMapOfStringToJSONObject(): Map<String, JSONObject> = asMapOfStringTo(JSONObject::getJSONObject)

    private inline fun <T> JSONObject.asMapOfStringTo(block: JSONObject.(String) -> T): Map<String, T> =
        keySet().associateWith { block(it) }

    private fun JSONArray.asStringSequence(): Sequence<String> = asSequence(JSONArray::getString)

    private inline fun <T> JSONArray.asSequence(crossinline block: JSONArray.(Int) -> T): Sequence<T> =
        indices.asSequence().map { block(it) }

    private val JSONArray.indices
        get() = 0 until length()

    private val File.isJSON
        get() = extension == "json"

    private fun File.readJSONObject() = reader().use { JSONObject(JSONTokener(it)) }

    private fun String.cleanBasePath() = this.replaceFirst("\${base_path}/", "")
}