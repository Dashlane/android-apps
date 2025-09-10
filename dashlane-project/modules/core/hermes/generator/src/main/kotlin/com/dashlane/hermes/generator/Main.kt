package com.dashlane.hermes.generator

import com.dashlane.hermes.generator.classdefinition.datamodel.toClass
import java.io.File

fun main(args: Array<String>) {
    val trackingFolder = File(args[0])
    val destinationFolder = File(args[1])

    val schemaReader = LocalSchemaReader()
    val definitions = schemaReader.readDefinitions(trackingFolder)

    val definitionsFolder = File(destinationFolder, "definitions").apply { mkdirs() }
    definitions.values.forEach { definition ->
        if (definition.type is Definition.Type.StringEnum) {
            val enumClass =
                definition.type.toClass(
                    id = definition.id,
                    documentation = definition.description,
                )
            val file = File(definitionsFolder, "${enumClass.className}.kt")
            file.writer().use { ClassProcessor.process(enumClass, it) }
        }
        if (definition.type is Definition.Type.Object) {
            val objectClass =
                definition.type.toClass(
                    id = definition.id,
                    definitions = definitions,
                    documentation = definition.description,
                    deprecated = definition.deprecated,
                )
            val file = File(definitionsFolder, "${objectClass.className}.kt")
            file.writer().use { ClassProcessor.process(objectClass, it) }
        }
        if (definition.type is Definition.Type.PageEnum) {
            val pageEnumClass =
                definition.type.toClass(
                    id = definition.id,
                    definitions = definitions,
                )
            val file = File(definitionsFolder, "${pageEnumClass.className}.kt")
            file.writer().use { ClassProcessor.process(pageEnumClass, it) }
        }
        if (definition.type is Definition.Type.ItemId) {
            val itemIdClass = definition.type.toClass(documentation = definition.description)
            val file = File(definitionsFolder, "ItemId.kt")
            file.writer().use { ClassProcessor.process(itemIdClass, it) }
        }
    }

    val events = schemaReader.readEvents(trackingFolder)
    val eventClasses = events.map { it.toClass(definitions) }

    val userEventsFolder = File(destinationFolder, "events/user").apply { mkdirs() }
    val anonEventsFolder = File(destinationFolder, "events/anonymous").apply { mkdirs() }

    eventClasses.forEach { eventClass ->
        val destFolder =
            if (eventClass.eventCategory == Event.Category.USER.name) {
                userEventsFolder
            } else {
                anonEventsFolder
            }
        val file = File(destFolder, "${eventClass.className}.kt")
        file.writer().use { ClassProcessor.process(eventClass, it) }
    }
}