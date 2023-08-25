package com.dashlane.ui.menu

import android.content.Context
import android.util.AttributeSet
import androidx.recyclerview.widget.LinearLayoutManager
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter
import com.dashlane.ui.menu.MenuComponent.Companion.invoke
import com.dashlane.ui.menu.teamspace.TeamspaceAdapterItem
import com.skocken.presentation.definition.Base
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class DashlaneMenuView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : ScrimInsetsRecyclerView(context, attrs, defStyleAttr), MenuDef.IView {
    var presenter: MenuDef.IPresenter = invoke(context).menuPresenter
    private val menuAdapter: DashlaneRecyclerAdapter<MenuDef.Item>

    val dataProvider: MenuDataProvider =
        EntryPointAccessors.fromApplication(context, MenuEntryPoint::class.java).menuDataProvider

    init {
        layoutManager = LinearLayoutManager(context, VERTICAL, false)
        (presenter as? MenuPresenter)?.setProvider(dataProvider)
        presenter.setView(this)
        setPresenter(presenter)
        itemAnimator = null 
        menuAdapter = DashlaneRecyclerAdapter()
        menuAdapter.setOnItemClickListener { _, _, item: MenuDef.Item?, _ ->
            item ?: return@setOnItemClickListener
            if (item is TeamspaceAdapterItem) {
                presenter.onTeamspaceSelected(item)
            }
            item.doNavigation(dataProvider.menuUsageLogger)
        }
        adapter = menuAdapter
    }

    override fun setItems(items: List<MenuDef.Item>) {
        menuAdapter.populateItems(items)
    }

    override fun setPresenter(presenter: Base.IPresenter?) {
    }

    fun refresh() {
        presenter.refreshMenuList()
    }

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface MenuEntryPoint {
        val menuDataProvider: MenuDataProvider
    }
}