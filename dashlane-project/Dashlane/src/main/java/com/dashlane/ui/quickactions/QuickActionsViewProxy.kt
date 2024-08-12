package com.dashlane.ui.quickactions

import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.dashlane.R
import com.dashlane.events.AppEvents
import com.dashlane.events.clearLastEvent
import com.dashlane.item.subview.Action
import com.dashlane.lock.UnlockEvent
import com.dashlane.login.lock.unlockItemIfNeeded
import com.dashlane.session.SessionManager
import com.dashlane.session.repository.LockRepository
import com.dashlane.session.repository.getLockManager
import com.dashlane.storage.userdata.accessor.GenericDataQuery
import com.dashlane.ui.activities.fragments.list.action.ActionItemHelper
import com.dashlane.ui.adapter.ItemListContext
import com.dashlane.ui.thumbnail.ThumbnailDomainIconView
import com.dashlane.vault.summary.SummaryObject
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.skocken.presentation.viewproxy.BaseViewProxy
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class QuickActionsViewProxy(
    private val fragment: BottomSheetDialogFragment,
    private val item: SummaryObject,
    private val quickActionsLogger: QuickActionsLogger,
    itemListContext: ItemListContext,
    private val lockRepository: LockRepository,
    private val sessionManager: SessionManager,
    private val dataQuery: GenericDataQuery,
    private val appEvents: AppEvents,
    private val originPage: String?
) : QuickActionsContract.ViewProxy, BaseViewProxy<QuickActionsContract.Presenter>(fragment) {

    private val activity = fragment.requireActivity() as AppCompatActivity

    init {
        quickActionsLogger.logOpenQuickActions(item, itemListContext)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun setActions(actions: List<Action>) {
        actions.forEach { action ->
            val tintColor = action.tintColorRes?.let { tintColorRes ->
                context.getColor(tintColorRes)
            }
            addAction(action.icon, action.text, tintColor) {
                
                fragment.dismiss()

                
                val lockManager = lockRepository.getLockManager(sessionManager) ?: return@addAction
                GlobalScope.launch(Dispatchers.Main.immediate) {
                    if (!lockManager.unlockItemIfNeeded(
                            context,
                            dataQuery,
                            item.id,
                            item.syncObjectType.xmlObjectName
                        )
                    ) {
                        return@launch
                    }

                    GlobalScope.launch {
                        delay(100)
                        withContext(Dispatchers.Main.immediate) {
                            action.onClickAction(activity)
                            quickActionsLogger.logCloseQuickActions(originPage)
                        }
                    }
                    appEvents.clearLastEvent<UnlockEvent>()
                }
            }
        }
    }

    override fun setItemDetail(
        title: String?,
        thumbnailType: Int?,
        thumbnailIconRes: Int?,
        thumbnailColorRes: Int?,
        thumbnailUrlDomain: String?
    ) {
        findViewByIdEfficient<ThumbnailDomainIconView>(R.id.quick_actions_icon)?.apply {
            thumbnailType?.let { this.thumbnailType = it }
            thumbnailColorRes?.let { this.color = context.getColor(it) }
            thumbnailIconRes?.let { this.iconRes = it }
            thumbnailUrlDomain?.let { this.domainUrl = it }
        }
        findViewByIdEfficient<TextView>(R.id.quick_actions_title)?.text = title
    }

    private fun addAction(@DrawableRes iconRes: Int, @StringRes title: Int, tintColor: Int?, action: () -> Unit) {
        val actionView = ActionItemHelper().createNewActionItem(
            context,
            ContextCompat.getDrawable(context, iconRes),
            true,
            context.getString(title),
            tintColor,
            action
        )
        findViewByIdEfficient<LinearLayout>(R.id.actions)?.addView(actionView)
    }
}
