package com.dashlane.item.subview.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.TextView
import com.dashlane.R
import com.dashlane.listeners.edittext.NoLockEditTextWatcher

@SuppressLint("InflateParams")
object EditTextInputProvider {

    fun create(context: Context, hint: String, value: String, textSize: Float, editable: Boolean): EditText {
        
        
        return (LayoutInflater.from(context).inflate(R.layout.edittext_input_provider_item, null) as EditText).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize)
            setHint(hint)
            setText(value, TextView.BufferType.EDITABLE)
            minimumHeight = context.resources.getDimensionPixelSize(R.dimen.minimum_clickable_area_size)
            if (editable) {
                
                addTextChangedListener(NoLockEditTextWatcher())
            } else {
                
                keyListener = null
                setTextIsSelectable(true)
            }
            
            background = null
        }
    }
}