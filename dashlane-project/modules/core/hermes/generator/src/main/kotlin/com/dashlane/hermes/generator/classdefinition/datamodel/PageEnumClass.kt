package com.dashlane.hermes.generator.classdefinition.datamodel

import com.dashlane.hermes.generator.Definition
import com.dashlane.hermes.generator.className
import com.dashlane.hermes.generator.slashToSnakeCase
import java.net.URI
import java.util.Locale

data class PageEnumClass(
    val className: String,
    val pagesEntries: List<Entry>,
    val priorityPagesEntries: List<Entry>,
) {
    data class Entry(
        val name: String,
        val code: String,
    )
}

fun Definition.Type.PageEnum.toClass(
    id: URI,
    definitions: Map<URI, Definition>,
): PageEnumClass {
    val pageEnum = definitions[pagesRef]!!.type as Definition.Type.StringEnum
    val priorityPageEnum = definitions[priorityPagesRef]!!.type as Definition.Type.StringEnum

    return PageEnumClass(
        className = id.className,
        pagesEntries =
            pageEnum.values.map {
                PageEnumClass.Entry(it.slashToSnakeCase().uppercase(Locale.US), it)
            },
        priorityPagesEntries =
            priorityPageEnum.values.map {
                PageEnumClass.Entry(it.slashToSnakeCase().uppercase(Locale.US), it)
            },
    )
}