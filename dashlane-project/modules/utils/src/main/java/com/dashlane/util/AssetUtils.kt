package com.dashlane.util

import android.content.res.AssetManager
import okio.buffer
import okio.sink
import okio.source
import java.io.File
import java.io.IOException
import java.util.zip.ZipInputStream



@Throws(IOException::class)
fun AssetManager.readString(asset: String) =
    open(asset).use { it.reader().readText() }



@Throws(IOException::class)
fun AssetManager.unzip(asset: String, destination: File) {
    if (!destination.exists()) destination.mkdirs()
    ZipInputStream(open(asset)).use { zis ->
        
        
        
        
        
        var entry = zis.nextEntry
        while (entry != null) {
            if (entry.isDirectory) {
                val file = File(destination, entry.name)
                if (!file.exists()) file.mkdirs()
            } else {
                File(destination, entry.name).sink().use { sink ->
                    zis.source().buffer().readAll(sink)
                }
            }

            entry = zis.nextEntry
        }
    }
}