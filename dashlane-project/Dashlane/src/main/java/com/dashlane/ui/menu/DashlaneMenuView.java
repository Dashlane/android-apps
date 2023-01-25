package com.dashlane.ui.menu;

import android.content.Context;
import android.util.AttributeSet;

import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.menu.teamspace.TeamspaceAdapterItem;
import com.skocken.presentation.definition.Base;

import java.util.List;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;



public class DashlaneMenuView extends ScrimInsetsRecyclerView implements MenuDef.IView {

    private MenuDef.IPresenter mPresenter;
    private DashlaneRecyclerAdapter<MenuDef.Item> mMenuAdapter;

    public DashlaneMenuView(Context context) {
        this(context, null);
    }

    public DashlaneMenuView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DashlaneMenuView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
        MenuDef.IPresenter presenter = MenuComponent.Companion.invoke(context).getMenuPresenter();
        MenuDataProvider provider = new MenuDataProvider();
        if (presenter instanceof MenuPresenter) {
            ((MenuPresenter) (presenter)).setProvider(provider);
        }
        presenter.setView(this);
        setPresenter(presenter);
        setItemAnimator(null); 

        mMenuAdapter = new DashlaneRecyclerAdapter<>();
        mMenuAdapter.setOnItemClickListener((adapter, view, item, position) -> {
            if (item instanceof TeamspaceAdapterItem) {
                mPresenter.onTeamspaceSelected((TeamspaceAdapterItem) item);
            }
            item.doNavigation(provider.getMenuUsageLogger());
        });
        setAdapter(mMenuAdapter);
    }

    @Override
    public final void setPresenter(Base.IPresenter presenter) {
        mPresenter = (MenuDef.IPresenter) presenter;
    }

    @Override
    public void setItems(List<? extends MenuDef.Item> items) {
        mMenuAdapter.populateItems((List<MenuDef.Item>) items);
    }

    public void refresh() {
        mPresenter.refreshMenuList();
    }

}
