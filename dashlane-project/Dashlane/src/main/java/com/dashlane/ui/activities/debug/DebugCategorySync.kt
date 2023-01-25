package com.dashlane.ui.activities.debug

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.widget.Toast
import androidx.preference.PreferenceGroup
import com.dashlane.dagger.singleton.SingletonProvider
import com.dashlane.storage.userdata.accessor.filter.VaultFilter
import com.dashlane.storage.userdata.accessor.filter.datatype.SpecificDataTypeFilter
import com.dashlane.util.FileUtils
import com.dashlane.util.showToaster
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch



internal class DebugCategorySync(debugActivity: Activity) : AbstractDebugCategory(debugActivity) {

    override fun getName(): String = "Sync"

    override fun addSubItems(group: PreferenceGroup?) {
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

        val list = SingletonProvider.getMainDataAccessor()
            .getVaultDataQuery()
            .queryAll(VaultFilter(dataTypeFilter = SpecificDataTypeFilter(SyncObjectType.DATA_CHANGE_HISTORY)))
            .mapNotNull { it.syncObject as? SyncObject.DataChangeHistory }
        val html = list.joinToString("\n") { changeHistory -> changeHistory.html() }

        GlobalScope.launch(Dispatchers.Main) {
            val uri = FileUtils.writeFileToPublicFolder(debugActivity, "data_history.html", "text/html") { stream ->
                stream.writer().use { writer -> writer.write(html) }
            }

            val browserIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "text/html")
            }
            runCatching { debugActivity.startActivity(browserIntent) }
                .onFailure {
                    debugActivity.showToaster("${it::class.java.simpleName} - ${it.message}", Toast.LENGTH_SHORT)
                }
        }
    }

    @SuppressLint("NewApi")
    private fun filePermissionsMissing(): Boolean {
        val denied = !SingletonProvider.getPermissionsManager().isAllowedToWriteToPublicFolder()
        if (denied) {
            debugActivity.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
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
