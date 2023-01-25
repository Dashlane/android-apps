package com.dashlane.item.subview.view

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatSpinner



class CustomItemSelectSpinner : AppCompatSpinner {
    constructor(
        context: Context,
        attrs: AttributeSet?,
        defStyleAttr: Int,
        mode: Int
    ) : super(context, attrs, defStyleAttr, mode)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, mode: Int) : super(context, mode)
    constructor(context: Context) : super(context)

    override fun setSelection(position: Int) {
        val sameSelected = position == selectedItemPosition
        super.setSelection(position)
        if (sameSelected) {
            
            onItemSelectedListener?.onItemSelected(this, selectedView, position, selectedItemId)
        }
    }

    override fun setSelection(position: Int, animate: Boolean) {
        val sameSelected = position == selectedItemPosition
        super.setSelection(position, animate)
        if (sameSelected) {
            
            onItemSelectedListener?.onItemSelected(this, selectedView, position, selectedItemId)
        }
    }
}