package com.dashlane.hermes.generator.classdefinition.datamodel

import com.dashlane.hermes.generator.Definition
import com.dashlane.hermes.generator.className
import com.dashlane.hermes.generator.slashToSnakeCase
import java.net.URI
import java.util.Locale

data class EnumClass(
    val documentation: String,
    val className: String,
    val entries: List<Entry>,
) {
    data class Entry(
        val name: String,
        val code: String,
    )
}

fun Definition.Type.StringEnum.toClass(
    id: URI,
    documentation: String,
) = EnumClass(
    documentation = documentation,
    className = id.className,
    entries = values.map { EnumClass.Entry(it.slashToSnakeCase().uppercase(Locale.US), it) },
)