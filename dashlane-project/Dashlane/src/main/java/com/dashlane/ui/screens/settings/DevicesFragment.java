package com.dashlane.ui.screens.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.dashlane.R;
import com.dashlane.dagger.singleton.SingletonComponentProxy;
import com.dashlane.dagger.singleton.SingletonProvider;
import com.dashlane.hermes.generated.definitions.AnyPage;
import com.dashlane.server.api.DashlaneApi;
import com.dashlane.server.api.endpoints.devices.ListDevicesService.Data.Device;
import com.dashlane.session.Session;
import com.dashlane.ui.activities.fragments.AbstractContentFragment;
import com.dashlane.ui.dialogs.fragments.NotificationDialogFragment;
import com.dashlane.util.DeviceListManager;
import com.dashlane.util.PageViewUtil;
import com.dashlane.vault.model.DeviceType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import kotlin.Unit;
import kotlinx.coroutines.Job;
import kotlinx.coroutines.SupervisorKt;

public class DevicesFragment extends AbstractContentFragment {

    private static final String FRAGMENT_TAG_INFO_DIALOG = "device_info_dialog";
    private static final String FRAGMENT_TAG_CONFIRM_DELETE_DIALOG = "device_delete_dialog";

    private GridView mListView;
    private ProgressBar mProgressBar;
    private TextView mProgressLabel;

    @Nullable
    private DeviceListManager mDeviceListManager;
    private Job mJob = SupervisorKt.SupervisorJob(null);

    private List<Device> mDeviceList = new ArrayList<>();
    private Device mUserSelectedDevice;
    private DialogFragment mInfoDialog;
    private DialogFragment mConfirmDeleteDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SingletonComponentProxy singletonComponent = SingletonProvider.getComponent();
        Session session = SingletonProvider.getSessionManager().getSession();
        if (session != null) {
            DashlaneApi dashlaneApi = singletonComponent.getDashlaneApi();
            mDeviceListManager = new DeviceListManager(mJob, session, dashlaneApi, (devices) -> {
                mDeviceList.clear();
                mDeviceList.addAll(devices);
                setupListView(true);
                return Unit.INSTANCE;
            });
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        refreshDevices();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        PageViewUtil.setCurrentPageView(this, AnyPage.SETTINGS_DEVICE_LIST);
        View view = inflater.inflate(R.layout.fragment_devices_list, container, false);
        mListView = view.findViewById(R.id.device_list);
        mProgressBar = view.findViewById(R.id.progressbar);
        mProgressLabel = view.findViewById(R.id.progressbar_label);
        setupListView(false);

        
        FragmentManager manager = getFragmentManager();
        if (manager != null) {
            DialogFragment previousDelete =
                    (DialogFragment) manager.findFragmentByTag(FRAGMENT_TAG_CONFIRM_DELETE_DIALOG);
            if (previousDelete != null) {
                previousDelete.dismissAllowingStateLoss();
            }
            DialogFragment previousInfoDialog = (DialogFragment) manager.findFragmentByTag(FRAGMENT_TAG_INFO_DIALOG);
            if (previousInfoDialog != null) {
                previousInfoDialog.dismissAllowingStateLoss();
            }
        }

        return view;
    }

    private void showDeviceInfoDialog() {
        if (mInfoDialog != null && mInfoDialog.isVisible()) {
            mInfoDialog.dismissAllowingStateLoss();
        }
        mInfoDialog = setupInfoDialog();
        mInfoDialog.show(getFragmentManager(), FRAGMENT_TAG_INFO_DIALOG);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mJob.cancel(null);
    }

    private DialogFragment setupConfirmDeleteDialog() {
        NotificationDialogFragment.TwoButtonClicker clicker = new NotificationDialogFragment.TwoButtonClicker() {
            @Override
            public void onPositiveButton() {
                deleteDevice(mUserSelectedDevice);
            }

            @Override
            public void onNegativeButton() {

            }
        };
        return new NotificationDialogFragment.Builder().setTitle(getString(R.string.delete_device_dialog_title))
                                                           .setMessage(getString(R.string.delete_device_dialog_body))
                                                           .setNegativeButtonText(getString(R.string.cancel))
                                                           .setPositiveButtonText(getString(R.string.delete_device))
                                                           .setCancelable(true)
                                                           .setClicker(clicker).setClickPositiveOnCancel(false)
                                                           .build();
    }

    private void showConfirmDeleteDialog() {
        if (mConfirmDeleteDialog != null && mConfirmDeleteDialog.isVisible()) {
            mConfirmDeleteDialog.dismissAllowingStateLoss();
        }
        mConfirmDeleteDialog = setupConfirmDeleteDialog();
        mConfirmDeleteDialog.show(getFragmentManager(), FRAGMENT_TAG_CONFIRM_DELETE_DIALOG);

    }

    private DialogFragment setupInfoDialog() {
        NotificationDialogFragment.TwoButtonClicker clicker = new NotificationDialogFragment.TwoButtonClicker() {
            @Override
            public void onPositiveButton() {
            }

            @Override
            public void onNegativeButton() {
                showConfirmDeleteDialog();
            }
        };
        StringBuilder devInfo = new StringBuilder();
        devInfo.append("\n")
               .append(getString(R.string.device_name))
               .append("\n")
               .append(mUserSelectedDevice.getName());
        String deviceId = SingletonProvider.getSessionManager().getSession().getDeviceId();
        boolean isCurrentDevice = deviceId.equals(mUserSelectedDevice.getId());
        if (isCurrentDevice) {
            devInfo.append("\n")
                   .append(getString(R.string.device_this_device).toUpperCase(Locale.getDefault()));
        }
        devInfo.append("\n\n")
               .append(getString(R.string.device_type))
               .append("\n")
               .append(getString(DeviceType.forValue(mUserSelectedDevice.getPlatform()).getNameResId()))
               .append("\n\n")
               .append(getString(R.string.device_created))
               .append("\n")
               .append(DeviceFragmentUtilsKt.getFormattedCreationDate(mUserSelectedDevice))
               .append("\n\n")
               .append(getString(R.string.device_lastupdate))
               .append("\n")
               .append(DeviceFragmentUtilsKt.getFormattedUpdateDate(mUserSelectedDevice));

        String deleteDevice = getString(R.string.delete_device);
        if (isCurrentDevice) {
            deleteDevice = null;
        }

        return new NotificationDialogFragment.Builder().setTitle(getString(R.string.device_dialog_title))
                                                           .setMessage(devInfo.toString())
                                                           .setNegativeButtonText(deleteDevice)
                                                           .setPositiveButtonText(getString(R.string.ok))
                                                           .setCancelable(true).setClicker(clicker)
                                                           .setClickPositiveOnCancel(false).build();
    }

    private void deleteDevice(final Device d) {
        DeviceListManager deviceListManager = mDeviceListManager;
        if (deviceListManager != null) {
            deviceListManager.deleteAsync(d, () -> {
                mDeviceList.remove(d);
                setupListView(true);
                refreshDevices();
                return Unit.INSTANCE;
            });
        }
    }

    private void setupListView(boolean result) {
        if (result) {
            mProgressBar.setVisibility(View.GONE);
            if (mDeviceList == null || mDeviceList.size() == 0) {
                mProgressLabel.setText(getText(R.string.loading_devices_error));
            } else {
                mProgressLabel.setVisibility(View.GONE);
            }
        }
        mListView.setAdapter(new DeviceListAdapter(mDeviceList));
        mListView.setOnItemClickListener((parent, view, position, id) -> {
            mUserSelectedDevice = mDeviceList.get(position);
            showDeviceInfoDialog();
        });
    }

    private void refreshDevices() {
        DeviceListManager deviceListManager = mDeviceListManager;
        if (deviceListManager != null) {
            deviceListManager.refresh();
        }
    }

    private class DeviceListAdapter extends BaseAdapter {

        private List<Device> aDeviceList;

        public DeviceListAdapter(List<Device> deviceList) {
            aDeviceList = new ArrayList<>(deviceList);
        }

        @Override
        public int getCount() {
            return aDeviceList.size();
        }

        @Override
        public Device getItem(int position) {
            return aDeviceList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = newView();
            }
            bindView(convertView, position);
            return convertView;
        }

        private View newView() {
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            return inflater.inflate(R.layout.item_device, null);
        }

        private void bindView(View convertView, int position) {
            Device device = aDeviceList.get(position);
            TextView name = convertView.findViewById(R.id.device_title);
            TextView type = convertView.findViewById(R.id.device_subtitle);
            ImageView thumb = convertView.findViewById(R.id.device_image);
            name.setText(device.getName());

            DeviceType platform = DeviceType.forValue(device.getPlatform());
            type.setText(platform.getNameResId());
            thumb.setImageResource(platform.getIconResId());
        }
    }
}
