package com.dashlane.xml.generator

import com.dashlane.xml.generator.templates.SyncObjectsTemplate
import com.dashlane.xml.generator.templates.toSyncObjectsTemplate
import com.dashlane.xml.model.TransactionDefinitions
import java.io.File

fun main(args: Array<String>) {
    val outputDirectoryPath = args.first()
    val outputDirectory =
        File(outputDirectoryPath).apply { if (!exists()) check(mkdirs()) { "$this couldn't be created" } }
    main(outputDirectory)
}

private fun main(outputDirectory: File) {
    val transactionDefinitions = TransactionDefinitions.load()
    val templateFactory = TemplateFactory()
    val syncObjectsTemplate = transactionDefinitions.toSyncObjectsTemplate()

    createSyncObjectTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createObjectConvertersTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createSyncObjectTypeTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createXmlNameTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createTransactionNameTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createConflictDuplicatesTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createSensitiveFieldsTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    createUserContentFieldsTemplate(outputDirectory, templateFactory, syncObjectsTemplate)
    val propertiesOutputDirectory =
        File(outputDirectory, "properties").apply { if (!exists()) check(mkdirs()) { "$this couldn't be created" } }
    createSyncObjectsXmlTemplate(propertiesOutputDirectory, templateFactory, syncObjectsTemplate)
}

private fun createSyncObjectTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncObject.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeObjectTemplate(template, it) }
}

private fun createObjectConvertersTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncObjectXmlConverters.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeObjectConvertersTemplate(template, it) }
}

private fun createSyncObjectTypeTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncObjectType.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeObjectTypeTemplate(template, it) }
}

private fun createXmlNameTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncObjectXmlName.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeXmlNameTemplate(template, it) }
}

private fun createTransactionNameTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncTransactionName.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeTransactionNameTemplate(template, it) }
}

private fun createConflictDuplicatesTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncConflictDuplicates.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeConflictDuplicatesTemplate(template, it) }
}

private fun createSensitiveFieldsTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncSensitiveFields.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeSensitiveFieldsTemplate(template, it) }
}

private fun createUserContentFieldsTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    val fileName = "SyncUserContentFields.kt"
    val file = File(outputDirectory, fileName)
    file.writer().use { templateFactory.writeUserContentFieldsTemplate(template, it) }
}

private fun createSyncObjectsXmlTemplate(
    outputDirectory: File,
    templateFactory: TemplateFactory,
    template: SyncObjectsTemplate,
) {
    (template.transactionTypes + template.baseType).forEach { transactionType ->
        val fileName = "${transactionType.className}Xml.kt"
        val file = File(outputDirectory, fileName)
        file.writer().use { templateFactory.writeXmlTemplate(transactionType, it) }
    }
}