package com.dashlane.ui.activities.debug

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.permission.PermissionsManager
import com.dashlane.storage.userdata.accessor.VaultDataQuery
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.util.FileUtils
import com.dashlane.util.showToaster
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import dagger.hilt.android.qualifiers.ActivityContext
import javax.inject.Inject
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

internal class DebugCategorySync @Inject constructor(
    @ActivityContext override val context: Context,
    val permissionsManager: PermissionsManager,
    private val vaultDataQuery: VaultDataQuery,
    private val fileUtils: FileUtils
) : AbstractDebugCategory() {

    override val name: String
        get() = "Sync"

    override fun addSubItems(group: PreferenceGroup) {
        addPreferenceButton(group, "View Data Change History") {
            showDataChangeHistory()
            true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun showDataChangeHistory() {
        if (filePermissionsMissing()) {
            return
        }

        val list = vaultDataQuery
            .queryAll(VaultFilter(dataTypeFilter = SpecificDataTypeFilter(SyncObjectType.DATA_CHANGE_HISTORY)))
            .mapNotNull { it.syncObject as? SyncObject.DataChangeHistory }
        val html = list.joinToString("\n") { changeHistory -> changeHistory.html() }

        GlobalScope.launch(Dispatchers.Main) {
            val uri = fileUtils.writeFileToPublicFolder(
                "data_history.html",
                "text/html"
            ) { stream ->
                stream.writer().use { writer -> writer.write(html) }
            }

            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
            }
            runCatching { context.startActivity(browserIntent) }
                .onFailure {
                    context.showToaster(
                        "${it::class.java.simpleName} - ${it.message}",
                        Toast.LENGTH_SHORT
                    )
                }
        }
    }

    @SuppressLint("NewApi")
    private fun filePermissionsMissing(): Boolean {
        context as Activity
        val denied = !permissionsManager.isAllowedToWriteToPublicFolder()
        if (denied) {
            context.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
        }
        return denied
    }

    private fun SyncObject.DataChangeHistory.html(): String =
        """
<!DOCTYPE html>
<html>
<body>
<h2>$objectTitle</h2>
${changeSets?.joinToString("\n") { it.html() }}
</body>
</html>
            """.trim()

    private fun SyncObject.DataChangeHistory.ChangeSet.html() =
        """
<h3>$id<br>$modificationDate $platform</h3>
<ul>
${currentData?.toList()?.joinToString("\n") { (key, value) -> "<li>$key: $value</li>" }}
</ul>
            """.trim()
}
