package com.dashlane.item.linkedwebsites.item

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.drawable.Drawable
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.core.widget.TextViewCompat
import com.dashlane.R
import com.dashlane.design.component.compat.view.BadgeView
import com.dashlane.design.component.compat.view.ButtonMediumView
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.dashlane.util.DeviceUtils
import com.dashlane.util.dpToPx
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder
import java.util.UUID

data class LinkedWebsitesItem(
    val defaultUrl: String,
    val isEditable: Boolean,
    val isPageEditMode: Boolean,
    val isMain: Boolean = false,
    var requestFocus: Boolean = false,
    val onValueUpdated: (String, String) -> Unit = { _, _ -> },
    val getUrlValue: (String, String) -> String = { _, _ -> defaultUrl },
    val removeWebsiteListener: (LinkedWebsitesItem) -> Unit = {},
    val openWebsiteListener: (LinkedWebsitesItem) -> Unit,
    val uid: String = UUID.randomUUID().toString()
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = DashlaneRecyclerAdapter.ViewType(
        R.layout.item_linked_websites,
        ViewHolder::class.java
    )

    class ViewHolder(v: View) : EfficientViewHolder<LinkedWebsitesItem>(v) {

        private val textChangerListener = object : TextWatcher {
            lateinit var actualItem: LinkedWebsitesItem

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                
            }

            override fun afterTextChanged(value: Editable?) {
                actualItem.onValueUpdated.invoke(actualItem.uid, value.toString())
            }
        }

        override fun updateView(context: Context, item: LinkedWebsitesItem?) {
            item ?: return
            textChangerListener.actualItem = item
            view.findViewById<LinearLayout>(R.id.editable_layout).isVisible = item.isEditable
            view.findViewById<LinearLayout>(R.id.view_only_layout).isVisible = !item.isEditable
            view.findViewById<ThumbnailDomainIconView>(R.id.website_icon_image).apply {
                domainUrl = item.defaultUrl
            }
            if (item.isEditable) {
                view.findViewById<EditText>(R.id.website_url)?.also {
                    
                    
                    it.removeTextChangedListener(textChangerListener)
                    it.setText(item.getUrlValue.invoke(item.uid, item.defaultUrl))
                    it.addTextChangedListener(textChangerListener)

                    if (item.requestFocus) {
                        it.post {
                            it.requestFocus()
                            DeviceUtils.showKeyboard(it)
                        }
                    }
                }
                view.findViewById<ButtonMediumView>(R.id.remove_button).onClick = {
                    item.removeWebsiteListener.invoke(item)
                }
            } else {
                view.findViewById<TextView>(R.id.website_url_text).let {
                    it.text = item.getUrlValue.invoke(item.uid, item.defaultUrl)
                    if (item.isPageEditMode) {
                        it.setTextColor(context.getColor(R.color.text_neutral_quiet))
                    } else {
                        it.setTextColor(context.getColor(R.color.text_neutral_standard))
                    }
                }
                view.findViewById<ButtonMediumView>(R.id.open_website_button).let {
                    it.isEnabled = !item.isPageEditMode
                    it.onClick = {
                        item.openWebsiteListener.invoke(item)
                    }
                }
            }
        }
    }
}

data class LinkedAppsItem(
    val appName: String?,
    val appIcon: Drawable?,
    val packageName: String,
    val isAppInstalled: Boolean,
    val removeAppListener: (LinkedAppsItem) -> Unit,
    val openAppListener: (LinkedAppsItem) -> Unit,
    val isEditable: Boolean,
    val isPageEditMode: Boolean
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = DashlaneRecyclerAdapter.ViewType(
        R.layout.item_linked_apps,
        ViewHolder::class.java
    )

    class ViewHolder(v: View) : EfficientViewHolder<LinkedAppsItem>(v) {

        override fun updateView(context: Context, item: LinkedAppsItem?) {
            item ?: return
            view.findViewById<TextView>(R.id.app_name).apply {
                text = item.appName ?: item.packageName
                if (!item.isAppInstalled || (!item.isEditable && item.isPageEditMode)) {
                    setTextColor(context.getColor(R.color.text_neutral_quiet))
                } else {
                    setTextColor(context.getColor(R.color.text_neutral_catchy))
                }
            }
            view.findViewById<BadgeView>(R.id.not_installed).apply {
                isVisible = !item.isAppInstalled
            }
            view.findViewById<ImageView>(R.id.app_logo).apply {
                setImageDrawable(item.appIcon)
            }
            view.findViewById<ButtonMediumView>(R.id.action_button).apply {
                if (item.isEditable && item.isPageEditMode) {
                    iconRes = R.drawable.ic_trash
                    onClick = {
                        item.removeAppListener.invoke(item)
                    }
                    iconDescription = context.getString(R.string.and_accessibility_delete_linked_app)
                } else {
                    iconRes = R.drawable.ic_action_open
                    onClick = {
                        item.openAppListener.invoke(item)
                    }
                    iconDescription = context.getString(R.string.and_accessibility_open_linked_app)
                    isEnabled = !item.isPageEditMode
                }
            }
        }
    }
}

data class LinkedServicesHeaderItem(@StringRes val title: Int, val locked: Boolean) :
    DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = DashlaneRecyclerAdapter.ViewType(
        R.layout.item_linked_services_header,
        ViewHolder::class.java
    )

    class ViewHolder(v: View) : EfficientViewHolder<LinkedServicesHeaderItem>(v) {

        override fun updateView(context: Context, item: LinkedServicesHeaderItem?) {
            item ?: return
            view.findViewById<TextView>(R.id.title).apply {
                text = context.getString(item.title)
                if (item.locked) {
                    setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock_filled, 0, 0, 0)
                    compoundDrawablePadding = context.dpToPx(8)
                    val color = ContextCompat.getColor(context, R.color.text_neutral_standard)
                    TextViewCompat.setCompoundDrawableTintList(this, ColorStateList.valueOf(color))
                } else {
                    setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }
        }
    }
}

data class LinkedWebsitesAddItem(
    @StringRes val title: Int,
    val onButtonCLick: (Int) -> Unit
) : DashlaneRecyclerAdapter.ViewTypeProvider {

    override fun getViewType(): DashlaneRecyclerAdapter.ViewType<*> = DashlaneRecyclerAdapter.ViewType(
        R.layout.item_linked_websites_add_button,
        ViewHolder::class.java
    )

    class ViewHolder(v: View) : EfficientViewHolder<LinkedWebsitesAddItem>(v) {

        override fun updateView(context: Context, item: LinkedWebsitesAddItem?) {
            item?.let {
                (view.findViewById<ButtonMediumView>(R.id.add_website_button)).apply {
                    text = context.getString(item.title)
                    onClick = {
                        item.onButtonCLick.invoke(lastBindPosition)
                    }
                }
            }
        }
    }
}