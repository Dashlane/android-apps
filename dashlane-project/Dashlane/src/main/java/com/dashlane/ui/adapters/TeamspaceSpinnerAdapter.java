package com.dashlane.ui.adapters;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.DimenRes;

import com.dashlane.R;
import com.dashlane.teamspaces.manager.TeamspaceDrawableProvider;
import com.dashlane.teamspaces.model.Teamspace;

import java.util.List;

public class TeamspaceSpinnerAdapter extends ArrayAdapter<Teamspace> {
    private boolean mDisabled;

    public TeamspaceSpinnerAdapter(Context context, List<Teamspace> list) {
        super(context, R.layout.spinner_item_dropdown, list);
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        return getView(position, convertView, parent, R.layout.spinner_item_dropdown,
                       R.dimen.teamspace_icon_size_normal);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = getView(position, convertView, parent, R.layout.spinner_item_preview,
                            R.dimen.teamspace_icon_size_edit_selector);
        view.setEnabled(mDisabled);
        return view;
    }

    private View getView(int position, View convertView, ViewGroup parent, int layoutResId, @DimenRes int iconSizeRes) {
        Resources resources = parent.getResources();
        TextView view;
        if (convertView instanceof TextView) {
            view = (TextView) convertView;
        } else {
            view = (TextView) LayoutInflater.from(parent.getContext())
                                            .inflate(layoutResId, parent, false);
            int padding = Math.round(resources.getDimension(R.dimen.spacing_small));
            view.setCompoundDrawablePadding(padding);
        }

        Teamspace teamspace = getItem(position);
        view.setText(teamspace.getTeamName());

        Drawable drawable = TeamspaceDrawableProvider.getIcon(view.getContext(), teamspace, iconSizeRes);
        view.setCompoundDrawables(drawable, null, null, null);

        return view;
    }

    public boolean isDisabled() {
        return mDisabled;
    }

    public void setDisabled(boolean disabled) {
        mDisabled = disabled;
        notifyDataSetChanged();
    }
}
