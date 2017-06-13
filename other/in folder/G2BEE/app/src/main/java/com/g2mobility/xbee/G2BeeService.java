package com.g2mobility.xbee;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.digi.xbee.api.RemoteXBeeDevice;
import com.digi.xbee.api.XBeeDevice;
import com.digi.xbee.api.XBeeNetwork;
import com.digi.xbee.api.connection.IConnectionInterface;
import com.digi.xbee.api.connection.serial.AbstractSerialPort;
import com.digi.xbee.api.connection.serial.SerialPortParameters;
import com.digi.xbee.api.exceptions.InterfaceNotOpenException;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.listeners.IDataReceiveListener;
import com.digi.xbee.api.listeners.IDiscoveryListener;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.models.XBeeMessage;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.api.DiscoverResponse;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.G2BeeResponse;
import com.g2mobility.xbee.api.GetParameterResponse;
import com.g2mobility.xbee.api.GetReceiveTimeoutResponse;
import com.g2mobility.xbee.api.RemoteG2BeeDevice;
import com.g2mobility.xbee.api.listeners.IG2BeeDataReceiveListener;
import com.g2mobility.xbee.api.models.G2BeeMessage;
import com.g2mobility.xbee.fragment.SettingsFragment;
import com.g2mobility.xbee.serial.SerialPortAndroidUsb;
import com.hoho.android.usbserial.driver.CdcAcmSerialDriver;
import com.hoho.android.usbserial.driver.ProbeTable;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service that allows reading and writing on XBee module connected to the USB host via a serial-USB
 * converter.
 * <p/>
 * An interface is provided for who bound to the service to do requests and get response via the
 * XBee radio module.
 *
 * @author Hanyu Li
 */
public class G2BeeService extends Service implements OnSharedPreferenceChangeListener {
    private static final String TAG = "G2Bee";

    public static boolean sDebug;

    private UsbManager mUsbManager;
    private UsbSerialProber mProber;

    /**
     * Binder interface. Returned when bound.
     */
    private G2BeeManager mG2BeeManager;
    /**
     * Local XBee devices.
     */
    private final Map<XBee64BitAddress, XBeeDevice> mXBeeDevices = new HashMap<>();

    private final RemoteCallbackList<IG2BeeDataReceiveListener> mDataReceiveListeners =
            new RemoteCallbackList<>();

    private final IDataReceiveListener mDataReceiveListener = new IDataReceiveListener() {
        @Override
        public void dataReceived(XBeeMessage xbeeMessage) {
            if (sDebug) {
                Log.d(TAG, "[RECV][" + xbeeMessage.getDevice().get64BitAddress() + "] " +
                        HexUtils.prettyHexString(xbeeMessage.getData()));
            }
            int i = mDataReceiveListeners.beginBroadcast();
            while (i > 0) {
                i--;
                try {
                    mDataReceiveListeners.getBroadcastItem(i).dataReceived(new G2BeeMessage
                            (xbeeMessage));
                } catch (RemoteException ignored) {
                }
            }
            mDataReceiveListeners.finishBroadcast();
        }
    };

    private SharedPreferences mSharedPreferences;
    private ExecutorService mExecutor = Executors.newSingleThreadExecutor();

    @Override
    public IBinder onBind(Intent intent) {
        return mG2BeeManager;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "XBeeService starting up");

        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

        ProbeTable table = UsbSerialProber.getDefaultProbeTable();
        table.addProduct(0x04B4, 0x0005, CdcAcmSerialDriver.class);
        mProber = new UsbSerialProber(table);

        mG2BeeManager = new G2BeeManager(this);

        mSharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        mSharedPreferences.registerOnSharedPreferenceChangeListener(this);

        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                openXBee();
            }
        });
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            openXBee();
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_DEBUG_ON:
                    enableListener();
                    break;
                case G2BeeConstants.ACTION_DEBUG_OFF:
                    disableListener();
                    break;
                case G2BeeConstants.ACTION_DISCOVER:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onDiscover();
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_EXECUTE_PARAMETER:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onExecuteParameter(intent);
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_GET_PARAMETER:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onGetParameter(intent);
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_LIST_DEVICE:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onListDevice();
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_SET_PARAMETER:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onSetParameter(intent);
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_SEND_DATA:
                    mExecutor.submit(new Runnable() {
                        @Override
                        public void run() {
                            onSendData(intent);
                        }
                    });
                    break;
                case G2BeeConstants.ACTION_GRANT_PERMISSION:
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        mExecutor.submit(new Runnable() {
                            @Override
                            public void run() {
                                openXBee();
                            }
                        });
                    }
                    break;
            }
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "XBeeService destroyed");
        for (XBeeDevice device : mXBeeDevices.values()) {
            if (device.isOpen()) {
                device.close();
            }
        }
        mSharedPreferences.unregisterOnSharedPreferenceChangeListener(this);
    }

    /**
     * Searches FTDI USB-Serial devices or CDC-ACM devices in system devices.
     *
     * @return the list of XBee devices' physical port name
     */
    private List<String> findXBeeDevices() {
        List<String> listDevices = new ArrayList<>();

        for (String deviceName : mUsbManager.getDeviceList().keySet()) {
            UsbDevice device = mUsbManager.getDeviceList().get(deviceName);
            if (mProber.probeDevice(device) != null) {
                listDevices.add(deviceName);
            }
        }

        return listDevices;
    }

    /**
     * Open all XBee devices connected via host USB API.
     */
    private synchronized void openXBee() {
        List<String> deviceNames = findXBeeDevices();

        if (deviceNames.isEmpty()) {
            Log.w(TAG, "No XBee device found");
            return;
        }
        List<String> openedPorts = new ArrayList<>();
        // Clear closed devices
        List<XBee64BitAddress> toRemove = new ArrayList<>();
        for (XBee64BitAddress address : mXBeeDevices.keySet()) {
            if (!mXBeeDevices.get(address).isOpen()) {
                toRemove.add(address);
            } else {
                IConnectionInterface connectionInterface = mXBeeDevices.get(address)
                        .getConnectionInterface();
                if (connectionInterface instanceof SerialPortAndroidUsb) {
                    openedPorts.add(((SerialPortAndroidUsb) connectionInterface).getPort());
                }
            }
        }
        for (XBee64BitAddress address : toRemove) {
            mXBeeDevices.remove(address);
        }
        for (String deviceName : deviceNames) {
            boolean useDevice = Boolean.parseBoolean(mSharedPreferences.getString
                    (SettingsFragment.PREF_USE_DEVICE + "_" + deviceName, getString(R.string
                            .default_use_device)));
            if (!useDevice) {
                continue;
            }
            if (!openedPorts.contains(deviceName)) {
                UsbDevice device = mUsbManager.getDeviceList().get(deviceName);
                if (device != null) {
                    if (!mUsbManager.hasPermission(device)) {
                        requirePermission(deviceName);
                        continue;
                    }
                    int baudRate = Integer.parseInt(mSharedPreferences.getString(SettingsFragment
                            .PREF_BAUD_RATE + "_" + deviceName, getString(R.string
                            .default_baud_rate)));
                    int dataBits = Integer.parseInt(mSharedPreferences.getString(SettingsFragment
                            .PREF_DATA_BITS + "_" + deviceName, getString(R.string
                            .default_data_bits)));
                    int stopBits = Integer.parseInt(mSharedPreferences.getString(SettingsFragment
                            .PREF_STOP_BITS + "_" + deviceName, getString(R.string
                            .default_stop_bits)));
                    int parity = Integer.parseInt(mSharedPreferences.getString(SettingsFragment
                            .PREF_PARITY + "_" + deviceName, getString(R.string.default_parity)));

                    SerialPortParameters parameters = new SerialPortParameters(baudRate,
                            dataBits, stopBits, parity, AbstractSerialPort.DEFAULT_FLOW_CONTROL);
                    SerialPortAndroidUsb serialPort = new SerialPortAndroidUsb(this, mProber,
                            deviceName, parameters, 200);
                    final XBeeDevice xbeeDevice = new XBeeDevice(serialPort);

                    try {
                        xbeeDevice.open();
                        xbeeDevice.addDataListener(mDataReceiveListener);
                        mXBeeDevices.put(xbeeDevice.get64BitAddress(), xbeeDevice);
                    } catch (XBeeException | InterfaceNotOpenException e) {
                        Log.e(TAG, "XBeeException occurred while opening xbee", e);
                    }
                }
            }
        }
    }

    /**
     * Gains the read/write permissions for all users for the XBee device. This will require a super
     * user root access of the Android device.
     *
     * @param deviceName the XBee device name
     */
    private void requirePermission(String deviceName) {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
        UsbDevice device = manager.getDeviceList().get(deviceName);

        if (checkCallingOrSelfPermission("android.permission.MANAGE_USB")
                == PackageManager.PERMISSION_GRANTED) {
            // Try to require permission with system permission
            try {
                Field binderField = UsbManager.class.getDeclaredField("mService");
                binderField.setAccessible(true);
                Object iUsbManager = binderField.get(manager);

                Method grantDevicePermissionMethod = iUsbManager.getClass().getDeclaredMethod(
                        "grantDevicePermission", UsbDevice.class, Integer.TYPE);

                PackageManager pm = getPackageManager();
                ApplicationInfo ai = pm.getApplicationInfo("com.g2mobility.xbee", 0);
                grantDevicePermissionMethod.invoke(iUsbManager, device, ai.uid);
            } catch (NoSuchFieldException | NoSuchMethodException | IllegalArgumentException |
                    NameNotFoundException | InvocationTargetException | IllegalAccessException e) {
                Log.w(TAG, "Cannot grant permission for USB device", e);
            }
        }

        if (manager.hasPermission(device)) {
            mExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    openXBee();
                }
            });
        } else {
            // Not granted, require normally
            manager.requestPermission(device, PendingIntent.getBroadcast(this, 0, new Intent(
                    G2BeeConstants.ACTION_GRANT_PERMISSION), PendingIntent.FLAG_ONE_SHOT));
        }
    }

    @Override
    public synchronized void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
            String key) {
        String deviceName = null;
        if (key.startsWith(SettingsFragment.PREF_USE_DEVICE)) {
            deviceName = key.substring(SettingsFragment.PREF_USE_DEVICE.length() + 1);
        }
        if (key.startsWith(SettingsFragment.PREF_BAUD_RATE)) {
            deviceName = key.substring(SettingsFragment.PREF_BAUD_RATE.length() + 1);
        }
        if (key.startsWith(SettingsFragment.PREF_DATA_BITS)) {
            deviceName = key.substring(SettingsFragment.PREF_DATA_BITS.length() + 1);
        }
        if (key.startsWith(SettingsFragment.PREF_STOP_BITS)) {
            deviceName = key.substring(SettingsFragment.PREF_DATA_BITS.length() + 1);
        }
        if (key.startsWith(SettingsFragment.PREF_PARITY)) {
            deviceName = key.substring(SettingsFragment.PREF_DATA_BITS.length() + 1);
        }
        if (deviceName != null) {
            for (XBee64BitAddress address : mXBeeDevices.keySet()) {
                XBeeDevice device = mXBeeDevices.get(address);
                IConnectionInterface connectionInterface = device.getConnectionInterface();
                if (connectionInterface instanceof SerialPortAndroidUsb) {
                    String portName = ((SerialPortAndroidUsb) connectionInterface).getPort();
                    if (portName.equals(deviceName) && device.isOpen()) {
                        device.close();
                        mXBeeDevices.remove(address);
                        break;
                    }
                }
            }
        }
        mExecutor.submit(new Runnable() {
            @Override
            public void run() {
                openXBee();
            }
        });
    }

    /**
     * Set a listener to display the messages received.
     */
    private void enableListener() {
        sDebug = true;
    }

    /**
     * Remove the listener.
     */
    private void disableListener() {
        sDebug = false;
    }

    /**
     * Retrieves the list of local XBee devices opened.
     *
     * @return list of local XBee devices
     */
    public synchronized List<G2BeeDevice> getLocalXBeeDevices() {
        List<G2BeeDevice> g2BeeDevices = new ArrayList<>();

        for (XBeeDevice device : mXBeeDevices.values()) {
            g2BeeDevices.add(new G2BeeDevice(device));
        }

        return g2BeeDevices;
    }

    /**
     * Retrieves the local XBee device whose network contains the specified remote device.
     *
     * @param xbee64BitAddress the remote device address
     * @return the local XBee device
     */
    public synchronized G2BeeDevice getLocalXBeeDeviceForRemoteDevice(byte[] xbee64BitAddress) {
        try {
            XBeeDevice device = getLocalDeviceForRemoteDevice(new XBee64BitAddress
                    (xbee64BitAddress));
            if (device != null) {
                return new G2BeeDevice(device);
            } else {
                return null;
            }
        } catch (RuntimeException e) {
            return null;
        }
    }

    /**
     * Retrieves the list of remote XBee devices.
     *
     * @return list of remote XBee devices
     */
    public synchronized List<RemoteG2BeeDevice> getRemoteXBeeDevices() {
        List<RemoteG2BeeDevice> remoteG2BeeDevices = new ArrayList<>();

        List<XBee64BitAddress> toRemove = new ArrayList<>();
        for (XBeeDevice device : mXBeeDevices.values()) {
            try {
                for (RemoteXBeeDevice remoteDevice : device.getNetwork().getDevices()) {
                    remoteG2BeeDevices.add(new RemoteG2BeeDevice(remoteDevice));
                }
            } catch (InterfaceNotOpenException e) {
                toRemove.add(device.get64BitAddress());
            }
        }
        if (!toRemove.isEmpty()) {
            for (XBee64BitAddress address : toRemove) {
                mXBeeDevices.remove(address);
            }
        }
        return remoteG2BeeDevices;
    }

    /**
     * Adds the provided listener to the list of listeners to be notified when new data is
     * received.
     * <p/>
     * If the listener has been already included this method does nothing.
     *
     * @param listener listener to be notified when new data is received.
     * @throws NullPointerException if {@code listener == null}
     * @see #removeDataListener(IG2BeeDataReceiveListener)
     * @see IG2BeeDataReceiveListener
     */
    public void addDataListener(IG2BeeDataReceiveListener listener) {
        if (listener != null) {
            mDataReceiveListeners.register(listener);
        }
    }

    /**
     * Removes the provided listener from the list of data listeners.
     * <p/>
     * If the listener was not in the list this method does nothing.
     *
     * @param listener listener to be removed from the list of listeners.
     * @see #addDataListener(IG2BeeDataReceiveListener)
     * @see IG2BeeDataReceiveListener
     */
    public void removeDataListener(IG2BeeDataReceiveListener listener) {
        if (listener != null) {
            mDataReceiveListeners.unregister(listener);
        }
    }

    /**
     * Returns this XBee device configured timeout for receiving packets in synchronous operations.
     *
     * @param xbee64BitAddress XBee device 64 bit address
     * @return The current receive timeout in milliseconds.
     * @see #setReceiveTimeout(byte[], int)
     */
    public GetReceiveTimeoutResponse getReceiveTimeout(byte[] xbee64BitAddress) {
        try {
            XBee64BitAddress address64 = new XBee64BitAddress(xbee64BitAddress);
            if (mXBeeDevices.containsKey(address64)) {
                int receiveTimeout = mXBeeDevices.get(address64).getReceiveTimeout();
                return new GetReceiveTimeoutResponse(receiveTimeout);
            }
            return new GetReceiveTimeoutResponse(new XBeeException("Local device not found"));
        } catch (RuntimeException e) {
            return new GetReceiveTimeoutResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Configures this XBee device timeout in milliseconds for receiving packets in synchronous
     * operations.
     *
     * @param xbee64BitAddress XBee device 64 bit address
     * @param receiveTimeout   The new receive timeout in milliseconds.
     * @throws IllegalArgumentException if {@code receiveTimeout < 0}.
     * @see #getReceiveTimeout(byte[])
     */
    public G2BeeResponse setReceiveTimeout(byte[] xbee64BitAddress, int receiveTimeout) {
        try {
            XBee64BitAddress address64 = new XBee64BitAddress(xbee64BitAddress);
            if (mXBeeDevices.containsKey(address64)) {
                mXBeeDevices.get(address64).setReceiveTimeout(receiveTimeout);
                return new G2BeeResponse();
            }
            return new G2BeeResponse(new XBeeException("Local device not found"));
        } catch (RuntimeException e) {
            return new GetReceiveTimeoutResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Gets the value of the given parameter from specified XBee device.
     *
     * @param xbee64BitAddress XBee device 64 bit address
     * @param parameter        the name of the parameter to retrieve its value.
     * @return a byte array containing the value of the parameter
     */
    public GetParameterResponse getParameter(byte[] xbee64BitAddress, String parameter) {
        try {
            XBee64BitAddress address64 = new XBee64BitAddress(xbee64BitAddress);
            if (mXBeeDevices.containsKey(address64)) {
                try {
                    byte[] value = mXBeeDevices.get(address64).getParameter(parameter);
                    return new GetParameterResponse(value);
                } catch (XBeeException e) {
                    return new GetParameterResponse(e);
                }
            }
            // Remote
            XBeeDevice localDevice = getLocalDeviceForRemoteDevice(address64);
            if (localDevice != null) {
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(localDevice, address64);
                try {
                    byte[] value = remoteDevice.getParameter(parameter);
                    return new GetParameterResponse(value);
                } catch (XBeeException e) {
                    localDevice.getNetwork().removeRemoteDevice(remoteDevice);
                }
            }
            // Not found
            for (XBeeDevice device : mXBeeDevices.values()) {
                if (localDevice != null && device.equals(localDevice)) {
                    continue;
                }
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(device, address64);
                try {
                    byte[] value = remoteDevice.getParameter(parameter);
                    return new GetParameterResponse(value);
                } catch (XBeeException ignored) {
                }
            }
            return new GetParameterResponse(new XBeeException("There was a timeout while " +
                    "executing the requested operation."));
        } catch (RuntimeException e) {
            return new GetParameterResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Sets the given parameter with the provided value in this XBee device. <p/> If the 'apply
     * configuration changes' option is enabled in this device, the configured value for the given
     * parameter will be immediately applied, if not the method {@code applyChanges()} must be
     * invoked to apply it.
     * <p/>
     * Use:
     * <p/>
     * <ul> <li>Method {@code isApplyConfigurationChangesEnabled()} to know if the 'apply
     * configuration changes' option is enabled.</li> <li>Method {@code
     * enableApplyConfigurationChanges(boolean)} to enable or disable this option.</li> </ul>
     * <p/>
     * To make parameter modifications persist through subsequent resets use the {@code
     * writeChanges()} method.
     *
     * @param xbee64BitAddress XBee device 64 bit address
     * @param parameter        The name of the parameter to be set.
     * @param value            The value of the parameter to set.
     */
    public G2BeeResponse setParameter(byte[] xbee64BitAddress, String parameter, byte[] value) {
        try {
            XBee64BitAddress address64 = new XBee64BitAddress(xbee64BitAddress);
            if (mXBeeDevices.containsKey(address64)) {
                try {
                    mXBeeDevices.get(address64).setParameter(parameter, value);
                    mXBeeDevices.get(address64).writeChanges();
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    return new GetParameterResponse(e);
                }
            }
            // Remote
            XBeeDevice localDevice = getLocalDeviceForRemoteDevice(address64);
            if (localDevice != null) {
                RemoteXBeeDevice remoteDevice = localDevice.getNetwork().getDevice(address64);
                try {
                    remoteDevice.setParameter(parameter, value);
                    remoteDevice.writeChanges();
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    localDevice.getNetwork().removeRemoteDevice(remoteDevice);
                }
            }
            // Not found
            for (XBeeDevice device : mXBeeDevices.values()) {
                if (localDevice != null && device.equals(localDevice)) {
                    continue;
                }
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(device, address64);
                try {
                    remoteDevice.setParameter(parameter, value);
                    remoteDevice.writeChanges();
                    return new G2BeeResponse();
                } catch (XBeeException ignored) {
                }
            }
            return new G2BeeResponse(new XBeeException("There was a timeout while executing " +
                    "the requested operation."));
        } catch (RuntimeException e) {
            return new G2BeeResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Executes the given command in this XBee device.
     * <p/>
     * This method is intended to be used for those AT parameters that cannot be read or written,
     * they just execute some action in the XBee module.
     *
     * @param xbee64BitAddress XBee device 64 bit address
     * @param parameter        The AT command to be executed.
     * @see #getParameter(byte[], String)
     * @see #setParameter(byte[], String, byte[])
     */
    public G2BeeResponse executeParameter(byte[] xbee64BitAddress, String parameter) {
        try {
            XBee64BitAddress address64 = new XBee64BitAddress(xbee64BitAddress);
            if (mXBeeDevices.containsKey(address64)) {
                try {
                    mXBeeDevices.get(address64).executeParameter(parameter);
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    return new GetParameterResponse(e);
                }
            }
            // Remote
            XBeeDevice localDevice = getLocalDeviceForRemoteDevice(address64);
            if (localDevice != null) {
                RemoteXBeeDevice remoteDevice = localDevice.getNetwork().getDevice(address64);
                try {
                    remoteDevice.executeParameter(parameter);
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    localDevice.getNetwork().removeRemoteDevice(remoteDevice);
                }
            }
            // Not found
            for (XBeeDevice device : mXBeeDevices.values()) {
                if (localDevice != null && device.equals(localDevice)) {
                    continue;
                }
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(device, address64);
                try {
                    remoteDevice.executeParameter(parameter);
                    return new G2BeeResponse();
                } catch (XBeeException ignored) {
                }
            }
            return new G2BeeResponse(new XBeeException("There was a timeout while executing " +
                    "the requested operation."));
        } catch (RuntimeException e) {
            return new G2BeeResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Discovers XBee devices on the XBee network.
     *
     * @param local64BitAddress the local XBee device to perform discovery
     * @return remote devices discovered
     */
    @SuppressWarnings("SynchronizationOnLocalVariableOrMethodParameter")
    public DiscoverResponse discover(byte[] local64BitAddress) {
        try {
            XBee64BitAddress xbee64BitAddress = new XBee64BitAddress(local64BitAddress);
            if (mXBeeDevices.containsKey(xbee64BitAddress)) {
                Log.i(TAG, "Discovering xbee nodes...");
                final List<RemoteG2BeeDevice> remoteDevices = new ArrayList<>();
                XBeeDevice device = mXBeeDevices.get(xbee64BitAddress);
                try {
                    final List<RemoteXBeeDevice> remoteXBeeDevices = new ArrayList<>();

                    byte[] timeoutValue = device.getParameter("NT");
                    long nodeDiscoveryTimeout = ByteUtils.byteArrayToInt(timeoutValue) * 100;

                    final XBeeNetwork network = device.getNetwork();
                    network.setDiscoveryTimeout(nodeDiscoveryTimeout);
                    network.addDiscoveryListener(new IDiscoveryListener() {
                        @Override
                        public void deviceDiscovered(RemoteXBeeDevice discoveredDevice) {
                            remoteXBeeDevices.add(discoveredDevice);
                        }

                        @Override
                        public void discoveryError(String error) {
                        }

                        @Override
                        public void discoveryFinished(String error) {
                            network.removeDiscoveryListener(this);
                            synchronized (remoteDevices) {
                                remoteDevices.notify();
                            }
                        }
                    });

                    synchronized (remoteDevices) {
                        network.startDiscoveryProcess();
                        try {
                            remoteDevices.wait();
                        } catch (InterruptedException ignored) {
                        }
                    }
                    for (RemoteXBeeDevice remoteXBeeDevice : remoteXBeeDevices) {
                        try {
                            remoteXBeeDevice.readDeviceInfo();
                        } catch (XBeeException ignored) {
                        }
                        remoteDevices.add(new RemoteG2BeeDevice(remoteXBeeDevice));
                    }
                    return new DiscoverResponse(remoteDevices);
                } catch (XBeeException e) {
                    Log.e(TAG, "Error occurred while discovering XBee devices", e);
                    return new DiscoverResponse(e);
                }
            } else {
                Log.e(TAG, "Local device not found");
                return new DiscoverResponse(new XBeeException("Local device not found"));
            }
        } catch (RuntimeException e) {
            return new DiscoverResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Sends the provided data to the given XBee device choosing the optimal send method depending
     * on the protocol of the local XBee device.
     * <p/>
     * This method blocks till a success or error response arrives or the configured receive timeout
     * expires.
     * <p/>
     * The receive timeout is configured using the {@code setReceiveTimeout} method and can be
     * consulted with {@code getReceiveTimeout} method.
     * <p/>
     * For non-blocking operations use the method {@link #sendDataAsync(byte[], byte[])}.
     *
     * @param remote64BitAddress The XBee device of the network that will receive the data.
     * @param data               Byte array containing the data to be sent.
     * @see #getReceiveTimeout(byte[])
     * @see #setReceiveTimeout(byte[], int)
     * @see #sendDataAsync(byte[], byte[])
     * @see com.digi.xbee.api.RemoteXBeeDevice
     */
    public G2BeeResponse sendData(byte[] remote64BitAddress, byte[] data) {
        if (sDebug)
            Log.d(TAG, "[SEND][" + HexUtils.byteArrayToHexString(remote64BitAddress) + "] " +
                    HexUtils.prettyHexString(data));
        try {
            XBee64BitAddress xbee64BitAddress = new XBee64BitAddress(remote64BitAddress);
            XBeeDevice localDevice = getLocalDeviceForRemoteDevice(xbee64BitAddress);
            if (localDevice != null) {
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(localDevice, xbee64BitAddress);
                try {
                    localDevice.sendData(remoteDevice, data);
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    localDevice.getNetwork().removeRemoteDevice(remoteDevice);
                }
            }
            // Not found
            for (XBeeDevice device : mXBeeDevices.values()) {
                if (localDevice != null && device.equals(localDevice)) {
                    continue;
                }
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(device, xbee64BitAddress);
                try {
                    device.sendData(remoteDevice, data);
                    return new G2BeeResponse();
                } catch (XBeeException ignored) {
                }
            }
            return new G2BeeResponse(new XBeeException("There was a timeout while executing the " +
                    "requested operation."));
        } catch (RuntimeException e) {
            return new DiscoverResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Sends the provided data to the provided XBee device asynchronously choosing the optimal send
     * method depending on the protocol of the local XBee device.
     * <p/>
     * Asynchronous transmissions do not wait for answer from the remote device or for transmit
     * status packet.
     *
     * @param remote64BitAddress The XBee device of the network that will receive the data.
     * @param data               Byte array containing the data to be sent.
     * @see #sendData(byte[], byte[])
     * @see RemoteXBeeDevice
     */
    public G2BeeResponse sendDataAsync(byte[] remote64BitAddress, byte[] data) {
        if (sDebug)
            Log.d(TAG, "[SEND][" + HexUtils.byteArrayToHexString(remote64BitAddress) + "] " +
                    HexUtils.prettyHexString(data));
        try {
            XBee64BitAddress xbee64BitAddress = new XBee64BitAddress(remote64BitAddress);
            XBeeDevice localDevice = getLocalDeviceForRemoteDevice(xbee64BitAddress);
            if (localDevice != null) {
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(localDevice, xbee64BitAddress);
                try {
                    localDevice.sendDataAsync(remoteDevice, data);
                    return new G2BeeResponse();
                } catch (XBeeException e) {
                    return new G2BeeResponse(e);
                }
            }
            // Not found
            for (XBeeDevice device : mXBeeDevices.values()) {
                RemoteXBeeDevice remoteDevice = new RemoteXBeeDevice(device, xbee64BitAddress);
                try {
                    device.sendDataAsync(remoteDevice, data);
                } catch (XBeeException e) {
                    return new G2BeeResponse(e);
                }
            }
            return new G2BeeResponse();
        } catch (RuntimeException e) {
            return new G2BeeResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Sends the provided data to all the XBee nodes of the network (broadcast).
     * <p/>
     * This method blocks till a success or error transmit status arrives or the configured receive
     * timeout expires.
     * <p/>
     * The receive timeout is configured using the {@code setReceiveTimeout} method and can be
     * consulted with {@code getReceiveTimeout} method.
     *
     * @param data Byte array containing the data to be sent.
     * @see #getReceiveTimeout(byte[])
     * @see #setReceiveTimeout(byte[], int)
     */
    public G2BeeResponse sendBroadcastData(byte[] data) {
        try {
            for (XBeeDevice device : mXBeeDevices.values()) {
                try {
                    device.sendBroadcastData(data);
                } catch (XBeeException ignored) {
                }
            }
            return new G2BeeResponse();
        } catch (RuntimeException e) {
            return new DiscoverResponse(new XBeeException(e.getMessage()));
        }
    }

    /**
     * Retrieves the local XBee device whose network contains the specified remote device.
     *
     * @param xbee64BitAddress the remote device address
     * @return the local XBee device
     */
    private XBeeDevice getLocalDeviceForRemoteDevice(XBee64BitAddress xbee64BitAddress) {
        for (XBeeDevice device : mXBeeDevices.values()) {
            if (device.getNetwork().getDevice(xbee64BitAddress) != null) {
                return device;
            }
        }
        return null;
    }

    /**
     * Called when broadcast intent received to list XBee devices.
     */
    private void onListDevice() {
        if (mXBeeDevices.isEmpty()) {
            Log.w(TAG, "[ACTION] No XBee device found");
        }
        for (XBeeDevice localDevice : mXBeeDevices.values()) {
            Log.i(TAG, "[ACTION] XBee device " + localDevice.get64BitAddress());
        }
    }

    /**
     * Called when broadcast intent received to do discovery.
     */
    private void onDiscover() {
        if (mXBeeDevices.isEmpty()) {
            Log.w(TAG, "[ACTION] No XBee device found");
        }
        for (XBeeDevice localDevice : mXBeeDevices.values()) {
            DiscoverResponse response = discover(localDevice.get64BitAddress().getValue());
            if (!response.isException()) {
                for (RemoteG2BeeDevice device : response.getDevices()) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("[ACTION] XBee device ");
                    if (device.getXbee64BitAddress().equals(localDevice.get64BitAddress())) {
                        sb.append("(local)  ");
                    } else {
                        sb.append("(remote) ");
                    }
                    sb.append(device.getXbee64BitAddress());
                    if (device.getNodeID() != null
                            && !device.getNodeID().isEmpty()
                            && !" ".equals(device.getNodeID())) {
                        sb.append(" (NI: ").append(device.getNodeID()).append(")");
                    }
                    Log.i(TAG, sb.toString());
                }
            } else {
                Log.e(TAG, "[ACTION] Error occurred while executing action", response
                        .getException());
            }
        }
    }

    /**
     * Called when broadcast intent received to get parameter.
     *
     * @param intent the broadcast intent
     */
    private void onGetParameter(Intent intent) {
        if (!intent.hasExtra(G2BeeConstants.KEY_ADDRESS)) {
            Log.e(TAG, "[ACTION] XBee device address is missing. Extra \"" + G2BeeConstants
                    .KEY_ADDRESS + "\" should be provided");
            return;
        }
        if (!intent.hasExtra(G2BeeConstants.KEY_PARAMETER)) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }

        String address = intent.getStringExtra(G2BeeConstants.KEY_ADDRESS);
        String parameter = intent.getStringExtra(G2BeeConstants.KEY_PARAMETER);

        if (parameter == null) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }

        parameter = parameter.toUpperCase(Locale.getDefault());

        XBee64BitAddress xbee64BitAddress;
        try {
            xbee64BitAddress = new XBee64BitAddress(address);
        } catch (RuntimeException e) {
            Log.e(TAG, "[ACTION] XBee address is invalid", e);
            return;
        }

        GetParameterResponse response = getParameter(xbee64BitAddress.getValue(), parameter);
        if (!response.isException()) {
            if (parameter.equals("NI")) {
                Log.i(TAG, "[ACTION] XBee device " + xbee64BitAddress.toString() + " parameter ["
                        + parameter + "]: " + ByteUtils.byteArrayToString(response.getValue()));
            } else {
                Log.i(TAG, "[ACTION] XBee device " + xbee64BitAddress.toString() + " parameter ["
                        + parameter + "]: " + HexUtils.byteArrayToHexString(response.getValue()));
            }
        } else {
            Log.e(TAG, "[ACTION] Error occurred while executing action", response.getException());
        }
    }

    /**
     * Called when broadcast intent received to execute parameter.
     *
     * @param intent the broadcast intent
     */
    private void onExecuteParameter(Intent intent) {
        if (!intent.hasExtra(G2BeeConstants.KEY_ADDRESS)) {
            Log.e(TAG, "[ACTION] XBee device address is missing. Extra \"" + G2BeeConstants
                    .KEY_ADDRESS + "\" should be provided");
            return;
        }
        if (!intent.hasExtra(G2BeeConstants.KEY_PARAMETER)) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }

        String address = intent.getStringExtra(G2BeeConstants.KEY_ADDRESS);
        String parameter = intent.getStringExtra(G2BeeConstants.KEY_PARAMETER);

        if (parameter == null) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }

        parameter = parameter.toUpperCase(Locale.getDefault());

        XBee64BitAddress xbee64BitAddress;
        try {
            xbee64BitAddress = new XBee64BitAddress(address);
        } catch (RuntimeException e) {
            Log.e(TAG, "[ACTION] XBee address is invalid", e);
            return;
        }

        G2BeeResponse response = executeParameter(xbee64BitAddress.getValue(), parameter);
        if (!response.isException()) {
            Log.i(TAG, "[ACTION] XBee device " + xbee64BitAddress.toString() + " execute " +
                    "parameter [" + parameter + "]: OK");
        } else {
            Log.e(TAG, "[ACTION] Error occurred while executing action", response.getException());
        }
    }

    /**
     * Called when broadcast intent received to set parameter.
     *
     * @param intent the broadcast intent
     */
    private void onSetParameter(Intent intent) {
        if (!intent.hasExtra(G2BeeConstants.KEY_ADDRESS)) {
            Log.e(TAG, "[ACTION] XBee device address is missing. Extra \"" + G2BeeConstants
                    .KEY_ADDRESS + "\" should be provided");
            return;
        }
        if (!intent.hasExtra(G2BeeConstants.KEY_PARAMETER)) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }
        if (!intent.hasExtra(G2BeeConstants.KEY_VALUE)) {
            Log.e(TAG, "[ACTION] XBee parameter value is missing. Extra \"" + G2BeeConstants
                    .KEY_VALUE + "\" should be provided");
            return;
        }

        String address = intent.getStringExtra(G2BeeConstants.KEY_ADDRESS);
        String parameter = intent.getStringExtra(G2BeeConstants.KEY_PARAMETER);
        String stringValue = intent.getStringExtra(G2BeeConstants.KEY_VALUE);

        if (parameter == null) {
            Log.e(TAG, "[ACTION] XBee parameter is missing. Extra \"" + G2BeeConstants
                    .KEY_PARAMETER + "\" should be provided");
            return;
        }
        if (stringValue == null) {
            Log.e(TAG, "[ACTION] XBee parameter value is missing. Extra \"" + G2BeeConstants
                    .KEY_VALUE + "\" should be provided");
            return;
        }

        parameter = parameter.toUpperCase(Locale.getDefault());

        XBee64BitAddress xbee64BitAddress;
        byte[] value;
        try {
            xbee64BitAddress = new XBee64BitAddress(address);
            if (parameter.equals("NI")) {
                value = ByteUtils.stringToByteArray(stringValue);
            } else {
                value = HexUtils.hexStringToByteArray(stringValue);
            }
        } catch (RuntimeException e) {
            Log.e(TAG, "[ACTION] XBee address is invalid", e);
            return;
        }

        G2BeeResponse response = setParameter(xbee64BitAddress.getValue(), parameter, value);
        if (!response.isException()) {
            Log.i(TAG, "[ACTION] XBee device " + xbee64BitAddress.toString() + " set parameter [" +
                    parameter + "]: OK");
        } else {
            Log.e(TAG, "[ACTION] Error occurred while executing action", response.getException());
        }
    }

    /**
     * Called when broadcast intent received to send data.
     *
     * @param intent the broadcast intent
     */
    private void onSendData(Intent intent) {
        if (!intent.hasExtra(G2BeeConstants.KEY_ADDRESS)) {
            Log.e(TAG, "[ACTION] XBee device address is missing. Extra \"" + G2BeeConstants
                    .KEY_ADDRESS + "\" should be provided");
            return;
        }
        if (!intent.hasExtra(G2BeeConstants.KEY_PAYLOAD)) {
            Log.e(TAG, "[ACTION] Payload data is missing. Extra \"" + G2BeeConstants
                    .KEY_PAYLOAD + "\" should be provided");
            return;
        }

        String address = intent.getStringExtra(G2BeeConstants.KEY_ADDRESS);
        String payload = intent.getStringExtra(G2BeeConstants.KEY_PAYLOAD);

        if (payload == null) {
            Log.e(TAG, "[ACTION] Payload data is missing. Extra \"" + G2BeeConstants
                    .KEY_PAYLOAD + "\" should be provided");
            return;
        }

        XBee64BitAddress xbee64BitAddress;
        byte[] data;
        try {
            xbee64BitAddress = new XBee64BitAddress(address);
            data = HexUtils.hexStringToByteArray(payload);
        } catch (RuntimeException e) {
            Log.e(TAG, "[ACTION] XBee address is invalid", e);
            return;
        }

        G2BeeResponse response = sendData(xbee64BitAddress.getValue(), data);
        if (!response.isException()) {
            Log.i(TAG, "[ACTION] Send to device " + xbee64BitAddress.toString() + " [" +
                    HexUtils.prettyHexString(data) + "]: OK");
        } else {
            Log.e(TAG, "[ACTION] Error occurred while executing action", response.getException());
        }
    }

}
