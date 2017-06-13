package com.g2mobility.xbee.serial;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import com.digi.xbee.api.connection.serial.AbstractSerialPort;
import com.digi.xbee.api.connection.serial.SerialPortParameters;
import com.digi.xbee.api.exceptions.InterfaceInUseException;
import com.digi.xbee.api.exceptions.InvalidConfigurationException;
import com.digi.xbee.api.exceptions.InvalidInterfaceException;
import com.digi.xbee.api.exceptions.PermissionDeniedException;
import com.hoho.android.usbserial.driver.UsbSerialDriver;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.driver.UsbSerialProber;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.HashMap;

/**
 * This class represents a serial port using the Android USB host API to communicate with it.
 */
public class SerialPortAndroidUsb extends AbstractSerialPort {
    private static final String TAG = "G2Bee";

    private static final int BUFFER_SIZE = 4096;

    private Context context;

    private UsbManager usbManager;

    private UsbSerialProber prober;

    private UsbSerialPort serialPort;

    private OutputStream outputStream;

    private PipedOutputStream pipedOutputStream;

    private PipedInputStream inputStream;

    private Handler writerHandler;

    /**
     * Class constructor. Instances a new {@code SerialPortRxTx} object using the given parameters.
     *
     * @param context    Android context
     * @param port       Serial port name to use.
     * @param parameters Serial port parameters.
     * @throws NullPointerException if {@code port == null} or if {@code parameters == null}.
     * @see #SerialPortAndroidUsb(Context, String, int)
     * @see #SerialPortAndroidUsb(Context, String, int, int)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters, int)
     * @see SerialPortParameters
     */
    public SerialPortAndroidUsb(Context context, String port, SerialPortParameters parameters) {
        this(context, port, parameters, DEFAULT_PORT_TIMEOUT);
    }

    /**
     * Class constructor. Instances a new {@code SerialPortRxTx} object using the given parameters.
     *
     * @param port     Serial port name to use.
     * @param baudRate Serial port baud rate, the rest of parameters will be set by default.
     * @throws NullPointerException if {@code port == null}.
     * @see #DEFAULT_DATA_BITS
     * @see #DEFAULT_FLOW_CONTROL
     * @see #DEFAULT_PARITY
     * @see #DEFAULT_STOP_BITS
     * @see #DEFAULT_PORT_TIMEOUT
     * @see #SerialPortAndroidUsb(Context, String, int, int)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters, int)
     * @see SerialPortParameters
     */
    public SerialPortAndroidUsb(Context context, String port, int baudRate) {
        this(context, port, baudRate, DEFAULT_PORT_TIMEOUT);
    }

    /**
     * Class constructor. Instances a new {@code SerialPortRxTx} object using the given parameters.
     *
     * @param port           Serial port name to use.
     * @param baudRate       Serial port baud rate, the rest of parameters will be set by default.
     * @param receiveTimeout Serial port receive timeout in milliseconds.
     * @throws IllegalArgumentException if {@code receiveTimeout < 0}.
     * @throws NullPointerException     if {@code port == null}.
     * @see #DEFAULT_DATA_BITS
     * @see #DEFAULT_FLOW_CONTROL
     * @see #DEFAULT_PARITY
     * @see #DEFAULT_STOP_BITS
     * @see #SerialPortAndroidUsb(Context, String, int)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters, int)
     * @see SerialPortParameters
     */
    public SerialPortAndroidUsb(Context context, String port, int baudRate, int receiveTimeout) {
        this(context, port, new SerialPortParameters(baudRate, DEFAULT_DATA_BITS,
                DEFAULT_STOP_BITS, DEFAULT_PARITY, DEFAULT_FLOW_CONTROL), receiveTimeout);
    }

    /**
     * Class constructor. Instances a new {@code SerialPortRxTx} object using the given parameters.
     *
     * @param port           Serial port name to use.
     * @param parameters     Serial port parameters.
     * @param receiveTimeout Serial port receive timeout in milliseconds.
     * @throws IllegalArgumentException if {@code receiveTimeout < 0}.
     * @throws NullPointerException     if {@code port == null} or if {@code parameters == null}.
     * @see #SerialPortAndroidUsb(Context, String, int)
     * @see #SerialPortAndroidUsb(Context, String, int, int)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters)
     * @see SerialPortParameters
     */
    public SerialPortAndroidUsb(Context context, String port, SerialPortParameters parameters,
            int receiveTimeout) {
        this(context, null, port, parameters, receiveTimeout);
    }

    /**
     * Class constructor. Instances a new {@code SerialPortRxTx} object using the given parameters.
     *
     * @param port           Serial port name to use.
     * @param parameters     Serial port parameters.
     * @param receiveTimeout Serial port receive timeout in milliseconds.
     * @throws IllegalArgumentException if {@code receiveTimeout < 0}.
     * @throws NullPointerException     if {@code port == null} or if {@code parameters == null}.
     * @see #SerialPortAndroidUsb(Context, String, int)
     * @see #SerialPortAndroidUsb(Context, String, int, int)
     * @see #SerialPortAndroidUsb(Context, String, SerialPortParameters)
     * @see SerialPortParameters
     */
    public SerialPortAndroidUsb(Context context, UsbSerialProber prober, String port,
            SerialPortParameters parameters, int receiveTimeout) {
        super(port, parameters, receiveTimeout);
        this.context = context;
        this.usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        if (prober != null) {
            this.prober = prober;
        } else {
            this.prober = new UsbSerialProber(UsbSerialProber.getDefaultProbeTable());
        }
    }

    @Override
    public void open() throws InterfaceInUseException, InvalidInterfaceException,
            InvalidConfigurationException, PermissionDeniedException {
        HashMap<String, UsbDevice> deviceList = usbManager.getDeviceList();

        UsbDevice device = deviceList.get(port);
        if (device == null) {
            throw new InvalidInterfaceException("No such port: " + port);
        }

        UsbSerialDriver driver = prober.probeDevice(device);

        if (driver == null) {
            throw new InvalidInterfaceException("No device driver for: " + port);
        }

        UsbDeviceConnection connection = usbManager.openDevice(device);
        if (connection == null) {
            throw new InvalidInterfaceException("Cannot open port: " + port);
        }

        if (parameters == null)
            parameters = new SerialPortParameters(baudRate, DEFAULT_DATA_BITS, DEFAULT_STOP_BITS,
                    DEFAULT_PARITY, DEFAULT_FLOW_CONTROL);

        try {
            serialPort = driver.getPorts().get(0);
            serialPort.open(connection);
            connectionOpen = true;
            serialPort.setParameters(baudRate, parameters.dataBits, parameters.stopBits, parameters
                    .parity);

            inputStream = new PipedInputStream(BUFFER_SIZE);
            pipedOutputStream = new PipedOutputStream();

            HandlerThread writerThread = new HandlerThread("Piped writer thread");
            writerThread.start();
            writerHandler = new Handler(writerThread.getLooper());

            outputStream = new OutputStream() {
                @Override
                public void write(final int oneByte) throws IOException {
                    writerHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                pipedOutputStream.write(oneByte);
                            } catch (IOException e) {
                                Log.e(TAG, e.getMessage(), e);
                            }
                        }
                    });
                }
            };

            new SerialInputThread().start();
            new SerialOutputThread().start();
        } catch (IOException e) {
            throw new InvalidConfigurationException(e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        connectionOpen = false;
        try {
            serialPort.close();
        } catch (IOException ignored) {
        }
        try {
            inputStream.close();
        } catch (IOException ignored) {
        }
        try {
            outputStream.close();
        } catch (IOException ignored) {
        }
    }

    @Override
    public void setDTR(boolean state) {
        try {
            serialPort.setDTR(state);
        } catch (IOException ignored) {
        }
    }

    @Override
    public void setRTS(boolean state) {
        try {
            serialPort.setRTS(state);
        } catch (IOException ignored) {
        }
    }

    @Override
    public boolean isCTS() {
        try {
            return serialPort.getCTS();
        } catch (IOException ignored) {
        }
        return false;
    }

    @Override
    public boolean isDSR() {
        try {
            return serialPort.getDSR();
        } catch (IOException ignored) {
        }
        return false;
    }

    @Override
    public boolean isCD() {
        try {
            return serialPort.getCD();
        } catch (IOException ignored) {
        }
        return false;
    }

    @Override
    public void setBreak(boolean enabled) {
    }

    @Override
    public void sendBreak(int duration) {
    }

    @Override
    public void setReadTimeout(int timeout) {
        receiveTimeout = timeout;
    }

    @Override
    public int getReadTimeout() {
        return receiveTimeout;
    }

    @Override
    public InputStream getInputStream() {
        return inputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * Thread that read from XBee connection and write to USB serial port.
     */
    private class SerialOutputThread extends Thread {

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            try {
                PipedInputStream pipedInputStream = new PipedInputStream(pipedOutputStream);
                while ((bytesRead = pipedInputStream.read(buffer)) >= 0) {
                    byte[] data = new byte[bytesRead];
                    System.arraycopy(buffer, 0, data, 0, bytesRead);
                    try {
                        serialPort.write(data, bytesRead + 200);
                    } catch (IOException e) {
                        Log.e(TAG, "IOException writing to usb serial port", e);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occurred while reading output from XBee connection", e);
            }
        }
    }

    /**
     * Thread that read from USB serial port and transfer to XBee connection input stream.
     */
    private class SerialInputThread extends Thread {

        @Override
        public void run() {
            byte[] buffer = new byte[BUFFER_SIZE];
            int bytesRead;
            long readStart = System.currentTimeMillis();
            boolean readSomething = false;
            try {
                PipedOutputStream pipedOutputStream = new PipedOutputStream(inputStream);
                while ((bytesRead = serialPort.read(buffer, receiveTimeout)) >= 0) {
                    if (bytesRead > 0) {
                        readSomething = true;
                        byte[] data = new byte[bytesRead];
                        System.arraycopy(buffer, 0, data, 0, bytesRead);

                        try {
                            pipedOutputStream.write(data);

                            // Implementation required to notify
                            synchronized (SerialPortAndroidUsb.this) {
                                SerialPortAndroidUsb.this.notify();
                            }
                        } catch (IOException e) {
                            Log.e(TAG, "IOException occurred while providing data to piped " +
                                    "input stream", e);
                        }
                    } else if (!readSomething
                            && System.currentTimeMillis() - readStart > receiveTimeout) {
                        throw new IOException(serialPort.getClass().getSimpleName() + " read " +
                                "timeout");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Exception occurred while reading input from usb serial port", e);
            }
            close();
        }
    }

}
