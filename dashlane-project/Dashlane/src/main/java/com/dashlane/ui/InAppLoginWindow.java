package com.dashlane.ui;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dashlane.R;
import com.dashlane.autofill.accessibility.AccessibilityEventHandler;
import com.dashlane.autofill.accessibility.DashlaneAccessibilityService;
import com.dashlane.autofill.accessibility.alwayson.AlwaysOnEventHandler;
import com.dashlane.autofill.accessibility.alwayson.AlwaysOnUiManager;
import com.dashlane.autofill.formdetector.model.AccessibilityLoginForm;
import com.dashlane.core.helpers.PackageNameSignatureHelper;
import com.dashlane.core.helpers.PackageSignatureStatus;
import com.dashlane.debug.DaDaDa;
import com.dashlane.debug.DeveloperUtilities;
import com.dashlane.login.lock.LockManager;
import com.dashlane.navigation.NavigationConstants;
import com.dashlane.navigation.NavigationHelper;
import com.dashlane.security.DashlaneIntent;
import com.dashlane.session.SessionManager;
import com.dashlane.ui.adapters.CredentialSuggestionsAdapter;
import com.dashlane.ui.controllers.interfaces.SuggestionPicker;
import com.dashlane.util.DeviceUtils;
import com.dashlane.util.StringUtils;
import com.dashlane.vault.summary.SummaryObject;
import com.dashlane.vault.util.AuthentifiantPackageNameSignatureUtilKt;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import wei.mark.standout.StandOutWindow;
import wei.mark.standout.constants.StandOutFlags;
import wei.mark.standout.ui.Window;

@AndroidEntryPoint
public class InAppLoginWindow extends AbstractDashlaneSubwindow implements View.OnClickListener, SuggestionPicker {
    public static final int WINDOW_ID = InAppLoginWindow.class.hashCode();
    public static final int MAX_SUGGESTION_SHOWN = 3;
    static int UNLOCK_COUNT = 0;
    private static int sWindowLayoutResId = R.layout.window_dashlane_bubble_content;
    private static int sItemLayoutResId = R.layout.list_item_in_app_login_suggestion;
    private static int[] sWindowDimensions = new int[]{0, 0};
    @Inject
    PackageNameSignatureHelper packageNameSignatureHelper;
    @Inject
    DaDaDa dadada;
    @Inject
    LockManager lockManager;
    @Inject
    SessionManager sessionManager;
    private AlwaysOnEventHandler.ScanResult mLastScanResult;
    private LayoutInflater mLayoutInflater;
    private ArrayAdapter<AuthentifiantWithWarningInfo> mSuggestionAdapter;

    public static int[] getWindowDimensions(Context context, int resultCount) {
        LayoutInflater layoutInflater = LayoutInflater.from(context)
            .cloneInContext(
                new ContextThemeWrapper(context, R.style.Theme_Dashlane));
        return getWindowDimensions(context, layoutInflater,
            new FakeAdapterForMeasure(layoutInflater, sItemLayoutResId, resultCount));
    }

    private static int[] getWindowDimensions(Context context, LayoutInflater layoutInflater, BaseAdapter adapter) {
        int[] screenSize = DeviceUtils.getScreenSize(context);
        Resources resources = context.getResources();
        int maxWidth = resources.getDimensionPixelSize(R.dimen.dashlane_content_bubble_max_width);
        int dividerHeight = resources.getDimensionPixelSize(R.dimen.spacing_normal);

        int listViewHeight = 0;
        FrameLayout fakeParent = new FrameLayout(context);
        for (int i = 0; i < Math.min(adapter.getCount(), MAX_SUGGESTION_SHOWN - 1); i++) {
            if (i > 0) {
                listViewHeight += dividerHeight;
            }
            View child = adapter.getView(i, null, fakeParent);
            int measuredHeightChild = getMeasuredMaxHeight(child);
            listViewHeight += measuredHeightChild;
        }

        if (listViewHeight > 0) {
            
            listViewHeight += dividerHeight;
        }
        View footerCreateAccount = createFooterAddAccount(layoutInflater);
        int measuredCreateAccountHeight = getMeasuredMaxHeight(footerCreateAccount) + dividerHeight;
        listViewHeight += measuredCreateAccountHeight;

        View container = layoutInflater.inflate(sWindowLayoutResId, null);
        int measuredContainerHeight = getMeasuredMaxHeight(container);
        listViewHeight += measuredContainerHeight;

        sWindowDimensions = new int[]{Math.min(maxWidth, screenSize[0]), listViewHeight};
        return sWindowDimensions;
    }

    private static int getMeasuredMaxHeight(View view) {
        view.measure(
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
            View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        return view.getMeasuredHeight();
    }

    private static View createFooterAddAccount(LayoutInflater layoutInflater) {
        return layoutInflater.inflate(R.layout.list_item_in_app_login_footer_create_account, null);
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mLayoutInflater = LayoutInflater.from(this);
        mSuggestionAdapter = new CredentialSuggestionsAdapter(this,
            sItemLayoutResId,
            new ArrayList<AuthentifiantWithWarningInfo>(),
            this);
        refreshLastLoadedInformations();
    }

    @Override
    public int getFlags(int id) {
        return super.getFlags(id) |
            StandOutFlags.FLAG_WINDOW_FOCUSABLE_DISABLE;
    }

    @Override
    public String getAppName() {
        return getString(R.string.dashlane_main_app_name);
    }

    @Override
    public int getAppIcon() {
        return R.drawable.ic_notification_small_icon;
    }

    @Override
    public void createAndAttachView(int id, FrameLayout frame) {
        refreshLastLoadedInformations();

        
        int[] windowDimensions = getWindowDimensions(this, mLayoutInflater, mSuggestionAdapter);
        StandOutLayoutParams params = getStandOutLayoutParams(id, windowDimensions);
        ((Window) frame.getParent()).setLayoutParams(params);

        mLayoutInflater.inflate(sWindowLayoutResId, frame, true);
        ListView suggestionsListView = frame.findViewById(R.id.suggestions_list);
        mArrowTop = frame.findViewById(R.id.arrow_top);
        mArrowBottom = frame.findViewById(R.id.arrow_bottom);
        mArrowLeft = frame.findViewById(R.id.arrow_left);
        mArrowRight = frame.findViewById(R.id.arrow_right);
        View footerCreateAccount = createFooterAddAccount(mLayoutInflater);
        footerCreateAccount.setOnClickListener(v -> onPickAddAnAccount());
        suggestionsListView.addFooterView(footerCreateAccount);
        suggestionsListView.setAdapter(mSuggestionAdapter);
        Button closeButton = frame.findViewById(R.id.close);
        closeButton.setOnClickListener(this);

        AccessibilityLoginForm loginForm = getLastScanLoginForm();
        TextView infoLabel = frame.findViewById(R.id.info_label);
        if (loginForm == null) {
            infoLabel.setVisibility(View.GONE);
        } else {
            infoLabel.setVisibility(View.VISIBLE);
            if (loginForm.getLogin() == null) {
                infoLabel.setText(R.string.autofill_password_only);
            } else if (loginForm.getPassword() == null) {
                infoLabel.setText(R.string.autofill_login_only);
            } else {
                infoLabel.setText(R.string.autofill_login_password);
            }
        }
    }

    @Override
    public StandOutLayoutParams getParams(int id, Window window) {
        return getStandOutLayoutParams(id, sWindowDimensions);
    }

    @Override
    public Animation getShowAnimation(int id) {
        return AnimationUtils.loadAnimation(this, R.anim.grow_from_topright_to_bottomleft);
    }

    @Override
    public Animation getCloseAnimation(int id) {
        return AnimationUtils.loadAnimation(this, R.anim.shrink_from_bottomleft_to_topright);
    }

    
    @SuppressWarnings("squid:S2696")
    @Override
    public void onPickSuggestion(int position) {
        boolean isDebugLock = false;
        if (DeveloperUtilities.systemIsInDebug(getApplicationContext()) && dadada.isInAppAutologinLockDebug()) {
            isDebugLock = (UNLOCK_COUNT % 2 == 0);
            ++UNLOCK_COUNT;
        }
        var session = sessionManager.getSession();
        if (session == null) {
            return;
        }
        boolean isLock = lockManager.isInAppLoginLocked() || isDebugLock;

        SummaryObject.Authentifiant authentifiant = mSuggestionAdapter.getItem(position).authentifiant;

        AlwaysOnUiManager alwaysOnUiManager = getAlwaysOnUiManager();
        if (isLock) {
            if (alwaysOnUiManager != null) {
                
                alwaysOnUiManager.getFocusOnField();
            }
            lockManager.showLockActivityForInAppLogin(this, authentifiant.getId());
        } else {
            if (alwaysOnUiManager != null) {
                alwaysOnUiManager.onItemPicked(authentifiant.getId());
            }
        }
        StandOutWindow.closeAll(this, InAppLoginWindow.class);
    }

    public void onPickAddAnAccount() {
        String packageName = getLastScanPackageName();
        String websiteUrl = getLastScanUrl();

        boolean useUrlOverPackageName = !StringUtils.isNullOrEmpty(websiteUrl);

        Intent createNewAccount = DashlaneIntent.newInstance();
        Uri createNewAccountUri = new Uri.Builder()
            .scheme(NavigationHelper.Destination.SCHEME)
            .encodedAuthority(NavigationHelper.Destination.MainPath.PASSWORDS)
            .path(NavigationHelper.Destination.SecondaryPath.Items.NEW)
            .appendPath(useUrlOverPackageName ? websiteUrl : packageName)
            .appendQueryParameter(NavigationHelper.Destination.PathQueryParameters.IS_PACKAGE_NAME,
                Boolean.toString(!useUrlOverPackageName))
            .build();
        createNewAccount.setAction(Intent.ACTION_VIEW);
        createNewAccount.setData(createNewAccountUri);
        createNewAccount.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        createNewAccount.putExtra(NavigationConstants.HOME_FORCE_CHANGE_CONTENT, true);
        startActivity(createNewAccount);
        StandOutWindow.closeAll(this, InAppLoginWindow.class);
    }

    @Override
    public void onClick(View v) {
        if (R.id.close == v.getId()) {
            StandOutWindow.closeAll(this, InAppLoginWindow.class);
            StandOutWindow.closeAll(this, DashlaneBubble.class);
        }
    }

    @Override
    public boolean onCloseAll() {
        sendData(InAppLoginWindow.WINDOW_ID, DashlaneBubble.class, DashlaneBubble.WINDOW_ID, DashlaneBubble
            .REQUEST_CODE_IN_APP_LOGIN_CLOSED, null);
        return super.onCloseAll();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Window window = getWindow(WINDOW_ID);
        if (window != null) {
            window.refreshScreenDimensions();
        }
    }

    @NonNull
    private StandOutLayoutParams getStandOutLayoutParams(int id, int[] windowDimensions) {
        return new StandOutLayoutParams(id,
            windowDimensions[0],
            windowDimensions[1],
            StandOutLayoutParams.LEFT,
            StandOutLayoutParams.TOP
        );
    }

    private void refreshLastLoadedInformations() {
        mSuggestionAdapter.clear();
        AlwaysOnUiManager alwaysOnUiManager = getAlwaysOnUiManager();
        if (alwaysOnUiManager == null) {
            mLastScanResult = null;
            return;
        }
        AlwaysOnEventHandler.ScanResult scanResult = alwaysOnUiManager.getLastScanResult();
        mLastScanResult = scanResult;
        if (scanResult != null) {
            AccessibilityLoginForm loginForm = scanResult.getLoginForm();
            String packageName = (loginForm == null) ? null : loginForm.getPackageName();
            List<SummaryObject.Authentifiant> authentifiants = scanResult.getAuthentifiants();
            if (authentifiants != null) {
                for (int i = 0; i < authentifiants.size(); i++) {
                    SummaryObject.Authentifiant authentifiant = authentifiants.get(i);
                    boolean warning = mayBeUnsafeApplicationFor(packageName, authentifiant);
                    mSuggestionAdapter.add(new AuthentifiantWithWarningInfo(authentifiant, warning));
                }
            }
        }
    }

    private boolean mayBeUnsafeApplicationFor(String packageName, SummaryObject.Authentifiant authentifiant) {
        return packageName != null
            && AuthentifiantPackageNameSignatureUtilKt
            .getSignatureVerificationWith(authentifiant, packageNameSignatureHelper, packageName) !=
            PackageSignatureStatus.VERIFIED;
    }

    @Nullable
    private AlwaysOnUiManager getAlwaysOnUiManager() {
        AccessibilityEventHandler eventHandler = DashlaneAccessibilityService.getEventHandler();
        if (eventHandler instanceof AlwaysOnEventHandler) {
            return ((AlwaysOnEventHandler) eventHandler).getAlwaysOnUiManager();
        } else {
            return null;
        }
    }

    @Nullable
    private String getLastScanPackageName() {
        AccessibilityLoginForm loginForm = getLastScanLoginForm();
        if (loginForm == null) {
            return null;
        }
        return loginForm.getPackageName();
    }

    @Nullable
    private String getLastScanUrl() {
        AccessibilityLoginForm loginForm = getLastScanLoginForm();
        if (loginForm == null) {
            return null;
        }
        return loginForm.getWebsiteUrl();
    }

    @Nullable
    private AccessibilityLoginForm getLastScanLoginForm() {
        if (mLastScanResult == null) {
            return null;
        }
        return mLastScanResult.getLoginForm();
    }

    public static class AuthentifiantWithWarningInfo {
        public final SummaryObject.Authentifiant authentifiant;
        public final boolean warningUnsafe;

        public AuthentifiantWithWarningInfo(SummaryObject.Authentifiant authentifiant, boolean warningUnsafe) {
            this.authentifiant = authentifiant;
            this.warningUnsafe = warningUnsafe;
        }
    }

    private static class FakeAdapterForMeasure extends BaseAdapter {

        private final LayoutInflater mLayoutInflater;
        private final int mLayoutRes;
        private final int mCount;

        public FakeAdapterForMeasure(LayoutInflater layoutInflater, int layoutRes, int count) {
            mLayoutInflater = layoutInflater;
            mLayoutRes = layoutRes;
            mCount = count;
        }

        @Override
        public int getCount() {
            return mCount;
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            if (view == null) {
                return mLayoutInflater.inflate(mLayoutRes, viewGroup, false);
            } else {
                return view;
            }
        }
    }

}
