@file:JvmName("SecureNoteCategoryUtils")

package com.dashlane.vault.util

import android.content.Context
import com.dashlane.R
import com.dashlane.storage.userdata.accessor.DataSaver
import com.dashlane.storage.userdata.accessor.MainDataAccessor
import com.dashlane.storage.userdata.accessor.filter.genericFilter
import com.dashlane.vault.model.CommonDataIdentifierAttrsImpl
import com.dashlane.vault.model.SyncState
import com.dashlane.vault.model.VaultItem
import com.dashlane.vault.model.createSecureNoteCategory
import com.dashlane.xml.domain.SyncObject
import com.dashlane.xml.domain.SyncObjectType

object SecureNoteCategoryUtils {

    suspend fun createDefaultCategoriesIfNotExist(context: Context, mainDataAccessor: MainDataAccessor) {
        val authCategories = mainDataAccessor.getGenericDataQuery().queryAll(
            genericFilter {
            specificDataType(SyncObjectType.SECURE_NOTE_CATEGORY)
        }
        )
        if (authCategories.isEmpty()) {
            generateAndSaveDefaultNoteCategories(context, mainDataAccessor.getDataSaver())
        }
    }

    private suspend fun generateAndSaveDefaultNoteCategories(
        context: Context,
        dataSaver: DataSaver
    ) {
        val categories = createCategories(context)
        dataSaver.save(categories)
    }

    private fun createCategories(context: Context): List<VaultItem<SyncObject.SecureNoteCategory>> {
        return listOf(
            createCategory(context, R.string.application_passwords),
            createCategory(context, R.string.databases),
            createCategory(context, R.string.legal_documents),
            createCategory(context, R.string.memberships),
            createCategory(context, R.string.other),
            createCategory(context, R.string.personal),
            createCategory(context, R.string.personal_finance),
            createCategory(context, R.string.server),
            createCategory(context, R.string.softwarelicences),
            createCategory(context, R.string.wi_fi_passwords),
            createCategory(context, R.string.work_related)
        )
    }

    private fun createCategory(context: Context, title: Int): VaultItem<SyncObject.SecureNoteCategory> {
        return createSecureNoteCategory(
            dataIdentifier = CommonDataIdentifierAttrsImpl(syncState = SyncState.MODIFIED),
            title = context.getString(title)
        )
    }
}
