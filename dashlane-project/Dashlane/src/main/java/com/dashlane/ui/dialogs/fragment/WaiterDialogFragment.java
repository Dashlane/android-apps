package com.dashlane.ui.dialogs.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;

import java.lang.ref.WeakReference;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;

public class WaiterDialogFragment extends DialogFragment {

    public static final String TAG = WaiterDialogFragment.class.getName();

    private static final String ARGS_TITLE = "ARGS_TITLE";
    private static final String ARGS_DESCRIPTION = "ARGS_DESCRIPTION";

    @NonNull
    private static WeakReference<WaiterDialogFragment> sLastDialog = new WeakReference<>(null);

    private String mQuestion;
    private View mView;

    private static WaiterDialogFragment newInstance(String title, String question) {
        WaiterDialogFragment fragment = new WaiterDialogFragment();
        Bundle args = new Bundle();
        args.putString(ARGS_TITLE, title);
        args.putString(ARGS_DESCRIPTION, question);
        fragment.setArguments(args);
        return fragment;
    }

    public static void showWaiter(boolean cancelable, String title, String question,
                                  FragmentManager fm) {
        WaiterDialogFragment dialog = (WaiterDialogFragment) fm.findFragmentByTag(WaiterDialogFragment.TAG);
        if (dialog == null) {
            dialog = WaiterDialogFragment.newInstance(title, question);
            dialog.setCancelable(cancelable);
            dialog.show(fm, WaiterDialogFragment.TAG);
            sLastDialog = new WeakReference<>(dialog);
        }
    }

    public static void dismissWaiter(FragmentManager fm) {
        if (fm == null) {
            return;
        }
        WaiterDialogFragment dialog = (WaiterDialogFragment) fm.findFragmentByTag(WaiterDialogFragment.TAG);
        if (dialog == null) {
            WaiterDialogFragment dialogFragment = sLastDialog.get();
            if (dialogFragment != null) {
                dialogFragment.dismiss();
            }
        } else {
            dialog.dismiss();
        }
        sLastDialog = new WeakReference<>(null);
    }

    public static void updateWaiterDescription(FragmentManager fm, boolean cancelable, String message) {
        WaiterDialogFragment dialog = (WaiterDialogFragment) fm.findFragmentByTag(WaiterDialogFragment.TAG);
        if (dialog != null) {
            dialog.setCancelable(cancelable);
            dialog.updateWaiterDescription(message);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, 0);
        parseArguments();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout
                                                                                                                   .progress_dialog,
                                                                                                           null);
        ((TextView) mView.findViewById(R.id.question)).setText(mQuestion);
        return mView;
    }

    @Override
    public void onResume() {
        super.onResume();
        
        SingletonProvider.getAnnouncementCenter().disable();
    }

    @Override
    public void onStop() {
        super.onStop();
        
        SingletonProvider.getAnnouncementCenter().restorePreviousStateIfDisabled();
    }

    public void updateWaiterDescription(String message) {
        if (mView != null) {
            ((TextView) mView.findViewById(R.id.question)).setText(message);
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null)
            getDialog().setDismissMessage(null);
        super.onDestroyView();
    }

    private void parseArguments() {
        if (getArguments() != null) {
            mQuestion = getArguments().getString(ARGS_DESCRIPTION);
        }
    }

}
