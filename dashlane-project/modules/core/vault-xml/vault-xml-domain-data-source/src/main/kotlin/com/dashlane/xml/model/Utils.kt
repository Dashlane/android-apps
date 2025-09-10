package com.dashlane.xml.model

import org.json.JSONObject
import org.json.JSONTokener
import org.w3c.dom.Document
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory

fun getResourceDirectoryFiles(path: String): List<String> =
    openResourceStream(path).reader().use { it.readLines() }

internal fun getResourceLines(path: String): List<String> =
    openResourceStream(path).reader().readLines()

internal fun getResourceJson(path: String): JSONObject =
    openResourceStream(path).use(InputStream::toJson)

internal fun InputStream.toJson(): JSONObject =
    JSONObject(JSONTokener(this))

internal fun InputStream.toXml(): Document =
    DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(this)

internal fun openResourceStream(path: String): InputStream =
    checkNotNull(contextClassLoader.getResourceAsStream(path)) { "Unable to open resource $path" }

private val contextClassLoader: ClassLoader
    get() = Thread.currentThread().contextClassLoader