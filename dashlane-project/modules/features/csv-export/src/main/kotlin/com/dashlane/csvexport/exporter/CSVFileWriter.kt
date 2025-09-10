package com.dashlane.csvexport.exporter

import com.dashlane.csvexport.model.ExportData
import okio.buffer
import okio.sink
import okio.use
import java.io.OutputStream

class CSVFileWriter {
    private val encoder = CSVEncoder()

    fun write(file: OutputStream, dataList: List<ExportData>) {
        if (dataList.isEmpty()) return

        file.sink().buffer().use {
            it.writeUtf8(encoder.encodeRaw(dataList.first().keys.toList()))
            it.writeUtf8(System.lineSeparator())
            dataList.forEach { item ->
                it.writeUtf8(encoder.encodeRaw(item.values.toList()))
                it.writeUtf8(System.lineSeparator())
            }
        }
    }
}