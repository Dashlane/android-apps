package com.dashlane.item.subview.view

import android.content.Context
import android.view.View
import android.widget.AdapterView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.dashlane.R
import com.dashlane.item.subview.ValueChangeManager
import com.dashlane.ui.adapter.SpinnerAdapterDefaultValueString
import com.dashlane.teamspaces.adapter.SpinnerUtil
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrResourceId

object SpinnerInputProvider {
    fun create(
        context: Context,
        title: String,
        defaultText: String,
        values: List<String>,
        editable: Boolean = false,
        currentValueChangeManager: ValueChangeManager<String>? = null,
        selectionAction: (Int) -> Unit = { _ -> }
    ): LinearLayout {
        val adapter = SpinnerAdapterDefaultValueString(
            context,
            R.layout.spinner_item_dropdown,
            R.layout.spinner_item_preview,
            values,
            defaultText
        )
        val promptText = context.getString(R.string.choose)

        return create(
            context = context,
            title = title,
            defaultValue = defaultText,
            editable = editable,
            promptText = promptText,
            adapter = adapter,
            currentValueChangeManager = currentValueChangeManager,
            selectionAction = selectionAction
        )
    }

    fun <T> createSpinner(
        context: Context,
        defaultValue: T,
        editable: Boolean,
        promptText: String?,
        adapter: SpinnerAdapter,
        currentValueChangeManager: ValueChangeManager<T>? = null,
        adapterValueChangeManager: ValueChangeManager<SpinnerAdapter>? = null,
        enableStateValueChangeManager: ValueChangeManager<Boolean>? = null,
        selectionAction: ((Int) -> Unit)? = null
    ): Spinner {
        return Spinner(context).apply {
            id = R.id.item_subview_spinner
            this.adapter = adapter
            var indexItem = indexOf(defaultValue)

            onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, selectedIndex: Int, p3: Long) {
                    if (indexItem != selectedIndex) {
                        
                        indexItem = selectedIndex
                        selectionAction?.invoke(selectedIndex)
                    }
                }

                override fun onNothingSelected(p0: AdapterView<*>?) {
                    
                }
            }
            setEditable(editable)
            prompt = promptText
            minimumHeight = context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size)

            indexItem?.let { setSelection(it) }

            currentValueChangeManager?.addValueChangedListener(object : ValueChangeManager.Listener<T> {
                override fun onValueChanged(origin: Any, newValue: T) {
                    setSelectionItem(newValue)
                }
            })
            adapterValueChangeManager?.addValueChangedListener(object : ValueChangeManager.Listener<SpinnerAdapter> {
                override fun onValueChanged(origin: Any, newValue: SpinnerAdapter) {
                    setAdapter(newValue)
                }
            })
            enableStateValueChangeManager?.addValueChangedListener(object : ValueChangeManager.Listener<Boolean> {
                override fun onValueChanged(origin: Any, newValue: Boolean) {
                    setEditable(newValue)
                }
            })
        }
    }

    fun <T> create(
        context: Context,
        title: String,
        defaultValue: T,
        editable: Boolean,
        promptText: String?,
        adapter: SpinnerAdapter,
        currentValueChangeManager: ValueChangeManager<T>? = null,
        adapterValueChangeManager: ValueChangeManager<SpinnerAdapter>? = null,
        enableStateValueChangeManager: ValueChangeManager<Boolean>? = null,
        selectionAction: ((Int) -> Unit)? = null
    ): LinearLayout {
        val layout = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
        }
        val spinner = createSpinner(
            context, defaultValue, editable, promptText,
            adapter, currentValueChangeManager, adapterValueChangeManager,
            enableStateValueChangeManager, selectionAction
        )

        layout.addView(
            TextView(context).apply {
            text = title
            id = R.id.item_subview_title
            setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
            setTextColor(context.getThemeAttrColor(R.attr.colorOnBackgroundMedium))
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                marginStart = context.dpToPx(4f).toInt()
            }
        }
        )
        layout.addView(
            spinner.apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        )
        return layout
    }

    private fun Spinner.setEditable(editable: Boolean) {
        if (editable) {
            SpinnerUtil.enableSpinner(this)
        } else {
            SpinnerUtil.disableSpinner(this)
        }
    }

    private fun <T> Spinner.setSelectionItem(value: T) {
        val index = indexOf(value) ?: return 
        setSelection(index)
    }

    private fun <T> Spinner.indexOf(value: T): Int? {
        return (0 until adapter.count).firstOrNull {
            adapter.getItem(it) == value
        }
    }
}