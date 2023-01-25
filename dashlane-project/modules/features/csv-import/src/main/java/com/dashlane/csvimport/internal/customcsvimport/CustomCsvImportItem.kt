package com.dashlane.csvimport.internal.customcsvimport

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import com.dashlane.csvimport.CustomCsvImportActivity.Category
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_PASSWORD
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_URL
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_USERNAME
import com.dashlane.csvimport.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class CustomCsvImportItem(
    val id: Int,
    val title: String,
    @Category val category: Int?
) : DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<CustomCsvImportItem>,
    Parcelable {
    override fun getViewType() = VIEW_TYPE

    class ViewHolder(itemView: View) : EfficientViewHolder<CustomCsvImportItem>(itemView) {
        private val title = findViewByIdEfficient<TextView>(R.id.title)!!
        private val description = findViewByIdEfficient<TextView>(R.id.description)!!

        override fun updateView(context: Context, item: CustomCsvImportItem?) {
            title.text = item!!.title
            val descriptionResId = when (item.category) {
                CATEGORY_URL -> R.string.custom_csv_import_item_description_url
                CATEGORY_USERNAME -> R.string.custom_csv_import_item_description_username
                CATEGORY_PASSWORD -> R.string.custom_csv_import_item_description_password
                else -> R.string.custom_csv_import_item_description_none
            }

            description.setText(descriptionResId)
        }
    }

    override fun isItemTheSame(item: CustomCsvImportItem) = this.id == item.id

    override fun isContentTheSame(item: CustomCsvImportItem) = this == item

    companion object {
        private val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.list_item_custom_csv_import,
            ViewHolder::class.java
        )
    }
}