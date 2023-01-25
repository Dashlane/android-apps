package com.dashlane.ui.menu.teamspace;

import com.dashlane.R;
import com.dashlane.teamspaces.model.Teamspace;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.menu.MenuDef;
import com.dashlane.ui.menu.MenuUsageLogger;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;



public class TeamspaceAdapterItem implements MenuDef.Item {

    private final static DashlaneRecyclerAdapter.ViewType<MenuDef.Item> VIEW_TYPE =
            new DashlaneRecyclerAdapter.ViewType<>(R.layout.item_menu_teamspace_item, MenuTeamspaceViewHolder.class);

    private final Teamspace mTeamspace;

    public TeamspaceAdapterItem(Teamspace teamspace) {
        mTeamspace = teamspace;
    }

    public Teamspace getTeamspace() {
        return mTeamspace;
    }

    @NonNull
    @Override
    public DashlaneRecyclerAdapter.ViewType getViewType() {
        return VIEW_TYPE;
    }

    @Override
    public void doNavigation(@NotNull MenuUsageLogger menuUsageLogger) {
        
    }
}
