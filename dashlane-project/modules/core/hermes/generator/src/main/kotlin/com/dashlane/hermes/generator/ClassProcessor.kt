package com.dashlane.hermes.generator

import com.dashlane.hermes.generator.classdefinition.datamodel.EnumClass
import com.dashlane.hermes.generator.classdefinition.datamodel.EventClass
import com.dashlane.hermes.generator.classdefinition.datamodel.ItemIdClass
import com.dashlane.hermes.generator.classdefinition.datamodel.ObjectClass
import com.dashlane.hermes.generator.classdefinition.datamodel.PageEnumClass
import freemarker.template.Configuration
import freemarker.template.Template
import freemarker.template.TemplateExceptionHandler
import java.io.Writer

object ClassProcessor {
    private val eventTemplate: Template
    private val enumTemplate: Template
    private val pageEnumTemplate: Template
    private val objectTemplate: Template
    private val itemIdTemplate: Template

    init {
        val templateConfiguration =
            Configuration(Configuration.VERSION_2_3_27).apply {
                defaultEncoding = "UTF-8"
                templateExceptionHandler = TemplateExceptionHandler.RETHROW_HANDLER
                setClassLoaderForTemplateLoading(ClassProcessor::class.java.classLoader, "")
            }
        eventTemplate = templateConfiguration.getTemplate("Event.kt.ftl")
        enumTemplate = templateConfiguration.getTemplate("Enum.kt.ftl")
        pageEnumTemplate = templateConfiguration.getTemplate("PageEnum.kt.ftl")
        objectTemplate = templateConfiguration.getTemplate("Object.kt.ftl")
        itemIdTemplate = templateConfiguration.getTemplate("ItemId.kt.ftl")
    }

    fun process(
        eventClass: EventClass,
        writer: Writer,
    ) {
        eventTemplate.process(eventClass, writer)
    }

    fun process(
        enumClass: EnumClass,
        writer: Writer,
    ) {
        enumTemplate.process(enumClass, writer)
    }

    fun process(
        pageEnumClass: PageEnumClass,
        writer: Writer,
    ) {
        pageEnumTemplate.process(pageEnumClass, writer)
    }

    fun process(
        objectClass: ObjectClass,
        writer: Writer,
    ) {
        objectTemplate.process(objectClass, writer)
    }

    fun process(
        itemIdClass: ItemIdClass,
        writer: Writer,
    ) {
        itemIdTemplate.process(itemIdClass, writer)
    }
}