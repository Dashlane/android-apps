package com.dashlane.ui.activities.intro

import android.app.Activity
import android.view.LayoutInflater
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.core.view.ViewCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.dashlane.design.component.compat.view.BaseButtonView
import com.dashlane.ui.R
import com.dashlane.ui.databinding.ActivityIntroBinding
import com.dashlane.util.findContentParent

fun Activity.setIntroScreenContent(
    @DrawableRes imageResId: Int = 0,
    @StringRes titleResId: Int = 0,
    @StringRes descriptionResId: Int = 0,
    @StringRes positiveButtonResId: Int = 0,
    @StringRes negativeButtonResId: Int = 0,
    linkResIds: List<Int> = emptyList(),
    onClickPositiveButton: (() -> Unit)? = null,
    onClickNegativeButton: (() -> Unit)? = null,
    onClickLink: ((index: Int, linkResId: Int) -> Unit)? = null
) {
    val binding = ActivityIntroBinding.inflate(layoutInflater, findContentParent(), false).apply {
        ViewCompat.setAccessibilityHeading(titleTextview, true)

        
        logoImageview.run {
            if (imageResId == 0) {
                isGone = true
            } else {
                setImageResource(imageResId)
                isVisible = true
            }
        }

        
        titleTextview.setTextOrGone(titleResId)
        descriptionTextview.setTextOrGone(descriptionResId)
        positiveButton.setTextOrGone(positiveButtonResId)
        negativeButton.setTextOrGone(negativeButtonResId)

        
        positiveButton.onClick = { onClickPositiveButton?.invoke() }
        negativeButton.onClick = { onClickNegativeButton?.invoke() }

        
        linksContainer.removeAllViews()
        val layoutInflater = LayoutInflater.from(linksContainer.context)
        linkResIds.forEachIndexed { index, linkResId ->
            val linkButton = layoutInflater.inflate(
                R.layout.include_intro_link,
                linksContainer,
                false
            ) as BaseButtonView
            linkButton.text = linkButton.context.getString(linkResId)
            linkButton.onClick = { onClickLink?.invoke(index, linkResId) }
            linksContainer.addView(linkButton)
        }
        linksContainer.isVisible = linksContainer.childCount != 0
    }

    setContentView(binding.root)
}

private fun BaseButtonView.setTextOrGone(textResId: Int) {
    if (textResId == 0) {
        isGone = true
        text = null
    } else {
        isVisible = true
        text = context.getString(textResId)
    }
}

private fun TextView.setTextOrGone(textResId: Int) {
    if (textResId == 0) {
        isGone = true
        text = null
    } else {
        isVisible = true
        setText(textResId)
    }
}