package com.dashlane.csvimport.internal

import java.io.Reader

internal fun Reader.csvLineSequence(
    separator: Char = ',',
    quoteChar: Char = '"'
): Sequence<List<String>> = CsvLinesSequence(
    ReadSequence(buffered()),
    separator,
    quoteChar
).constrainOnce()

private class CsvLinesSequence(
    private val readSequence: ReadSequence,
    private val separator: Char,
    private val quoteChar: Char
) : Sequence<List<String>> {
    private val quoteString = quoteChar.toString()

    override fun iterator() = object : Iterator<List<String>> {
        private val readIterator = readSequence.iterator()
        private val builder = StringBuilder()

        override fun hasNext() = readIterator.hasNext()

        override fun next(): List<String> {
            if (!hasNext()) throw NoSuchElementException()

            builder.clear()
            return readIterator.nextLine().map { it.unQuote() }
        }

        private fun Iterator<Char>.nextLine(): List<String> {
            val results = mutableListOf<String>()
            var enclosed = false

            for (c in this) {
                
                if (!enclosed && c == '\n') {
                    break
                }

                if (!enclosed && c == '\r') {
                    continue
                }

                
                if (!enclosed && c == separator) {
                    results += builder.toString()
                    builder.clear()
                    continue
                }

                builder.append(c)

                
                if (c == quoteChar) {
                    enclosed = !enclosed
                }
            }

            results += builder.toString()
            return results
        }

        private fun String.unQuote(): String {
            val trimmed = trim()

            return if (trimmed.startsWith(quoteString)) {
                if (!trimmed.endsWith(quoteString)) {
                    throw Exception("Missing closing quote: $quoteString")
                }
                
                trimmed.removeSurrounding(quoteString)
                    .replace("$quoteString$quoteString", quoteString)
            } else {
                this
            }
        }
    }
}

private class ReadSequence(
    private val reader: Reader
) : Sequence<Char> {
    override fun iterator() = object : Iterator<Char> {
        private var nextRead: Int = -1
        private var done = false

        override fun hasNext(): Boolean {
            if (nextRead == -1 && !done) {
                nextRead = reader.read()
                if (nextRead == -1) {
                    done = true
                }
            }

            return nextRead != -1
        }

        override fun next(): Char {
            if (!hasNext()) throw NoSuchElementException()

            val result = nextRead
            nextRead = -1
            return result.toChar()
        }
    }
}