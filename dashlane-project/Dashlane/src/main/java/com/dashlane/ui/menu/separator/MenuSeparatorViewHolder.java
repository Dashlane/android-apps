package com.dashlane.ui.menu.separator;

import android.content.Context;
import android.view.View;

import com.dashlane.R;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.menu.MenuDef;
import com.dashlane.ui.menu.MenuUsageLogger;
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;

public class MenuSeparatorViewHolder extends EfficientViewHolder<MenuDef.Item> {

    public static final MenuDef.Item ITEM = new MenuDef.Item() {

        final DashlaneRecyclerAdapter.ViewType<MenuDef.Item> VIEW_TYPE =
                new DashlaneRecyclerAdapter.ViewType<>(
                        R.layout.item_separator,
                        MenuSeparatorViewHolder.class);

        @NonNull
        @Override
        public DashlaneRecyclerAdapter.ViewType getViewType() {
            return VIEW_TYPE;
        }

        @Override
        public void doNavigation(@NotNull MenuUsageLogger menuUsageLogger) {
            
        }
    };

    public MenuSeparatorViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void updateView(Context context, MenuDef.Item object) {
        
    }
}
