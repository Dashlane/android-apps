package com.dashlane.xml.serializer

import org.xml.sax.SAXParseException

class XmlException(
    message: String? = null,
    cause: Throwable? = null,
    val xmlErrorType: String? = null,
) : Exception(message, cause)

internal fun SAXParseException.toXmlException(): XmlException {
    val (anonymousMessage, errorType) = anonymizeMessage()
    return XmlException(
        message = anonymousMessage,
        cause = anonymize(),
        xmlErrorType = errorType,
    )
}

private fun Throwable.anonymize(): XmlException {
    val obfuscatedThrowable = XmlException(javaClass.name, cause?.anonymize())
    obfuscatedThrowable.stackTrace = stackTrace
    return obfuscatedThrowable
}

private val xmlMessageRegex = """^At line ([0-9]+), column [0-9]+: (.*)$""".toRegex()

private val acceptableXmlErrorMessage =
    setOf(
        "out of memory",
        "syntax error",
        "no element found",
        "not well-formed (invalid token)",
        "unclosed token",
        "partial character",
        "mismatched tag",
        "duplicate attribute",
        "junk after document element",
        "illegal parameter entity reference",
        "undefined entity",
        "recursive entity reference",
        "asynchronous entity",
        "reference to invalid character number",
        "reference to binary entity",
        "reference to external entity in attribute",
        "XML or text declaration not at start of entity",
        "unknown encoding",
        "encoding specified in XML declaration is incorrect",
        "unclosed CDATA section",
        "error in processing external entity reference",
        "document is not standalone",
        "unexpected parser state - please send a bug report",
        "entity declared in parameter entity",
        "requested feature requires XML_DTD support in Expat",
        "cannot change setting once parsing has begun",
        "unbound prefix",
        "must not undeclare prefix",
        "incomplete markup in parameter entity",
        "XML declaration not well-formed",
        "text declaration not well-formed",
        "illegal character(s) in public id",
        "parser suspended",
        "parser not suspended",
        "parsing aborted",
        "parsing finished",
        "cannot suspend in external parameter entity",
        "reserved prefix (xml) must not be undeclared or bound to another namespace name",
        "reserved prefix (xmlns) must not be declared or undeclared",
        "prefix must not be bound to one of the reserved namespace names",
    )

private fun SAXParseException.anonymizeMessage(): Pair<String, String?> {
    val exceptionMessage = message ?: return "Empty Exception Message" to null

    return if (exceptionMessage in acceptableXmlErrorMessage) {
        
        exceptionMessage to exceptionMessage
    } else {
        
        val matchResult = xmlMessageRegex.find(exceptionMessage) ?: return "Unrecognized Exception Message" to null
        val matchResults = matchResult.groupValues

        val errorLine = matchResults[1]
        val errorMessage = matchResults[2]
        if (errorMessage !in acceptableXmlErrorMessage) {
            
            "Unknown error" to null
        } else {
            "Line $errorLine: $errorMessage" to errorMessage
        }
    }
}