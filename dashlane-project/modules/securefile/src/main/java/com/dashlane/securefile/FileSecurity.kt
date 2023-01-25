package com.dashlane.securefile



object FileSecurity {
    @Transient
    private val GENERIC_SUPPORTED_TYPES = arrayOf("image", "audio", "video", "text")

    @Transient
    private val SUPPORTED_TYPES = arrayOf(
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
        "application/msword", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
        "application/vnd.ms-excel", "application/vnd.ms-powerpoint",
        "application/vnd.openxmlformats-officedocument.presentationml.presentation", "application/pdf",
        "application/zip", "multipart/x-zip", "application/gzip", "application/tar", "application/x-tar",
        "application/tar+gzip", "application/x-gzip", "application/pkcs8", "application/pgp-keys",
        "application/vnd.oasis.opendocument.text", "application/vnd.oasis.opendocument.spreadsheet",
        "application/vnd.oasis.opendocument.presentation", "application/vnd.oasis.opendocument.graphics",
        "application/x-rar-compressed", "application/rar", "application/ogg",
        "application/x-7z-compressed", "application/x-sqlite3", "application/octet-stream", "application/unknown"
        
        
        
    )
    @Transient
    private val SUPPORTED_EXTENSIONS = arrayOf(
        "docx", "doc", "csv", "xlsx", "xls", "ppt", "pptx", "pdf", "txt", "zip",
        "key", "png", "jpg", "jpeg", "gif", "tiff", "bmp", "odt", "avi", "ogg", "m4a", "mov", "mp3", "mp4", "mpg",
        "wav", "wmv", "ods", "rar", "7z", "mkv", "raw", "db", "aac", "3gp", "webm", "flac", "gz", "tar", "azw3",
        "odp", "odg", "heic", "heif"
    )

    fun isSupportedType(filename: String?, mimeType: String?): Boolean {
        filename ?: return false
        mimeType ?: return false
        val type = mimeType.lowercase()
        val extensionIndex = filename.lastIndexOf(".")
        val extensionDelimiter = extensionIndex + 1
        if (extensionIndex == -1 || extensionDelimiter >= filename.length) {
            
            return true
        }
        val extension = filename.substring(extensionDelimiter, filename.length).lowercase()
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            
            return false
        }
        
        if (!SUPPORTED_TYPES.contains(type)) {
            
            GENERIC_SUPPORTED_TYPES.forEach {
                val mimeFirstPart = type.split("/")[0]
                if (it.contains(mimeFirstPart, true)) {
                    return true
                }
            }
            return false
        }
        return true
    }
}
