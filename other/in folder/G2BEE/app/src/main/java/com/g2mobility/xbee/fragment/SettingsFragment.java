package com.g2mobility.xbee.fragment;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.g2mobility.xbee.R;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Fragment for application settings.
 *
 * @author Hanyu Li
 */
public class SettingsFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener, View.OnClickListener, CompoundButton
        .OnCheckedChangeListener {

    public static final String PREF_USE_DEVICE = "use_device";
    public static final String PREF_BAUD_RATE = "baud_rate";
    public static final String PREF_DATA_BITS = "data_bits";
    public static final String PREF_STOP_BITS = "stop_bits";
    public static final String PREF_PARITY = "parity";

    private SharedPreferences mSharedPreferences;

    private final Map<String, UsbDevice> mUsbDevices = new HashMap<>();
    private String mSelectedDevice;

    private String[] mBaudRateEntries;
    private String[] mBaudRateValues;

    private String[] mDataBitsEntries;
    private String[] mDataBitsValues;

    private String[] mStopBitsEntries;
    private String[] mStopBitsValues;

    private String[] mParityEntries;
    private String[] mParityValues;

    private ViewGroup mLayoutUsbDevice;
    private TextView mTextViewLabelUsbDevice;
    private TextView mTextViewUsbDevice;

    private ViewGroup mLayoutUseDevice;
    private TextView mTextViewLabelUseDevice;
    private TextView mTextViewSummaryUseDevice;
    private CheckBox mCheckBoxUseDevice;

    private ViewGroup mLayoutBaudRate;
    private TextView mTextViewLabelBaudRate;
    private TextView mTextViewBaudRate;

    private ViewGroup mLayoutDataBits;
    private TextView mTextViewLabelDataBits;
    private TextView mTextViewDataBits;

    private ViewGroup mLayoutStopBits;
    private TextView mTextViewLabelStopBits;
    private TextView mTextViewStopBits;

    private ViewGroup mLayoutParity;
    private TextView mTextViewLabelParity;
    private TextView mTextViewParity;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUsbDevices();
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(getActivity());

        mBaudRateEntries = getResources().getStringArray(R.array.baud_rate_entries);
        mBaudRateValues = getResources().getStringArray(R.array.baud_rate_entry_values);

        mDataBitsEntries = getResources().getStringArray(R.array.data_bits_entries);
        mDataBitsValues = getResources().getStringArray(R.array.data_bits_entry_values);

        mStopBitsEntries = getResources().getStringArray(R.array.stop_bits_entries);
        mStopBitsValues = getResources().getStringArray(R.array.stop_bits_entry_values);

        mParityEntries = getResources().getStringArray(R.array.parity_entries);
        mParityValues = getResources().getStringArray(R.array.parity_entry_values);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);

        mLayoutUsbDevice = (ViewGroup) view.findViewById(R.id.layout_usb_device);
        mTextViewLabelUsbDevice = (TextView) view.findViewById(R.id.label_usb_device);
        mTextViewUsbDevice = (TextView) view.findViewById(R.id.usb_device);

        mLayoutUseDevice = (ViewGroup) view.findViewById(R.id.layout_use_device);
        mTextViewLabelUseDevice = (TextView) view.findViewById(R.id.label_use_device);
        mTextViewSummaryUseDevice = (TextView) view.findViewById(R.id.summary_use_device);
        mCheckBoxUseDevice = (CheckBox) view.findViewById(R.id.use_device);

        mLayoutBaudRate = (ViewGroup) view.findViewById(R.id.layout_baud_rate);
        mTextViewLabelBaudRate = (TextView) view.findViewById(R.id.label_baud_rate);
        mTextViewBaudRate = (TextView) view.findViewById(R.id.baud_rate);
        mTextViewBaudRate.setText(mBaudRateEntries[2]);

        mLayoutDataBits = (ViewGroup) view.findViewById(R.id.layout_data_bits);
        mTextViewLabelDataBits = (TextView) view.findViewById(R.id.label_data_bits);
        mTextViewDataBits = (TextView) view.findViewById(R.id.data_bits);
        mTextViewDataBits.setText(mDataBitsEntries[3]);

        mLayoutStopBits = (ViewGroup) view.findViewById(R.id.layout_stop_bits);
        mTextViewLabelStopBits = (TextView) view.findViewById(R.id.label_stop_bits);
        mTextViewStopBits = (TextView) view.findViewById(R.id.stop_bits);
        mTextViewStopBits.setText(mStopBitsEntries[0]);

        mLayoutParity = (ViewGroup) view.findViewById(R.id.layout_parity);
        mTextViewLabelParity = (TextView) view.findViewById(R.id.label_parity);
        mTextViewParity = (TextView) view.findViewById(R.id.parity);
        mTextViewParity.setText(mParityEntries[0]);

        mLayoutUsbDevice.setOnClickListener(this);
        mLayoutUseDevice.setOnClickListener(this);
        mLayoutBaudRate.setOnClickListener(this);
        mLayoutDataBits.setOnClickListener(this);
        mLayoutStopBits.setOnClickListener(this);
        mLayoutParity.setOnClickListener(this);

        mCheckBoxUseDevice.setOnCheckedChangeListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(mReceiver, filter);

        updateUsbDevices();
    }

    @Override
    public void onPause() {
        super.onPause();
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
        LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mReceiver);
    }

    /**
     * Updates USB device list.
     */
    private void updateUsbDevices() {
        UsbManager usbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        Map<String, UsbDevice> deviceList = usbManager.getDeviceList();

        ProbeTable table = UsbSerialProber.getDefaultProbeTable();
        table.addProduct(0x04B4, 0x0005, CdcAcmSerialDriver.class);
        UsbSerialProber prober = new UsbSerialProber(table);

        List<String> toRemove = new ArrayList<>();
        for (String deviceName : mUsbDevices.keySet()) {
            if (!deviceList.containsKey(deviceName)) {
                toRemove.add(deviceName);
            } else if (prober.probeDevice(deviceList.get(deviceName)) == null) {
                toRemove.add(deviceName);
            }
        }
        for (String deviceName : toRemove) {
            mUsbDevices.remove(deviceName);
            // TODO Check selection
        }

        for (String key : deviceList.keySet()) {
            UsbDevice device = deviceList.get(key);
            if (prober.probeDevice(device) != null
                    && !mUsbDevices.containsKey(key)) {
                mUsbDevices.put(key, device);
            }
        }

        if (mUsbDevices.isEmpty()) {
            mSelectedDevice = null;
        } else if (mSelectedDevice == null
                || !mUsbDevices.containsKey(mSelectedDevice)) {
            UsbDevice device = mUsbDevices.values().iterator().next();
            mSelectedDevice = device.getDeviceName();
        }
        onSelectedUsbDeviceChanged();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String deviceName = null;
        if (key.startsWith(PREF_BAUD_RATE)) {
            deviceName = key.substring(PREF_BAUD_RATE.length() + 1);
        }
        if (key.startsWith(PREF_DATA_BITS)) {
            deviceName = key.substring(PREF_DATA_BITS.length() + 1);
        }
        if (key.startsWith(PREF_STOP_BITS)) {
            deviceName = key.substring(PREF_STOP_BITS.length() + 1);
        }
        if (key.startsWith(PREF_PARITY)) {
            deviceName = key.substring(PREF_PARITY.length() + 1);
        }
        if (deviceName != null
                && mSelectedDevice != null
                && mSelectedDevice.equals(deviceName)) {
            updateSettings();
        }
    }

    @Override
    public void onClick(View v) {
        if (mSelectedDevice == null) {
            return;
        }
        switch (v.getId()) {
            case R.id.layout_usb_device:
                showSelectUsbDeviceDialog();
                break;
            case R.id.layout_use_device:
                mCheckBoxUseDevice.setChecked(!mCheckBoxUseDevice.isChecked());
                break;
            case R.id.layout_baud_rate:
                showSelectBaudRateDialog();
                break;
            case R.id.layout_data_bits:
                showSelectDataBitsDialog();
                break;
            case R.id.layout_stop_bits:
                showSelectStopBitsDialog();
                break;
            case R.id.layout_parity:
                showSelectParityDialog();
                break;
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (mSelectedDevice == null) {
            return;
        }
        mTextViewSummaryUseDevice.setText(isChecked ? R.string.summary_use_device : R.string
                .summary_not_use_device);

        boolean useDevice = Boolean.parseBoolean(mSharedPreferences.getString(PREF_USE_DEVICE +
                "_" + mSelectedDevice, getString(R.string.default_use_device)));
        if (useDevice != isChecked) {
            mSharedPreferences.edit().putString(PREF_USE_DEVICE + "_" + mSelectedDevice, Boolean
                    .toString(isChecked)).apply();
        }
    }

    /**
     * Shows dialog to choose USB device.
     */
    private void showSelectUsbDeviceDialog() {
        int selectedIndex = -1;

        List<String> choices = new ArrayList<>();
        final List<String> values = new ArrayList<>();
        for (UsbDevice device : mUsbDevices.values()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                choices.add(device.getProductName());
            } else {
                choices.add(device.getDeviceName());
            }
            values.add(device.getDeviceName());
        }
        for (int i = 0; i < choices.size(); i++) {
            if (values.get(i).equals(mSelectedDevice)) {
                selectedIndex = i;
                break;
            }
        }

        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_usb_device)
                .items(choices)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        mSelectedDevice = values.get(which);
                        onSelectedUsbDeviceChanged();
                        return true;
                    }
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    /**
     * Shows dialog to choose baud rate.
     */
    private void showSelectBaudRateDialog() {
        String baudRate = mSharedPreferences.getString(PREF_BAUD_RATE + "_" + mSelectedDevice,
                getString(R.string.default_baud_rate));
        int selectedIndex = getBaudRateEntryPosition(baudRate);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_baud_rate)
                .items(mBaudRateEntries)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        String newBaudRate = mBaudRateValues[which];
                        mSharedPreferences.edit().putString(PREF_BAUD_RATE + "_" +
                                mSelectedDevice, newBaudRate).apply();
                        return true;
                    }
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    /**
     * Shows dialog to choose data bits.
     */
    private void showSelectDataBitsDialog() {
        String dataBits = mSharedPreferences.getString(PREF_DATA_BITS + "_" + mSelectedDevice,
                getString(R.string.default_data_bits));
        int selectedIndex = getDataBitsEntryPosition(dataBits);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_data_bits)
                .items(mDataBitsEntries)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        String newDataBits = mDataBitsValues[which];
                        mSharedPreferences.edit().putString(PREF_DATA_BITS + "_" +
                                mSelectedDevice, newDataBits).apply();
                        return true;
                    }
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    /**
     * Shows dialog to choose stop bits.
     */
    private void showSelectStopBitsDialog() {
        String stopBits = mSharedPreferences.getString(PREF_STOP_BITS + "_" + mSelectedDevice,
                getString(R.string.default_stop_bits));
        int selectedIndex = getStopBitsEntryPosition(stopBits);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_stop_bits)
                .items(mStopBitsEntries)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        String newStopBits = mStopBitsValues[which];
                        mSharedPreferences.edit().putString(PREF_STOP_BITS + "_" +
                                mSelectedDevice, newStopBits).apply();
                        return true;
                    }
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    /**
     * Shows dialog to choose parity.
     */
    private void showSelectParityDialog() {
        String parity = mSharedPreferences.getString(PREF_PARITY + "_" + mSelectedDevice,
                getString(R.string.default_parity));
        int selectedIndex = getParityEntryPosition(parity);

        new MaterialDialog.Builder(getActivity())
                .title(R.string.title_parity)
                .items(mParityEntries)
                .itemsCallbackSingleChoice(selectedIndex, new MaterialDialog
                        .ListCallbackSingleChoice() {
                    @Override
                    public boolean onSelection(MaterialDialog dialog, View itemView, int which,
                            CharSequence text) {
                        String newParity = mParityValues[which];
                        mSharedPreferences.edit().putString(PREF_PARITY + "_" +
                                mSelectedDevice, newParity).apply();
                        return true;
                    }
                })
                .negativeText(R.string.button_cancel)
                .show();
    }

    /**
     * Called when selected USB device has changed.
     */
    private void onSelectedUsbDeviceChanged() {
        if (mSelectedDevice == null) {
            mLayoutUsbDevice.setClickable(false);
            mLayoutUseDevice.setClickable(false);
            mLayoutBaudRate.setClickable(false);
            mLayoutDataBits.setClickable(false);
            mLayoutStopBits.setClickable(false);
            mLayoutParity.setClickable(false);
            mCheckBoxUseDevice.setEnabled(false);

            mTextViewLabelUsbDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewUsbDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewUsbDevice.setText(getString(R.string.summary_no_usb_device));
            mTextViewLabelUseDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewSummaryUseDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));

            mTextViewLabelBaudRate.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewBaudRate.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewLabelDataBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewDataBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewLabelStopBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewStopBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewLabelParity.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
            mTextViewParity.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_26));
        } else {
            mLayoutUsbDevice.setClickable(true);
            mLayoutUseDevice.setClickable(true);
            mLayoutBaudRate.setClickable(true);
            mLayoutDataBits.setClickable(true);
            mLayoutStopBits.setClickable(true);
            mLayoutParity.setClickable(true);
            mCheckBoxUseDevice.setEnabled(true);

            mTextViewLabelUsbDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewUsbDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));
            mTextViewLabelUseDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewSummaryUseDevice.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));

            mTextViewLabelBaudRate.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewBaudRate.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));
            mTextViewLabelDataBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewDataBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));
            mTextViewLabelStopBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewStopBits.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));
            mTextViewLabelParity.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_87));
            mTextViewParity.setTextColor(ContextCompat.getColor(getActivity(), R.color
                    .black_54));

            UsbDevice device = mUsbDevices.get(mSelectedDevice);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mTextViewUsbDevice.setText(device.getProductName());
            } else {
                mTextViewUsbDevice.setText(device.getDeviceName());
            }
        }

        updateSettings();
    }

    /**
     * Updates selected USB device serial settings.
     */
    private void updateSettings() {
        if (mSelectedDevice == null) {
            return;
        }
        boolean useDevice = Boolean.parseBoolean(mSharedPreferences.getString(PREF_USE_DEVICE +
                "_" + mSelectedDevice, getString(R.string.default_use_device)));
        String baudRate = mSharedPreferences.getString(PREF_BAUD_RATE + "_" + mSelectedDevice,
                getString(R.string.default_baud_rate));
        String dataBits = mSharedPreferences.getString(PREF_DATA_BITS + "_" + mSelectedDevice,
                getString(R.string.default_data_bits));
        String stopBits = mSharedPreferences.getString(PREF_STOP_BITS + "_" + mSelectedDevice,
                getString(R.string.default_stop_bits));
        String parity = mSharedPreferences.getString(PREF_PARITY + "_" + mSelectedDevice,
                getString(R.string.default_parity));
        mCheckBoxUseDevice.setChecked(useDevice);
        mTextViewBaudRate.setText(mBaudRateEntries[getBaudRateEntryPosition(baudRate)]);
        mTextViewDataBits.setText(mDataBitsEntries[getDataBitsEntryPosition(dataBits)]);
        mTextViewStopBits.setText(mStopBitsEntries[getStopBitsEntryPosition(stopBits)]);
        mTextViewParity.setText(mParityEntries[getParityEntryPosition(parity)]);
    }

    /**
     * Gets the index of baud rate value in the entry list.
     *
     * @param baudRate the baud rate
     * @return the baud rate entry position
     */
    private int getBaudRateEntryPosition(String baudRate) {
        for (int i = 0; i < mBaudRateValues.length; i++) {
            if (mBaudRateValues[i].equals(baudRate)) {
                return i;
            }
        }
        return 2;
    }

    /**
     * Gets the index of data bits value in the entry list.
     *
     * @param dataBits the data bits
     * @return the data bits entry position
     */
    private int getDataBitsEntryPosition(String dataBits) {
        for (int i = 0; i < mDataBitsValues.length; i++) {
            if (mDataBitsValues[i].equals(dataBits)) {
                return i;
            }
        }
        return 3;
    }

    /**
     * Gets the index of stop bits value in the entry list.
     *
     * @param stopBits the stop bits
     * @return the stop bits entry position
     */
    private int getStopBitsEntryPosition(String stopBits) {
        for (int i = 0; i < mStopBitsValues.length; i++) {
            if (mStopBitsValues[i].equals(stopBits)) {
                return i;
            }
        }
        return 0;
    }

    /**
     * Gets the index of parity value in the entry list.
     *
     * @param parity the parity
     * @return the data bits entry position
     */
    private int getParityEntryPosition(String parity) {
        for (int i = 0; i < mParityValues.length; i++) {
            if (mParityValues[i].equals(parity)) {
                return i;
            }
        }
        return 0;
    }

}
