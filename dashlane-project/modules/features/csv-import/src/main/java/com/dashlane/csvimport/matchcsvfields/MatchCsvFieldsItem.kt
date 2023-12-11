package com.dashlane.csvimport.matchcsvfields

import android.content.Context
import android.os.Parcelable
import android.view.View
import android.widget.TextView
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Category
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_PASSWORD
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_URL
import com.dashlane.csvimport.matchcsvfields.MatchCsvFieldsActivity.Companion.CATEGORY_USERNAME
import com.dashlane.csvimport.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import kotlinx.parcelize.Parcelize

@Parcelize
internal data class MatchCsvFieldsItem(
    val id: Int,
    val title: String,
    @Category val category: Int?
) : DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<MatchCsvFieldsItem>,
    Parcelable {
    override fun getViewType() = VIEW_TYPE

    class ViewHolder(itemView: View) : EfficientViewHolder<MatchCsvFieldsItem>(itemView) {
        private val title = findViewByIdEfficient<TextView>(R.id.title)!!
        private val description = findViewByIdEfficient<TextView>(R.id.description)!!

        override fun updateView(context: Context, item: MatchCsvFieldsItem?) {
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

    override fun isItemTheSame(item: MatchCsvFieldsItem) = this.id == item.id

    override fun isContentTheSame(item: MatchCsvFieldsItem) = this == item

    companion object {
        private val VIEW_TYPE = DashlaneRecyclerAdapter.ViewType(
            R.layout.list_item_custom_csv_import,
            ViewHolder::class.java
        )
    }
}