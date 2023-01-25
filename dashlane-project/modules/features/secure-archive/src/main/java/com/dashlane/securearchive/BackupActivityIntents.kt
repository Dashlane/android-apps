package com.dashlane.securearchive

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.SavedStateHandle

object BackupActivityIntents {

    internal const val EXTRA_STARTED_WITH = "started_with"
    internal const val EXTRA_IS_SUCCESSFUL = "is_successful"
    internal const val EXTRA_SHARED = "is_shared"
    internal const val EXTRA_COUNT = "count"

    private const val EXTRA_ACTION = "action"
    private const val EXTRA_URI = "uri"

    private const val ACTION_EXPORT = "export"
    private const val ACTION_IMPORT = "import"

    internal fun getBackupAction(savedStateHandle: SavedStateHandle): BackupOperation =
        when (val action = requireNotNull(savedStateHandle.get<String>(EXTRA_ACTION)) { "Required argument $EXTRA_ACTION is missing." }) {
            ACTION_EXPORT -> BackupOperation.Export
            ACTION_IMPORT -> BackupOperation.Import(uri = requireNotNull(savedStateHandle.get(EXTRA_URI)) { "Required argument $EXTRA_URI is missing." })
            else -> throw IllegalArgumentException("Unexpected action $action")
        }

    internal fun newData(
        startedWith: Intent,
        isSuccessful: Boolean,
        isShared: Boolean,
        count: Int
    ): Intent = Intent()
        .putExtra(EXTRA_STARTED_WITH, startedWith)
        .putExtra(EXTRA_IS_SUCCESSFUL, isSuccessful)
        .putExtra(EXTRA_SHARED, isShared)
        .putExtra(EXTRA_COUNT, count)

    internal fun newExportIntent(
        context: Context
    ) = newIntent(context, ACTION_EXPORT)

    internal fun newImportIntent(
        context: Context,
        uri: Uri
    ) = newIntent(context, ACTION_IMPORT, uri)

    private fun newIntent(
        context: Context,
        action: String,
        uri: Uri? = null
    ): Intent = Intent(context, BackupActivity::class.java)
        .putExtra(EXTRA_ACTION, action)
        .putExtra(EXTRA_URI, uri)
}