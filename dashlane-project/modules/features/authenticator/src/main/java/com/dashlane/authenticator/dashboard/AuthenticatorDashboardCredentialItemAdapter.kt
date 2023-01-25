package com.dashlane.authenticator.dashboard

import com.dashlane.authenticator.Otp
import com.dashlane.authenticator.dashboard.AuthenticatorDashboardUiState.HasLogins.CredentialItem
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.widgets.view.ExpandableCardView
import com.skocken.efficientadapter.lib.adapter.EfficientAdapter
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder

class AuthenticatorDashboardCredentialItemAdapter(
    private val listener: Listener
) : DashlaneRecyclerAdapter<CredentialItem>() {

    interface Listener {
        fun onOtpCounterUpdate(itemId: String, otp: Otp)
        fun onOtpCopy(code: String, itemId: String, domain: String?)
        fun onOtpDelete(item: CredentialItem, issuer: String?)
    }

    private val itemClickListener =
        EfficientAdapter.OnItemClickListener<CredentialItem> { adapter, view, item, position ->
            val holder = view as ExpandableCardView
            item as CredentialItem
            if (!item.editMode) {
                item.expanded = !holder.expanded
                adapter.notifyItemChanged(position)
                
                if (item.expanded) shrinkNotAtPosition(adapter, position)
            }
        }

    val allItemsShown: Boolean
        get() = nbItemsShown >= objects.size || objects.any { it.editMode }

    var nbItemsShown = 0

    init {
        onItemClickListener = itemClickListener
    }

    override fun onBindViewHolder(viewHolder: EfficientViewHolder<CredentialItem>, position: Int) {
        (viewHolder as AuthenticatorDashboardCredentialItemViewHolder).listener = listener
        super.onBindViewHolder(viewHolder, position)
    }

    override fun size() = if (allItemsShown) objects.size else nbItemsShown

    fun setEditMode() {
        objects.forEachIndexed { index, item ->
            item.editMode = true
            item.expanded = true
            notifyItemChanged(index)
        }
    }

    fun setViewMode() {
        objects.forEachIndexed { index, item ->
            item.editMode = false
            item.expanded = index == 0
            notifyItemChanged(index)
        }
    }

    private fun shrinkNotAtPosition(adapter: EfficientAdapter<CredentialItem>, position: Int) {
        adapter.objects.forEachIndexed { index, _ ->
            val adapterItem = adapter.get(index)
            if (index != position && adapterItem.expanded) {
                adapterItem.expanded = false
                adapter.notifyItemChanged(index)
            }
        }
    }
}
