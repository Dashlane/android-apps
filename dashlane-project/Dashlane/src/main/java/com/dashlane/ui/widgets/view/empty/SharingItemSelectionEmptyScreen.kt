package com.dashlane.ui.widgets.view.empty

import android.content.Context
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat
import com.dashlane.R
import com.dashlane.xml.domain.SyncObjectType

object SharingItemSelectionEmptyScreen {
    fun newInstance(
        context: Context,
        dataType: SyncObjectType,
        withSearchFilter: Boolean
    ): EmptyScreenViewProvider {
        val iconResId: Int
        val line2ResId: Int
        when (dataType) {
            SyncObjectType.AUTHENTIFIANT -> {
                iconResId = R.drawable.ic_empty_password
                line2ResId = if (withSearchFilter) {
                    R.string.list_sharing_emergency_item_selection_no_password_matching_filter
                } else {
                    R.string.list_sharing_emergency_item_selection_empty_password
                }
            }
            SyncObjectType.SECURE_NOTE -> {
                iconResId = R.drawable.ic_empty_secure_note
                line2ResId = if (withSearchFilter) {
                    R.string.list_sharing_emergency_item_selection_no_securenote_matching_filter
                } else {
                    R.string.list_sharing_emergency_item_selection_empty_securenote
                }
            }
            else -> throw IllegalArgumentException("Unsupported type for empty screen")
        }
        return EmptyScreenViewProvider(
            EmptyScreenConfiguration.Builder()
                .setImage(VectorDrawableCompat.create(context.resources, iconResId, null))
                .setLine2(context.getString(line2ResId))
                .build()
        )
    }
}
