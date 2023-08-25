package com.dashlane.security.identitydashboard.password;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.lifecycle.LifecycleCoroutineScope;
import androidx.lifecycle.LifecycleOwnerKt;

import com.dashlane.R;
import com.dashlane.navigation.Navigator;
import com.dashlane.session.BySessionRepository;
import com.dashlane.session.SessionManager;
import com.dashlane.teamspaces.manager.TeamspaceAccessor;
import com.dashlane.ui.activities.fragments.AbstractContentFragment;
import com.dashlane.useractivity.log.usage.UsageLogRepository;
import com.dashlane.util.Toaster;
import com.dashlane.util.inject.OptionalProvider;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class PasswordAnalysisFragment extends AbstractContentFragment {

    @Inject
    PasswordAnalysisDataProvider mDataProvider;

    @Inject
    SessionManager mSessionManager;

    @Inject
    BySessionRepository<UsageLogRepository> mBySessionUsageLogRepository;

    @Inject
    OptionalProvider<TeamspaceAccessor> mTeamspaceAccessorProvider;
    private PasswordAnalysisPresenter mPresenter;

    @Inject
    Toaster mToaster;

    @Inject
    Navigator mNavigator;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_dashboard_password_analysis, container, false);

        PasswordAnalysisContract.ViewProxy viewProxy = new PasswordAnalysisViewProxy(view);

        if (mPresenter == null) {
            LifecycleCoroutineScope scope = LifecycleOwnerKt.getLifecycleScope(getActivity());
            mPresenter = new PasswordAnalysisPresenter(scope, mToaster, mNavigator);
            PasswordAnalysisLogger logger =
                    new PasswordAnalysisLogger(
                            mSessionManager, mBySessionUsageLogRepository,
                            mTeamspaceAccessorProvider, savedInstanceState == null);
            Bundle bundle = getArguments();
            if (bundle != null) {
                PasswordAnalysisFragmentArgs args = PasswordAnalysisFragmentArgs.fromBundle(bundle);
                mPresenter.setDefaultDestination(args.getTab());
                logger.setOrigin(args.getOrigin());
                if (savedInstanceState == null) {
                    mPresenter.setFocusBreachIdPending(args.getBreachFocus());
                }
            }
            mPresenter.setLogger(logger);
        }
        mPresenter.setProvider(mDataProvider);
        mPresenter.setView(viewProxy);
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mPresenter.onViewVisible();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.onViewHidden();
    }
}
