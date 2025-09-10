package com.dashlane.cryptography

internal object XmlArchiveTag {
    const val HEADER: String = "-------------------- Dashlane Secured Export ----------------------"
    const val ID_BEGIN: String = "--------------------        Id BEGIN         ----------------------"
    const val ID_END: String = "--------------------         Id END          ----------------------"
    const val DATA_BEGIN: String = "--------------------       Data BEGIN        ----------------------"
    const val DATA_END: String = "--------------------        Data END         ----------------------"
    const val FILE_BEGIN: String = "--------------------       Files BEGIN       ----------------------"
    const val FILE_END: String = "--------------------        Files END        ----------------------"
    const val ID_DELIMITER = ";"
}