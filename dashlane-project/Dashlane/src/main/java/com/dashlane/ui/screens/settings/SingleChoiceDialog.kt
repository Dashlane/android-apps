package com.dashlane.ui.screens.settings

import android.content.Context
import android.widget.ArrayAdapter
import androidx.annotation.ArrayRes
import androidx.annotation.StringRes
import com.dashlane.R
import com.dashlane.ui.util.DialogHelper

class SingleChoiceDialog(private val dialogHelper: DialogHelper) {

    fun show(
        context: Context,
        @StringRes title: Int,
        @ArrayRes items: Int,
        selectedItem: Int,
        onSelect: (Int) -> Unit
    ) {
        val adapter = ArrayAdapter<CharSequence>(
            context,
            R.layout.simple_list_item_single_choice,
            context.resources.getTextArray(items)
        )
        dialogHelper.builder(context)
            .setTitle(title)
            .setSingleChoiceItems(adapter, selectedItem) { dialogInterface, which ->
                dialogInterface.dismiss()
                onSelect.invoke(which)
            }
            .setCancelable(true)
            .show()
    }
}