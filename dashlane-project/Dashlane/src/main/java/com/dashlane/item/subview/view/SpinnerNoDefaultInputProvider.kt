package com.dashlane.item.subview.view

import android.content.Context
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.SpinnerAdapter
import android.widget.TextView
import com.dashlane.R
import com.dashlane.ui.adapter.SpinnerAdapterDefaultValueString
import com.dashlane.ui.util.SpinnerUtil
import com.dashlane.util.addTextChangedListener
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrDrawable



object SpinnerNoDefaultInputProvider {
    fun create(
        context: Context,
        title: String,
        defaultText: String,
        values: List<String>,
        selectionAction: (String) -> Unit = { _ -> }
    ): View {
        val adapter = object : SpinnerAdapterDefaultValueString(
            context,
            R.layout.spinner_item_dropdown,
            R.layout.spinner_item_preview,
            values,
            values[0]
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                return (super.getView(position, convertView, parent) as TextView).apply {
                    
                    setTextColor(Color.TRANSPARENT)
                }
            }
        }
        val promptText = context.getString(R.string.choose)

        return create(
            context = context,
            title = title,
            defaultText = defaultText,
            values = values,
            promptText = promptText,
            adapter = adapter,
            selectionAction = selectionAction
        )
    }

    private fun createSpinner(
        context: Context,
        promptText: String?,
        adapter: SpinnerAdapter,
        itemSelectedListener: AdapterView.OnItemSelectedListener
    ): Spinner {
        return CustomItemSelectSpinner(context).apply {
            minimumHeight = context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size)
            id = R.id.item_subview_spinner
            this.adapter = adapter
            prompt = promptText
            onItemSelectedListener = itemSelectedListener
        }
    }

    private fun create(
        context: Context,
        title: String,
        defaultText: String,
        values: List<String>,
        promptText: String?,
        adapter: SpinnerAdapter,
        selectionAction: (String) -> Unit
    ): FrameLayout {
        val layout = FrameLayout(context)
        val textInputLayout = TextInputLayoutProvider.create(context, title, defaultText, false)

        var userSelect = false

        val spinnerListener = object : AdapterView.OnItemSelectedListener {

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (userSelect) {
                    textInputLayout.editText?.setText(values[position])
                    userSelect = false
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) = Unit
        }
        val spinner = createSpinner(context, promptText, adapter, spinnerListener)

        val clickListener = View.OnClickListener {
            userSelect = true
            spinner.performClick()
        }

        textInputLayout.apply {
            setOnClickListener(clickListener)
            editText?.apply {
                setOnClickListener(clickListener)
                
                background = context.getThemeAttrDrawable(R.attr.editTextBackground)
                addTextChangedListener {
                    afterTextChanged {
                        val newValue = it.toString()
                        selectionAction.invoke(newValue)
                    }
                }
            }
        }
        layout.apply {
            addView(spinner.apply {
                layoutParams = FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, context.dpToPx(16f).toInt(), 0, 0)
                }
                SpinnerUtil.disableSpinner(this)
            })
            addView(textInputLayout.apply {
                layoutParams = FrameLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
            })
        }

        return layout
    }
}