package com.dashlane.xml.generator

import com.dashlane.xml.generator.templates.SyncObjectsTemplate
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.Writer

class TemplateFactory {
    private val objectTemplate: Template
        get() = configuration.getTemplate("SyncObject.kt.ftl")
    private val objectConvertersTemplate: Template
        get() = configuration.getTemplate("SyncObjectXmlConverters.kt.ftl")
    private val objectTypeTemplate: Template
        get() = configuration.getTemplate("SyncObjectType.kt.ftl")
    private val xmlNameTemplate: Template
        get() = configuration.getTemplate("SyncObjectXmlName.kt.ftl")
    private val transactionNameTemplate: Template
        get() = configuration.getTemplate("SyncTransactionName.kt.ftl")
    private val conflictDuplicatesTemplate: Template
        get() = configuration.getTemplate("SyncConflictDuplicates.kt.ftl")
    private val sensitiveFieldsTemplate: Template
        get() = configuration.getTemplate("SyncSensitiveFields.kt.ftl")
    private val userEditableFieldsTemplate: Template
        get() = configuration.getTemplate("SyncUserContentFields.kt.ftl")
    private val objectXmlTemplate: Template
        get() = configuration.getTemplate("SyncObjectXml.kt.ftl")

    private val configuration = Configuration(Configuration.VERSION_2_3_27).apply {
        defaultEncoding = "UTF-8"
        templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
        setClassLoaderForTemplateLoading(TemplateFactory::class.java.classLoader, "")
    }

    fun writeObjectTemplate(template: SyncObjectsTemplate, writer: Writer) {
        objectTemplate.process(template, writer)
    }

    fun writeObjectConvertersTemplate(template: SyncObjectsTemplate, writer: Writer) {
        objectConvertersTemplate.process(template, writer)
    }

    fun writeObjectTypeTemplate(template: SyncObjectsTemplate, writer: Writer) {
        objectTypeTemplate.process(template, writer)
    }

    fun writeXmlNameTemplate(template: SyncObjectsTemplate, writer: Writer) {
        xmlNameTemplate.process(template, writer)
    }

    fun writeTransactionNameTemplate(template: SyncObjectsTemplate, writer: Writer) {
        transactionNameTemplate.process(template, writer)
    }

    fun writeConflictDuplicatesTemplate(template: SyncObjectsTemplate, writer: Writer) {
        conflictDuplicatesTemplate.process(template, writer)
    }

    fun writeSensitiveFieldsTemplate(template: SyncObjectsTemplate, writer: Writer) {
        sensitiveFieldsTemplate.process(template, writer)
    }

    fun writeUserContentFieldsTemplate(template: SyncObjectsTemplate, writer: Writer) {
        userEditableFieldsTemplate.process(template, writer)
    }

    fun writeXmlTemplate(template: SyncObjectsTemplate.TransactionType, writer: Writer) {
        objectXmlTemplate.process(template, writer)
    }
}