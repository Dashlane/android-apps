package com.dashlane.login

import android.annotation.SuppressLint
import android.content.res.Resources
import android.graphics.drawable.LayerDrawable
import android.view.View
import android.widget.EditText
import com.dashlane.ui.R
import com.dashlane.util.getThemeAttrColor



object CodeInputViewHelper {

    

    fun initialize(editText: EditText, width: Int) {
        val characterCount = editText.text.length
        if (width == 0) {
            initializeSize(editText, characterCount)
        } else {
            initializeText(editText, width)
            initializeBackground(editText, characterCount)
        }
    }

    

    private fun initializeSize(editText: EditText, characterCount: Int) {
        editText.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
            override fun onLayoutChange(
                v: View,
                left: Int,
                top: Int,
                right: Int,
                bottom: Int,
                oldLeft: Int,
                oldTop: Int,
                oldRight: Int,
                oldBottom: Int
            ) {
                val width = right - left
                if (width < 40) return
                editText.removeOnLayoutChangeListener(this)
                initializeText(editText, width)
                initializeBackground(editText, characterCount)
            }
        })
    }

    private fun initializeText(editText: EditText, width: Int) {
        val context = editText.context
        editText.width = width
        editText.text.clear()
        editText.setTextColor(context.getThemeAttrColor(R.attr.colorOnBackground))
    }

    

    private fun initializeBackground(editText: EditText, characterCount: Int) {
        val context = editText.context
        val resources = context.resources

        val drawables = Array(characterCount) { context.getDrawable(R.drawable.code_input_character_background) }
        val paddingLeft = editText.paddingLeft
        val paddingRight = editText.paddingRight
        editText.background = object : LayerDrawable(drawables) {
            val spacing = resources.getDimensionPixelOffset(R.dimen.code_input_underline_spacing)
            val layerBottom = bottomInset(resources)

            override fun setBounds(left: Int, top: Int, right: Int, bottom: Int) {
                val width = right - left - paddingLeft - paddingRight
                val height = bottom - top
                val count = numberOfLayers
                val layerWidth = width / count

                for (i in 0 until count) {
                    val layerLeft = i * layerWidth + spacing + paddingLeft
                    val layerRight = (count - i - 1) * layerWidth + spacing + paddingRight
                    val layerTop = height - layerBottom - getDrawable(i).intrinsicHeight
                    setLayerInset(i, layerLeft, layerTop, layerRight, layerBottom)
                }

                super.setBounds(left, top, right, bottom)
            }

            

            @SuppressLint("PrivateResource")
            private fun bottomInset(resources: Resources) =
                resources.getDimensionPixelOffset(R.dimen.abc_edit_text_inset_bottom_material)
        }
    }
}
