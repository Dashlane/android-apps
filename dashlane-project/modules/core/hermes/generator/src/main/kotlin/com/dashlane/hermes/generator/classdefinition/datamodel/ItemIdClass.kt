package com.dashlane.hermes.generator.classdefinition.datamodel

import com.dashlane.hermes.generator.Definition

data class ItemIdClass(
    val imports: List<String>,
    val documentation: String,
)

fun Definition.Type.ItemId.toClass(documentation: String) =
    ItemIdClass(
        imports =
            listOf(
                "com.dashlane.hermes.TrackingLog",
            ),
        documentation = documentation,
    )