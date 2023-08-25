package com.dashlane.ui.menu.separator;

import com.dashlane.R;
import com.dashlane.ui.adapter.DashlaneRecyclerAdapter;
import com.dashlane.ui.adapter.util.DiffUtilComparator;
import com.dashlane.ui.menu.MenuDef;
import com.dashlane.ui.menu.MenuUsageLogger;

import org.jetbrains.annotations.NotNull;

import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

public class MenuSectionHeaderItem implements MenuDef.Item, DiffUtilComparator<MenuSectionHeaderItem> {

    final DashlaneRecyclerAdapter.ViewType<MenuDef.Item> VIEW_TYPE =
            new DashlaneRecyclerAdapter.ViewType<>(
                    R.layout.item_section_header,
                    MenuSectionHeaderViewHolder.class);

    @StringRes
    private int mTitleResId;

    public MenuSectionHeaderItem(@StringRes int titleResId) {
        mTitleResId = titleResId;
    }

    @StringRes
    public int getTitleResId() {
        return mTitleResId;
    }

    @NonNull
    @Override
    public DashlaneRecyclerAdapter.ViewType getViewType() {
        return VIEW_TYPE;
    }

    @Override
    public boolean isItemTheSame(MenuSectionHeaderItem item) {
        return mTitleResId == item.getTitleResId();
    }

    @Override
    public boolean isContentTheSame(MenuSectionHeaderItem item) {
        return isItemTheSame(item);
    }

    @Override
    public void doNavigation(@NotNull MenuUsageLogger menuUsageLogger) {
        
    }
}
