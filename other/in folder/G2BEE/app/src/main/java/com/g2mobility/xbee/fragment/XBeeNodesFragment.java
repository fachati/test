package com.g2mobility.xbee.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.api.G2Bee;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.RemoteG2BeeDevice;
import com.g2mobility.xbee.recycler.XBeeDeviceAdapter;
import com.g2mobility.xbee.recycler.XBeeNodeItemDecoration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Fragment for XBee node discover and selection.
 *
 * @author Hanyu Li
 */
public class XBeeNodesFragment extends Fragment implements View.OnClickListener {
    private static final String TAG = "G2Bee";

    private XBeeDeviceAdapter mAdapter;
    private boolean mIsDiscovering = false;

    private G2Bee mG2Bee;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {

                        Thread.sleep(1500);
                    } catch (InterruptedException ignored) {
                    }
                    updateDevices(mG2Bee.getLocalXBeeDevices(), mG2Bee.getRemoteXBeeDevices());
                }
            }).start();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mAdapter = new XBeeDeviceAdapter();
        mG2Bee = new G2Bee(getActivity(), TAG);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setRetainInstance(true);

        View view = inflater.inflate(R.layout.fragment_xbee_nodes, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_xbee_nodes);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.offsetChildrenVertical(getResources().getDimensionPixelSize(
                R.dimen.recycler_view_offset));

        recyclerView.addItemDecoration(new XBeeNodeItemDecoration());
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);

        FloatingActionButton fabDiscover = (FloatingActionButton) view.findViewById(R.id
                .fab_discover);
        fabDiscover.setOnClickListener(this);

        if (savedInstanceState != null
                && savedInstanceState.containsKey(getString(R.string.key_xbee_selection))) {
            String address = savedInstanceState.getString
                    (getString(R.string.key_xbee_selection));
            if (address != null) {
                mAdapter.setSelection(new XBee64BitAddress(address));
            }
        }

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException ignored) {
                }
                updateDevices(mG2Bee.getLocalXBeeDevices(), mG2Bee.getRemoteXBeeDevices());
            }
        }).start();
    }

    @Override
    public void onPause() {
        super.onPause();

        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        if (mAdapter.getSelectedAddress() != null) {
            savedInstanceState.putString(getString(R.string.key_xbee_selection),
                    HexUtils.prettyHexString(mAdapter.getSelectedAddress().getValue()));
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_g2bee, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_read_radio_configurations:
                // Read selected XBee radio configurations
                ((G2BeeActivity) getActivity()).readRadioConfigurations();
                return true;
            case R.id.action_software_reset:
                // Do software reset on the selected XBee node
                ((G2BeeActivity) getActivity()).softwareReset();
                return true;
            case R.id.action_restore_defaults:
                // Restore the selected XBee node to factory defaults
                ((G2BeeActivity) getActivity()).restoreDefaults();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public int getSelectedPosition() {
        return mAdapter.getSelectedPosition();
    }

    public XBee64BitAddress getSelectedAddress() {
        return mAdapter.getSelectedAddress();
    }

    public List<G2BeeDevice> getXBeeDevices() {
        List<G2BeeDevice> devices = new ArrayList<>();
        for (int i = 0; i < mAdapter.getItemCount(); i++) {
            devices.add(mAdapter.getDevice(i));
        }

        return devices;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_discover:
                if (!mIsDiscovering) {
                    mIsDiscovering = true;
                    discoverXBeeNodes();
                }
                break;
        }
    }

    /**
     * Update local devices and remote devices on the network without doing discovery.
     */
    private void updateDevices(final List<G2BeeDevice> localDevices,
            final List<RemoteG2BeeDevice> remoteDevices) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAdapter.setLocalDevices(localDevices);
                List<XBee64BitAddress> toRemove = new ArrayList<>();

                for (int i = 1; i < mAdapter.getItemCount(); i++) {
                    G2BeeDevice device = mAdapter.getDevice(i);
                    if (device.isRemote()) {
                        boolean found = false;
                        for (RemoteG2BeeDevice remoteDevice : remoteDevices) {
                            if (remoteDevice.getXbee64BitAddress()
                                    .equals(device.getXbee64BitAddress())) {
                                found = true;
                                break;
                            }
                        }
                        if (!found) {
                            toRemove.add(device.getXbee64BitAddress());
                        }
                    }
                }

                for (XBee64BitAddress addressToRemove : toRemove) {
                    mAdapter.removeDevice(addressToRemove);
                }
                for (RemoteG2BeeDevice node : remoteDevices) {
                    mAdapter.addRemoteDevice(node);
                }
            }
        });
    }

    /**
     * Do XBee node discovery.
     */
    private void discoverXBeeNodes() {
        final MaterialDialog progressDialog = new MaterialDialog.Builder(getActivity())
                .content(R.string.discovering)
                .cancelable(false)
                .progress(false, 100, false)
                .negativeText(R.string.button_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        final AsyncTask<Void, Integer, List<RemoteG2BeeDevice>> task = new AsyncTask<Void, Integer,
                List<RemoteG2BeeDevice>>() {

            @Override
            protected List<RemoteG2BeeDevice> doInBackground(Void... params) {
                final List<RemoteG2BeeDevice> remoteDevices = new ArrayList<>();
                List<G2BeeDevice> localDevices = mG2Bee.getLocalXBeeDevices();
                int part = 50;
                for (G2BeeDevice device : localDevices) {
                    try {
                        byte[] timeoutValue = mG2Bee.getParameter(device.getXbee64BitAddress(),
                                "NT");
                        int timeout = ByteUtils.byteArrayToInt(timeoutValue);
                        if (timeout + 20 > part) {
                            part = timeout + 20;
                        }
                    } catch (XBeeException ignored) {
                    }
                }
                if (!localDevices.isEmpty()) {
                    ExecutorService pool = Executors.newFixedThreadPool(localDevices.size());

                    for (final G2BeeDevice device : localDevices) {
                        pool.submit(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    List<RemoteG2BeeDevice> discover = mG2Bee.discover(device
                                            .getXbee64BitAddress());
                                    remoteDevices.addAll(discover);
                                } catch (XBeeException e) {
                                    Log.e(TAG, "Cannot discover xbee nodes", e);
                                }
                            }
                        });
                    }
                }
                int progress = 0;
                while (progress < 100 && !isCancelled()) {
                    try {
                        Thread.sleep(part);
                        progress += 1;
                        publishProgress(progress);
                    } catch (InterruptedException e) {
                        Log.w(TAG, "Waiting for node discovery interrupted");
                    }
                }
                return remoteDevices;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(List<RemoteG2BeeDevice> devices) {
                if (isCancelled()) {
                    progressDialog.dismiss();
                } else {
                    updateDevices(mG2Bee.getLocalXBeeDevices(), devices);
                    progressDialog.dismiss();
                }
            }
        };
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                task.cancel(true);
                mIsDiscovering = false;
                ((G2BeeActivity) getActivity()).unlockScreenOrientation();
            }
        });

        ((G2BeeActivity) getActivity()).lockScreenOrientation();
        progressDialog.show();
        task.execute();
    }

}
