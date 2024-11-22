package com.dashlane.item.subview.action

import androidx.appcompat.app.AppCompatActivity
import com.dashlane.R
import com.dashlane.item.ItemEditViewContract
import com.dashlane.ui.action.Action

class OpenLinkedWebsitesAction(
    private val listener: ItemEditViewContract.View.UiUpdateListener,
    private val itemUid: String,
    private val editMode: Boolean,
    private val temporaryWebsites: List<String>,
    private val temporaryApps: List<String>?,
    private val urlDomain: String?,
) : Action {
    override val text: Int = R.string.multi_domain_credentials_title
    override val icon: Int = R.drawable.ic_chevron_right
    override val tintColorRes: Int? = null

    override fun onClickAction(activity: AppCompatActivity) {
        listener.openLinkedServices(itemUid, !editMode, false, temporaryWebsites, temporaryApps, urlDomain)
    }
}