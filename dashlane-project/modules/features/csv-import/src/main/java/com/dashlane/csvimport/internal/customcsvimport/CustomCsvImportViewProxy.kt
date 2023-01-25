package com.dashlane.csvimport.internal.customcsvimport

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_NONE
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_PASSWORD
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_URL
import com.dashlane.csvimport.CustomCsvImportActivity.Companion.CATEGORY_USERNAME
import com.dashlane.csvimport.R
import com.dashlane.csvimport.internal.OnSwipeTouchListener
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.util.DialogHelper
import com.dashlane.util.showToaster
import com.rd.PageIndicatorView
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch

internal class CustomCsvImportViewProxy(
    private val activity: AppCompatActivity,
    private val viewModel: CustomCsvImportViewModelContract
) {
    private val rvAdapter = DashlaneRecyclerAdapter<CustomCsvImportItem>().apply {
        onItemClickListener = EfficientAdapter.OnItemClickListener { _, _, _, newPosition ->
            viewModel.onPositionChanged(newPosition)
            toggleHeader()
        }
    }

    private val header = activity.findViewById<View>(R.id.header)!!

    private val viewAll = activity.findViewById<TextView>(R.id.view_all)!!
    private val title = activity.findViewById<TextView>(R.id.title)!!
    private val pageIndicator = activity.findViewById<PageIndicatorView>(R.id.page_indicator)!!
    private val primaryCta = activity.findViewById<TextView>(R.id.primary_cta)!!
    private val list = activity.findViewById<RecyclerView>(R.id.list)!!.apply {
        setHasFixedSize(true)
        layoutManager = LinearLayoutManager(context)
        adapter = rvAdapter
    }

    private val swipeTouchListener = object : OnSwipeTouchListener(activity) {
        override fun onSwipeRight(): Boolean {
            viewModel.decrementPosition()
            return true
        }

        override fun onSwipeLeft(): Boolean {
            viewModel.incrementPosition()
            return true
        }
    }

    init {
        viewAll.setOnClickListener { toggleHeader() }

        activity.findViewById<View>(R.id.secondary_cta).setOnClickListener { viewModel.onCancelClicked() }

        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.showIntroPopupFlow.filterNotNull().collect { itemsSize ->
                    DialogHelper().builder(activity, R.style.ThemeOverlay_Dashlane_DashlaneAlertDialog)
                        .setTitle(R.string.custom_csv_import_instructions_title)
                        .setMessage(
                            activity.getString(
                                R.string.custom_csv_import_instructions_message,
                                itemsSize
                            )
                        )
                        .setPositiveButton(R.string.custom_csv_import_instructions_primary_cta) { _, _ -> viewModel.onDismissIntroPopup() }
                        .setOnDismissListener { viewModel.onDismissIntroPopup() }
                        .show()
                }
            }
        }
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.stateFlow.collect { (items, position, canValidate) ->
                    rvAdapter.populateItems(items)
                    pageIndicator.count = items.size

                    if (items.size < VIEW_ALL_ITEM_THRESHOLD) {
                        viewAll.visibility = View.GONE
                        pageIndicator.visibility = View.VISIBLE
                        header.setOnTouchListener(swipeTouchListener)
                    } else {
                        viewAll.visibility = View.VISIBLE
                        pageIndicator.visibility = View.GONE
                        header.setOnTouchListener(null)
                    }

                    updateView(items, position, canValidate)
                }
            }
        }
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityResultFlow.collect { data ->
                    activity.setResult(Activity.RESULT_OK, data)
                    activity.finish()
                }
            }
        }
        activity.lifecycleScope.launch {
            activity.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.activityCancelFlow.collect { message ->
                    if (message != null) activity.showToaster(message, Toast.LENGTH_LONG)
                    activity.setResult(Activity.RESULT_CANCELED)
                    activity.finish()
                }
            }
        }
    }

    private fun toggleHeader() {
        val expand = header.layoutParams.height == ViewGroup.LayoutParams.WRAP_CONTENT

        val headerHeight: Int
        val titleVisibility: Int
        val listVisibility: Int
        val viewAllStringRes: Int
        val viewAllDrawableRes: Int

        if (expand) {
            headerHeight = ViewGroup.LayoutParams.MATCH_PARENT
            titleVisibility = View.GONE
            listVisibility = View.VISIBLE
            viewAllStringRes = R.string.custom_csv_import_toggle_expanded
            viewAllDrawableRes = R.drawable.ic_arrow_up_text_secondary_24dp
        } else {
            headerHeight = ViewGroup.LayoutParams.WRAP_CONTENT
            titleVisibility = View.VISIBLE
            listVisibility = View.GONE
            viewAllStringRes = R.string.custom_csv_import_toggle_collapsed
            viewAllDrawableRes = R.drawable.ic_arrow_down_text_secondary_24dp
        }

        header.layoutParams = header.layoutParams.apply { height = headerHeight }
        title.visibility = titleVisibility
        list.visibility = listVisibility

        viewAll.run {
            setText(viewAllStringRes)
            val drawable = ContextCompat.getDrawable(viewAll.context, viewAllDrawableRes)
            setCompoundDrawablesRelativeWithIntrinsicBounds(null, null, drawable, null)
        }
    }

    private fun updateView(items: List<CustomCsvImportItem>, position: Int, canValidate: Boolean) {
        val item = items[position]
        val selectedCategories = items.filter { it.category != CATEGORY_NONE && it.id != item.id }
            .mapNotNull { it.category }
            .toSet()

        title.text = item.title
        categoryToIds.forEach { (category, id) ->
            activity.findViewById<View>(id)?.run {
                isActivated = category == item.category
                isEnabled = category !in selectedCategories
            }
        }

        val idToCategories = categoryToIds.toList().associate { (category, id) -> id to category }

        val onCategoryClickListener = View.OnClickListener { view ->
            idToCategories.getValue(view.id)
                .let { category -> viewModel.onCategorySelected(items[position], category) }
        }

        idToCategories.keys.forEach { id ->
            activity.findViewById<View>(id)!!.setOnClickListener(onCategoryClickListener)
        }

        pageIndicator.selection = position

        primaryCta.run {
            val isLast = position == items.size - 1
            if (canValidate || isLast) {
                isEnabled = canValidate
                setText(R.string.custom_csv_import_primary_cta_done)
            } else {
                isEnabled = item.category != null
                setText(R.string.custom_csv_import_primary_cta_next)
            }
            setOnClickListener {
                if (canValidate) {
                    
                    viewModel.onValidateClicked()
                } else {
                    
                    viewModel.incrementPosition()
                }
            }
        }
    }

    companion object {
        private const val VIEW_ALL_ITEM_THRESHOLD = 10

        private val categoryToIds = mapOf(
            CATEGORY_URL to R.id.button_url,
            CATEGORY_USERNAME to R.id.button_username,
            CATEGORY_PASSWORD to R.id.button_password,
            CATEGORY_NONE to R.id.button_other
        )
    }
}