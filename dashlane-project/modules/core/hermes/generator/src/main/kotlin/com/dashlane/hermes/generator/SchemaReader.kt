package com.dashlane.hermes.generator

import java.io.File
import java.net.URI

interface SchemaReader {
    fun readDefinitions(tracking: File): Map<URI, Definition>

    fun readEvents(tracking: File): List<Event>
}