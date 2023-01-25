package com.dashlane.ui.menu.separator;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.dashlane.ui.menu.MenuDef;
import com.skocken.efficientadapter.lib.viewholder.EfficientViewHolder;



public class MenuSectionHeaderViewHolder extends EfficientViewHolder<MenuDef.Item> {

    public MenuSectionHeaderViewHolder(View itemView) {
        super(itemView);
    }

    @Override
    protected void updateView(Context context, MenuDef.Item object) {
        if (!(object instanceof MenuSectionHeaderItem)) {
            return;
        }
        TextView headerTextView = (TextView) getView();
        headerTextView.setText(((MenuSectionHeaderItem) object).getTitleResId());

    }
}
