package com.dashlane.item.subview.view

import android.annotation.SuppressLint
import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import com.dashlane.R
import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.Totp
import com.dashlane.util.dpToPx
import com.dashlane.util.getThemeAttrColor
import com.dashlane.util.getThemeAttrDrawable
import com.dashlane.util.getThemeAttrResourceId



object AuthenticatorViewProvider {

    @SuppressLint("InflateParams")
    fun createActivate(context: Context, title: String) = LinearLayout(context).apply {
        createBaseLayout(context, title)
        LayoutInflater.from(context)
            .inflate(R.layout.include_item_edit_authenticator_activate, this)
    }

    fun create(context: Context, title: String, otp: Otp) =
        LinearLayout(context).apply {
            createBaseLayout(context, title)
            createCodeLayout(context, otp)
            
        }

    private fun LinearLayout.createCodeLayout(context: Context, otp: Otp) {
        val margin = context.dpToPx(6)
        addView(LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = margin
            params.topMargin = margin
            layoutParams = params
            setVerticalGravity(Gravity.CENTER)
            addView(
                ImageView(
                    context,
                    null,
                    R.style.Widget_AppCompat_Button_Borderless
                ).apply {
                    id = R.id.item_subview_imageview
                    scaleType = ImageView.ScaleType.FIT_XY
                    val size = when (otp) {
                        is Totp -> context.dpToPx(16)
                        else -> context.dpToPx(24)
                    }
                    layoutParams =
                        LinearLayout.LayoutParams(size, size)
                            .apply { marginEnd = context.dpToPx(8) }
                    background =
                        context.getThemeAttrDrawable(R.attr.selectableItemBackgroundBorderless)
                })
            addView(TextView(context).apply {
                id = R.id.item_subview_textview
                setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody1))
                typeface = ResourcesCompat.getFont(context, R.font.roboto_mono_regular)
            })
        })
    }

    private fun LinearLayout.createBaseLayout(context: Context, title: String) {
        orientation = LinearLayout.VERTICAL
        val marginStart = context.dpToPx(4)

        addView(TextView(context).apply {
            setTextAppearance(context.getThemeAttrResourceId(R.attr.textAppearanceBody2))
            setTextColor(context.getThemeAttrColor(R.attr.colorOnBackgroundMedium))
            this.text = title
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            params.marginStart = marginStart
            layoutParams = params
        })
    }
}