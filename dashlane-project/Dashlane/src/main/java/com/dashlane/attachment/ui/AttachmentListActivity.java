package com.dashlane.attachment.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwnerKt;

import com.dashlane.R;
import com.dashlane.announcements.AnnouncementCenter;
import com.dashlane.attachment.AttachmentListContract;
import com.dashlane.attachment.AttachmentListDataProvider;
import com.dashlane.attachment.VaultItemLogAttachmentHelper;
import com.dashlane.core.DataSync;
import com.dashlane.lock.LockHelper;
import com.dashlane.permission.PermissionsManager;
import com.dashlane.securefile.DeleteFileManager;
import com.dashlane.securefile.DownloadFileContract;
import com.dashlane.securefile.UploadFileContract;
import com.dashlane.securefile.storage.SecureFileStorage;
import com.dashlane.session.SessionManager;
import com.dashlane.storage.userdata.accessor.MainDataAccessor;
import com.dashlane.storage.userdata.accessor.filter.VaultFilter;
import com.dashlane.ui.activities.DashlaneActivity;
import com.dashlane.util.userfeatures.UserFeaturesChecker;
import com.dashlane.vault.VaultItemLogger;
import com.dashlane.vault.model.VaultItem;
import com.dashlane.xml.domain.SyncObjectType;
import com.google.gson.Gson;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import kotlin.Unit;

@AndroidEntryPoint
public class AttachmentListActivity extends DashlaneActivity {

    static final String LOG_TAG = "ATTACHMENTS";

    public static final String ITEM_ID = "itemId";
    public static final String ITEM_TYPE = "itemType";
    public static final String ITEM_ATTACHMENTS = "itemAttachments";
    public static final String ITEM_COLOR = "itemColor";
    public static final int REQUEST_CODE_ATTACHMENT_LIST = 123;
    public static final String EXTRA_ATTACHMENTS_STRING = "attachments";
    public static final String EXTRA_HAVE_ATTACHMENTS_CHANGED = "haveAttachmentsChanged";

    @Inject
    SessionManager mSessionManager;
    @Inject
    MainDataAccessor mMainDataAccessor;

    @Inject
    DataSync mDataSync;

    @Inject
    UploadFileContract.DataProvider mUploadFileDataProvider;
    @Inject
    DownloadFileContract.DataProvider mDownloadFileDataProvider;
    @Inject
    SecureFileStorage mSecureFileStorage;

    @Inject
    VaultItemLogger mVaultItemLogger;

    @Inject
    DeleteFileManager deleteFileManager;

    @Inject
    PermissionsManager mPermissionsManager;

    @Inject
    UserFeaturesChecker mUserFeaturesChecker;

    @Inject
    AnnouncementCenter mAnnouncementCenter;

    AttachmentListContract.DataProvider mAttachmentListProvider;

    private AttachmentListPresenter mAttachmentListPresenter;
    private UploadAttachmentsPresenter mUploadAttachmentsPresenter;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        if (mAttachmentListPresenter != null) mAttachmentListPresenter.onCreateOptionsMenu(getMenuInflater(), menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onApplicationUnlocked() {
        super.onApplicationUnlocked();
        
        mUploadAttachmentsPresenter.resumeLockedFileUpload();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return mAttachmentListPresenter.onOptionsItemSelected(item);
    }

    @Override
    public void finish() {
        if (mAttachmentListProvider != null && mAttachmentListPresenter != null) {
            setAttachmentsResult(mAttachmentListProvider, mAttachmentListPresenter);
        } else {
            
            setResult(Activity.RESULT_CANCELED);
        }
        super.finish();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_attachment_list);
        getActionBarUtil().setup();

        Bundle extras = getIntent().getExtras();
        if (extras == null) {
            finish();
            return;
        }
        SyncObjectType itemType = SyncObjectType.Companion.forXmlNameOrNull(extras.getString(ITEM_TYPE));
        String itemId = extras.getString(ITEM_ID);
        VaultItem vaultItem = null;
        if (itemType != null && itemId != null) {
            vaultItem = mMainDataAccessor.getVaultDataQuery().query(new VaultFilter(itemId, itemType));
        }
        if (vaultItem == null) {
            finish();
            return;
        }

        final VaultItemLogAttachmentHelper vaultItemLogAttachmentHelper =
                new VaultItemLogAttachmentHelper(mVaultItemLogger, vaultItem);
        LockHelper lockHelper = getLockHelper();
        String attachments;
        boolean isAttachmentListUpdated = false;
        if (savedInstanceState != null) {
            attachments = savedInstanceState.getString(EXTRA_ATTACHMENTS_STRING);
            isAttachmentListUpdated = savedInstanceState.getBoolean(EXTRA_HAVE_ATTACHMENTS_CHANGED);
        } else {
            attachments = getIntent().getStringExtra(ITEM_ATTACHMENTS);
        }
        mAttachmentListProvider =
                new AttachmentListDataProvider(attachments,
                        vaultItem,
                        mMainDataAccessor.getDataSaver(),
                        mDataSync,
                        mSecureFileStorage);

        ActivityResultLauncher<Unit> openDocumentResultLauncher =
                registerForActivityResult(new OpenDocumentResultContract(), this::onOpenDocumentResult);
        mUploadAttachmentsPresenter =
                new UploadAttachmentsPresenter(mUserFeaturesChecker, lockHelper,
                        LifecycleOwnerKt.getLifecycleScope(this),
                        mSessionManager,
                        vaultItemLogAttachmentHelper,
                        openDocumentResultLauncher);
        mUploadAttachmentsPresenter.setProvider(mUploadFileDataProvider);
        mUploadAttachmentsPresenter.setView(new UploadAttachmentsViewProxy(this));

        DownloadAttachmentsPresenter downloadPresenter = new DownloadAttachmentsPresenter(
                LifecycleOwnerKt.getLifecycleScope(this),
                vaultItemLogAttachmentHelper);
        downloadPresenter.setProvider(mDownloadFileDataProvider);
        downloadPresenter.setView(new DownloadAttachmentsViewProxy(this));

        int actionBarColor = extras.getInt(ITEM_COLOR);
        mAttachmentListPresenter =
                new AttachmentListPresenter(LifecycleOwnerKt.getLifecycleScope(this), actionBarColor,
                        mUploadAttachmentsPresenter,
                        downloadPresenter, deleteFileManager, lockHelper,
                        mPermissionsManager,
                        vaultItemLogAttachmentHelper);
        mAttachmentListPresenter.setAttachmentListUpdated(isAttachmentListUpdated);
        mAttachmentListPresenter.setProvider(mAttachmentListProvider);
        mAttachmentListPresenter.setView(new AttachmentListViewProxy(this));
        mAttachmentListPresenter.onCreate();

        
        mAnnouncementCenter.disable();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!getApplicationLocked()) {
            mAttachmentListPresenter.onResume();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        
        outState.putString(EXTRA_ATTACHMENTS_STRING, new Gson().toJson(mAttachmentListProvider.getAttachments()));
        outState.putBoolean(EXTRA_HAVE_ATTACHMENTS_CHANGED, mAttachmentListPresenter.isAttachmentListUpdated());
        super.onSaveInstanceState(outState);
    }

    private void onOpenDocumentResult(Uri uri) {
        mAttachmentListPresenter.onOpenDocumentResult(uri);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            mAnnouncementCenter.restorePreviousState();
        }
    }

    private void setAttachmentsResult(@NonNull AttachmentListContract.DataProvider provider,
                                      @NonNull AttachmentListContract.Presenter presenter) {
        Intent result = new Intent();
        boolean attachmentsChanged = presenter.isAttachmentListUpdated();
        result.putExtra(EXTRA_HAVE_ATTACHMENTS_CHANGED, attachmentsChanged);
        result.putExtra(EXTRA_ATTACHMENTS_STRING, new Gson().toJson(provider.getAttachments()));
        setResult(Activity.RESULT_OK, result);
    }
}