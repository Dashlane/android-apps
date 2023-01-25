package com.dashlane.ui.credential.passwordgenerator;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.StringDef;
import androidx.fragment.app.FragmentManager;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.hermes.LogRepository;
import com.dashlane.passwordstrength.PasswordStrength;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;
import com.dashlane.ui.fragments.PasswordGenerationCallback;
import com.dashlane.ui.fragments.PasswordGeneratorFragment;
import com.dashlane.useractivity.log.inject.UserActivityComponent;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.vault.model.VaultItem;
import com.dashlane.xml.domain.SyncObject;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;



@SuppressWarnings("java:S110")
public class PasswordGeneratorDialog extends NotificationDialogFragment implements PasswordGenerationCallback {

    @StringDef({Origin.EDIT_VIEW, Origin.CREATION_VIEW})
    @Retention(RetentionPolicy.SOURCE)
    public @interface Origin {
        String EDIT_VIEW = "editPasswordView";
        String CREATION_VIEW = "addPasswordView";
    }

    @Origin
    private String mOrigin;
    private String mDomainAsking;
    private Callback mPasswordCallback;

    public interface Callback {
        void onPasswordAccepted(VaultItem<SyncObject.GeneratedPassword> generatedPassword);
    }

    @Override
    public void onPasswordGenerated() {
        
        setButtonEnable(DialogInterface.BUTTON_POSITIVE, true);
    }

    @Override
    public void passwordSaved(VaultItem<SyncObject.GeneratedPassword> generatedPassword, PasswordStrength strength) {
        getPasswordGeneratorLogger().log(mOrigin, "clickUse",
                (strength == null) ? null : String.valueOf(strength.getPercentScore()));
        if (mPasswordCallback != null) {
            mPasswordCallback.onPasswordAccepted(generatedPassword);
        }
    }

    @Override
    public void passwordGeneratedColor(int color) {
        
    }

    @Override
    public void restoreDominantColor(int color) {
        
    }

    @Override
    public void showPreviouslyGenerated() {
        
    }

    @Override
    public View onCreateDialogCustomView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_dialog_password_generator, container, false);
        ViewGroup fragmentViewGroup = v.findViewById(R.id.dialogPasswordGeneratorFragment);
        View copyButton = fragmentViewGroup.findViewById(R.id.copy_generated_password);
        copyButton.setVisibility(View.GONE);

        PasswordGeneratorFragment generatorFragment = getPasswordGeneratorFragment();
        if (generatorFragment != null) {
            generatorFragment.setLog75Subtype(mOrigin);
            generatorFragment.setPasswordGenerationCallback(this);
        }
        return v;
    }

    @Override
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        getPasswordGeneratorLogger().log(mOrigin, "clickGenerate", null);
    }

    @Override
    public void onStart() {
        super.onStart();
        
        setButtonEnable(DialogInterface.BUTTON_POSITIVE, false);
    }

    @Override
    protected void onClickPositiveButton() {
        super.onClickPositiveButton();

        PasswordGeneratorFragment fragment = getPasswordGeneratorFragment();
        if (fragment != null) {
            fragment.copyAndSaveGeneratedPassword(mDomainAsking, false);
        }
    }

    @Override
    protected void onClickNegativeButton() {
        super.onClickNegativeButton();
        getPasswordGeneratorLogger().log(mOrigin, "clickCancel", null);
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        PasswordGeneratorFragment fragment = getPasswordGeneratorFragment();
        if (fragment != null) {
            getActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commitNowAllowingStateLoss();
        }
    }

    public void setOrigin(@Origin String origin) {
        mOrigin = origin;
    }

    public void setDomainAsking(String domainAsking) {
        mDomainAsking = domainAsking;
    }

    private PasswordGeneratorLogger getPasswordGeneratorLogger() {
        Context context = getContext();
        LogRepository trackingRepository = SingletonProvider.getComponent().getLogRepository();
        if (context == null) return new PasswordGeneratorLogger(null, trackingRepository);

        UsageLogRepository usageLogRepository =
                UserActivityComponent.Companion.invoke(context).getCurrentSessionUsageLogRepository();
        return new PasswordGeneratorLogger(usageLogRepository, trackingRepository);
    }

    PasswordGeneratorFragment getPasswordGeneratorFragment() {
        return (PasswordGeneratorFragment) getActivity().getSupportFragmentManager()
                                                        .findFragmentById(R.id.dialogPasswordGeneratorFragment);
    }

    public void setPasswordCallback(Callback callback) {
        mPasswordCallback = callback;
    }

    private static class StrengthNotComputedException extends Exception {
        private StrengthNotComputedException(String message) {
            super(message);
        }
    }
}
