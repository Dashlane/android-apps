package com.dashlane.securearchive

import android.net.Uri

sealed class BackupOperation {
    data class Import(val uri: Uri) : BackupOperation()
    object Export : BackupOperation()
}