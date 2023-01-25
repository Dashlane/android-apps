package com.dashlane.ui.activities.intro

import android.view.View
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import com.skocken.presentation.definition.Base



interface IntroScreenContract {

    interface ViewProxy : Base.IView {
        fun setImageResource(@DrawableRes imageResId: Int)
        fun setImageResourceWithTint(@DrawableRes imageResId: Int, @ColorRes colorRes: Int?)
        fun setTitle(@StringRes textResId: Int)
        fun setTitle(text: String)
        fun setDescription(@StringRes textResId: Int)
        fun setDescription(text: String)
        fun setPositiveButton(@StringRes textResId: Int)
        fun setNeutralButton(@StringRes textResId: Int)
        fun setNegativeButton(@StringRes textResId: Int)
        fun setDetailView(view: View)
        fun setLinks(@StringRes vararg links: Int)
        fun setShowProgress(show: Boolean)
        fun setInfobox(textResId: Int)
    }

    interface Presenter : Base.IPresenter {
        fun onClickPositiveButton()
        fun onClickNeutralButton()
        fun onClickNegativeButton()
        fun onClickLink(position: Int, @StringRes label: Int)
    }

    interface DataProvider : Base.IDataProvider
}