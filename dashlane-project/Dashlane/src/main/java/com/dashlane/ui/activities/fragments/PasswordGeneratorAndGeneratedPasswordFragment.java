package com.dashlane.ui.activities.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.dashlane.R;
import com.dashlane.hermes.generated.definitions.AnyPage;
import com.dashlane.passwordstrength.PasswordStrength;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.ui.activities.MenuContainer;
import com.dashlane.ui.fragments.PasswordGenerationCallback;
import com.dashlane.ui.fragments.PasswordGeneratorFragment;
import com.dashlane.ui.screens.activities.GeneratedPasswordHistoryActivity;
import com.dashlane.ui.util.ActionBarUtil;
import com.dashlane.util.PageViewUtil;
import com.dashlane.vault.model.VaultItem;
import com.dashlane.xml.domain.SyncObject;

public class PasswordGeneratorAndGeneratedPasswordFragment extends AbstractContentFragment implements
                                                                                           PasswordGenerationCallback {

    private View mView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PageViewUtil.setCurrentPageView(this, AnyPage.TOOLS_PASSWORD_GENERATOR);
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.simple_frame_layout, container, false);


        setHasOptionsMenu(true);
        return mView;
    }

    @Override
    public void onStart() {
        super.onStart();
        DashlaneActivity dashlaneActivity = (DashlaneActivity) getActivity();
        ActionBarUtil actionBarUtil = dashlaneActivity.getActionBarUtil();
        actionBarUtil.restoreDefaultActionBarColor();

        PasswordGeneratorFragment pgf =
                PasswordGeneratorFragment.newInstance(dashlaneActivity instanceof MenuContainer);
        pgf.setToolbarRef(actionBarUtil.getToolbar());
        getFragmentManager().beginTransaction().replace(R.id.simple_frame_layout, pgf, PasswordGeneratorFragment.TAG)
                            .commit();
        pgf.setPasswordGenerationCallback(this);
    }

    @Override
    public void onStop() {
        DashlaneActivity dashlaneActivity = (DashlaneActivity) getActivity();
        ActionBarUtil actionBarUtil = dashlaneActivity.getActionBarUtil();
        actionBarUtil.restoreDefaultActionBarColor();
        Fragment previousPwdgen = getFragmentManager().findFragmentByTag(PasswordGeneratorFragment.TAG);
        if (previousPwdgen != null) {
            getFragmentManager().beginTransaction().remove(previousPwdgen).commitAllowingStateLoss();
        }
        super.onStop();
    }

    @Override
    public void onPasswordGenerated() {
        
    }

    @Override
    public void passwordSaved(VaultItem<SyncObject.GeneratedPassword> generatedPassword, PasswordStrength strength) {
        PasswordGeneratorFragment pgf =
                (PasswordGeneratorFragment) getFragmentManager().findFragmentByTag(PasswordGeneratorFragment.TAG);
        if (pgf != null) {
            pgf.refreshPreviouslyGeneratedPasswordButton();
        }
    }

    @Override
    public void restoreDominantColor(int color) {
        DashlaneActivity activity = (DashlaneActivity) getActivity();
        if (activity instanceof MenuContainer) {
            activity.getActionBarUtil().setActionBarColor(color);
        }
    }

    @Override
    public void showPreviouslyGenerated() {
        startActivity(new Intent(getActivity(), GeneratedPasswordHistoryActivity.class));
    }
}
