package com.dashlane.ui.adapters.viewedit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.vault.summary.SummaryObject;
import com.dashlane.xml.domain.SyncObject;


public class SecureNoteCategoryArrayAdapter extends ArrayAdapter<SummaryObject.SecureNoteCategory> {

    private int mLayoutResId;
    private SummaryObject.SecureNoteCategory[] mData;
    private int mSelectedPosition;

    public SecureNoteCategoryArrayAdapter(Context context, int resource, SummaryObject.SecureNoteCategory[] objects, int
            selectedPos) {
        super(context, resource, objects);
        mLayoutResId = resource;
        mData = objects;
        mSelectedPosition = selectedPos;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        NoteCategoryHolder holder;
        if (convertView != null) {
            holder = (NoteCategoryHolder) convertView.getTag(R.id.view_holder_pattern_tag);
        } else {
            convertView = LayoutInflater.from(getContext()).inflate(mLayoutResId, parent, false);
            holder = new NoteCategoryHolder(convertView);
            convertView.setTag(R.id.view_holder_pattern_tag, holder);
        }
        if (position == mSelectedPosition) {
            holder.categoryCheck.setVisibility(View.VISIBLE);
        } else {
            holder.categoryCheck.setVisibility(View.GONE);
        }
        holder.categoryName.setText(mData[position].getCategoryName());
        return convertView;
    }

    static class NoteCategoryHolder {

        TextView categoryName;
        ImageView categoryCheck;

        NoteCategoryHolder(View v) {
            categoryName = v.findViewById(R.id.secure_note_category_name);
            categoryCheck = v.findViewById(R.id.secure_note_category_check);
        }
    }
}
