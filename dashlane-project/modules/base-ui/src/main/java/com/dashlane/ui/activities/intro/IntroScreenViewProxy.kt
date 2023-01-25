package com.dashlane.ui.activities.intro

import android.app.Activity
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.ViewCompat
import androidx.core.view.isVisible
import com.dashlane.design.component.compat.view.BaseButtonView
import com.dashlane.ui.R
import com.dashlane.ui.widgets.view.Infobox
import com.skocken.presentation.viewproxy.BaseViewProxy



class IntroScreenViewProxy : BaseViewProxy<IntroScreenContract.Presenter>,
    IntroScreenContract.ViewProxy {

    constructor(view: View) : super(view)
    constructor(activity: Activity) : super(activity)

    init {
        findViewByIdEfficient<BaseButtonView>(R.id.positive_button)!!.onClick = {
            presenterOrNull?.onClickPositiveButton()
        }
        findViewByIdEfficient<BaseButtonView>(R.id.negative_button)!!.onClick = {
            presenterOrNull?.onClickNegativeButton()
        }
        findViewByIdEfficient<BaseButtonView>(R.id.neutral_button)?.onClick = {
            presenterOrNull?.onClickNeutralButton()
        }
        ViewCompat.setAccessibilityHeading(
            findViewByIdEfficient<TextView>(R.id.title_textview)!!,
            true
        )
    }

    override fun setImageResource(imageResId: Int) {
        findViewByIdEfficient<ImageView>(R.id.logo_imageview)!!.apply {
            imageTintList = ColorStateList.valueOf(context.getColor(R.color.text_brand_quiet))
            setImageResource(imageResId)
        }
        setGoneIfZero(R.id.logo_imageview, imageResId)
    }

    override fun setImageResourceWithTint(imageResId: Int, @ColorRes colorRes: Int?) {
        findViewByIdEfficient<ImageView>(R.id.logo_imageview)!!.apply {
            imageTintList = colorRes?.let { ColorStateList.valueOf(context.getColor(it)) }
            setImageResource(imageResId)
        }
        setGoneIfZero(R.id.logo_imageview, imageResId)
    }

    override fun setTitle(textResId: Int) {
        setTextOrGone(R.id.title_textview, textResId)
    }

    override fun setTitle(text: String) {
        findViewByIdEfficient<TextView>(R.id.title_textview)!!.text = text
    }

    override fun setDescription(textResId: Int) {
        setTextOrGone(R.id.description_textview, textResId)
    }

    override fun setDescription(text: String) {
        findViewByIdEfficient<TextView>(R.id.description_textview)!!.text = text
    }

    override fun setPositiveButton(textResId: Int) {
        setTextOrGone(R.id.positive_button, textResId)
    }

    override fun setNeutralButton(textResId: Int) {
        setTextOrGone(R.id.neutral_button, textResId)
    }

    override fun setNegativeButton(textResId: Int) {
        setTextOrGone(R.id.negative_button, textResId)
    }

    override fun setShowProgress(show: Boolean) {
        findViewByIdEfficient<TextView>(R.id.positive_button)!!.run {
            if (show) {
                isEnabled = false
                setTextColor(Color.TRANSPARENT)
            } else {
                isEnabled = true
                setTextColor(context.getColor(R.color.text_neutral_catchy))
            }
        }
        findViewByIdEfficient<ProgressBar>(R.id.view_progress)!!.visibility =
            if (show) View.VISIBLE else View.GONE
    }

    override fun setInfobox(textResId: Int) {
        setGoneIfZero(R.id.infobox, textResId)
        findViewByIdEfficient<Infobox>(R.id.infobox)!!.run {
            text = context.getString(textResId)
        }
    }

    override fun setDetailView(view: View) {
        findViewByIdEfficient<LinearLayout>(R.id.details_container)!!.apply {
            removeAllViews()
            addView(view)
            isVisible = true
        }
    }

    override fun setLinks(vararg links: Int) {
        val linkContainer = findViewByIdEfficient<LinearLayout>(R.id.links_container)!!
        linkContainer.dividerPadding

        
        linkContainer.removeAllViews()

        
        val layoutInflater = LayoutInflater.from(linkContainer.context)
        links.forEachIndexed { index, linkResId ->
            val buttonLink = layoutInflater.inflate(
                R.layout.include_intro_link,
                linkContainer,
                false
            ) as BaseButtonView
            buttonLink.text = buttonLink.context.getString(linkResId)
            buttonLink.onClick = { presenterOrNull?.onClickLink(index, linkResId) }
            linkContainer.addView(buttonLink)
        }

        
        linkContainer.apply {
            isVisible = childCount != 0
        }
    }

    private fun setTextOrGone(viewId: Int, textResId: Int) {
        setGoneIfZero(viewId, textResId)
        findViewByIdEfficient<View>(viewId)!!.run {
            if (visibility == View.GONE) {
                when (this) {
                    is TextView -> text = null
                    is BaseButtonView -> text = null
                }
            } else {
                when (this) {
                    is TextView -> setText(textResId)
                    is BaseButtonView -> text = context.getString(textResId)
                }
            }
        }
    }

    private fun setGoneIfZero(viewId: Int, value: Int) {
        val view = findViewByIdEfficient<View>(viewId)!!
        if (value == 0) {
            view.visibility = View.GONE
        } else {
            view.visibility = View.VISIBLE
        }
    }
}