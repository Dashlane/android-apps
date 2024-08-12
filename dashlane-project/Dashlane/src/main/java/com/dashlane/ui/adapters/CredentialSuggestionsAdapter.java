package com.dashlane.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.help.HelpCenterLink;
import com.dashlane.ui.InAppLoginWindow;
import com.dashlane.ui.controllers.interfaces.SuggestionPicker;
import com.dashlane.util.StringUtils;
import com.dashlane.vault.model.AuthentifiantKt;
import com.dashlane.vault.summary.SummaryObject;

import java.util.List;


public class CredentialSuggestionsAdapter
    extends ArrayAdapter<InAppLoginWindow.AuthentifiantWithWarningInfo>
    implements View.OnClickListener {

    private int mLayoutRes;
    private SuggestionPicker mPickerCallback;

    public CredentialSuggestionsAdapter(Context context, int resource,
                                        List<InAppLoginWindow.AuthentifiantWithWarningInfo> objects,
                                        SuggestionPicker pickCallback) {
        super(context, resource, objects);
        mLayoutRes = resource;
        mPickerCallback = pickCallback;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = newView();
        }
        convertView.setTag(R.id.tag_item_position, position);
        ViewHolder holder = (ViewHolder) convertView.getTag(R.id.view_holder_pattern_tag);
        InAppLoginWindow.AuthentifiantWithWarningInfo authentifiantWithWarningInfo = getItem(position);
        SummaryObject.Authentifiant authentifiant = authentifiantWithWarningInfo.authentifiant;
        String loginForUi = AuthentifiantKt.getLoginForUi(authentifiant);
        String line1 = loginForUi;
        String line2 = authentifiant.getTitle();
        if (!StringUtils.isNotSemanticallyNull(line1)) {
            line1 = getContext().getString(R.string.incomplete);
        }
        holder.account.setText(line1);
        holder.label.setText(line2);
        if (authentifiantWithWarningInfo.warningUnsafe) {
            holder.unsecureLabel.setVisibility(View.VISIBLE);
            holder.unsecureImage.setVisibility(View.VISIBLE);
            holder.unsecureImage.setOnClickListener(view -> {
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(HelpCenterLink.ARTICLE_AUTOFILL_WARNING.getAndroidUri());
                view.getContext().startActivity(i);
            });
        } else {
            holder.unsecureLabel.setVisibility(View.GONE);
            holder.unsecureImage.setVisibility(View.GONE);
        }
        return convertView;
    }

    @Override
    public void onClick(View v) {
        int position = (Integer) v.getTag(R.id.tag_item_position);
        if (mPickerCallback != null) {
            mPickerCallback.onPickSuggestion(position);
        }
    }

    private View newView() {
        View v = LayoutInflater.from(getContext()).inflate(mLayoutRes, null);
        v.setTag(R.id.view_holder_pattern_tag, new ViewHolder(v));
        if (mPickerCallback != null) {
            v.setOnClickListener(this);
        }
        return v;
    }

    static class ViewHolder {

        final ImageView icon;
        final TextView account;
        final TextView label;
        final View unsecureLabel;
        final View unsecureImage;

        ViewHolder(View v) {
            super();
            icon = v.findViewById(R.id.suggestion_icon);
            account = v.findViewById(R.id.suggestion_account);
            label = v.findViewById(R.id.suggestion_account_label);
            unsecureLabel = v.findViewById(R.id.unsecure_app_textview);
            unsecureImage = v.findViewById(R.id.unsecure_app_imageview);

        }
    }
}
