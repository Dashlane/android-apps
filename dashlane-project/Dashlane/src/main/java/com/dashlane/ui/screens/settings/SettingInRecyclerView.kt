package com.dashlane.ui.screens.settings

import android.animation.ObjectAnimator.ofArgb
import android.content.Context
import android.graphics.Color
import android.view.View
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import com.dashlane.R
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.adapter.util.DiffUtilComparator
import com.dashlane.ui.screens.settings.item.SettingCheckable
import com.dashlane.ui.screens.settings.item.SettingItem
import com.dashlane.ui.screens.settings.item.SettingLoadable
import com.dashlane.util.getThemeAttrColor
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class SettingInRecyclerView(val display: SettingItem) :
    DashlaneRecyclerAdapter.ViewTypeProvider,
    DiffUtilComparator<SettingInRecyclerView> {
    var onSettingInteraction: (() -> Unit)? = null
    var needsHighlight = false

    override fun getViewType() = VIEW_TYPE

    override fun isItemTheSame(item: SettingInRecyclerView): Boolean =
        item.display.isItemTheSame(display)

    override fun isContentTheSame(item: SettingInRecyclerView): Boolean =
        item.display.isContentTheSame(display) && item.needsHighlight == needsHighlight

    class ViewHolder(view: View) : EfficientViewHolder<SettingInRecyclerView>(view) {
        private val clickListener = View.OnClickListener {
            `object`?.let {
                it.display.onClick(context)
                it.onSettingInteraction?.invoke()
                updateView(context, it)
            }
        }

        override fun updateView(context: Context, item: SettingInRecyclerView?) {
            val display = item?.display
            val editable = display?.isEnable() ?: false
            setText(R.id.setting_title, display?.title)
            setText(R.id.setting_description, display?.description)

            findViewByIdEfficient<TextView>(R.id.setting_title)?.apply {
                isEnabled = editable
                setTextColor(
                    context.getColor(
                        if (editable) {
                            R.color.text_neutral_catchy
                        } else {
                            R.color.text_oddity_disabled
                        }
                    )
                )
            }

            findViewByIdEfficient<TextView>(R.id.setting_description)?.apply {
                isEnabled = editable
                setTextColor(
                    context.getColor(
                        if (editable) {
                            R.color.text_neutral_standard
                        } else {
                            R.color.text_oddity_disabled
                        }
                    )
                )
            }

            if (display is SettingLoadable && !display.isLoaded(context)) {
                setVisibility(R.id.setting_checkbox, View.INVISIBLE)
                setVisibility(R.id.setting_progress_bar, View.VISIBLE)
                setVisibility(R.id.setting_trailing, View.VISIBLE)
            } else if (display is SettingCheckable) {
                setVisibility(R.id.setting_checkbox, View.VISIBLE)
                setVisibility(R.id.setting_progress_bar, View.INVISIBLE)
                setVisibility(R.id.setting_trailing, View.VISIBLE)
                val checkBox = findViewByIdEfficient<SwitchCompat>(R.id.setting_checkbox)!!
                checkBox.setOnCheckedChangeListener(null) 
                checkBox.isEnabled = editable
                checkBox.isChecked = display.isChecked(context)
                checkBox.setOnCheckedChangeListener { _, isChecked ->
                    display.onCheckChanged(context, isChecked)
                    item.onSettingInteraction?.invoke()
                }
            } else {
                setVisibility(R.id.setting_trailing, View.GONE)
            }
            item?.highlightIfNecessary()
        }

        private fun SettingInRecyclerView.highlightIfNecessary() {
            if (needsHighlight) {
                needsHighlight = false 

                val highlightColor = context.getThemeAttrColor(R.attr.colorControlHighlight)
                val animator = ofArgb(
                    view,
                    "backgroundColor",
                    Color.TRANSPARENT,
                    highlightColor,
                    Color.TRANSPARENT
                )
                animator.repeatCount = 1
                animator.duration = 1000
                animator.startDelay = 500
                animator.start()
            }
        }

        override fun getOnClickListener(adapterHasListener: Boolean) = clickListener
    }

    companion object {

        val VIEW_TYPE: DashlaneRecyclerAdapter.ViewType<SettingInRecyclerView> =
            DashlaneRecyclerAdapter.ViewType(
                R.layout.list_item_setting_checkbox,
                ViewHolder::class.java
            )
    }
}