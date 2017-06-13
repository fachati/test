package com.g2mobility.xbee;

/**
 * G2Bee constants.
 */
public class G2BeeConstants {

    /**
     * Intent action that is called back when USB manager grants permission for a USB device.
     */
    public static final String ACTION_GRANT_PERMISSION
            = "com.g2mobility.XBEE_GRANT_PERMISSION";
    /**
     * Intent action to list XBee devices.
     */
    public static final String ACTION_LIST_DEVICE
            = "com.g2mobility.XBEE_LIST_DEVICE";
    /**
     * Intent action to discover remote XBee devices.
     */
    public static final String ACTION_DISCOVER
            = "com.g2mobility.XBEE_DISCOVER";
    /**
     * Intent action to get XBee parameter.
     * <p/>
     * Extras "addr" and "param" should be provided indicating the XBee device address and the
     * parameter to get.
     *
     * @see #KEY_ADDRESS
     * @see #KEY_PARAMETER
     */
    public static final String ACTION_GET_PARAMETER
            = "com.g2mobility.XBEE_GET_PARAMETER";
    /**
     * Intent action to set XBee parameter
     * <p/>
     * Extras "addr", "cmd" and "value" should be provided indicating the remote XBee address, the
     * parameter and the value to set.
     *
     * @see #KEY_ADDRESS
     * @see #KEY_PARAMETER
     * @see #KEY_VALUE
     */
    public static final String ACTION_SET_PARAMETER
            = "com.g2mobility.XBEE_SET_PARAMETER";
    /**
     * Intent action to execute XBee parameter.
     * <p/>
     * Extras "addr" and "param" should be provided indicating the XBee device address and the
     * parameter to execute.
     *
     * @see #KEY_ADDRESS
     * @see #KEY_PARAMETER
     */
    public static final String ACTION_EXECUTE_PARAMETER
            = "com.g2mobility.XBEE_EXECUTE_PARAMETER";
    /**
     * Intent action to send data to XBee device.
     * <p/>
     * Extras "addr" and "payload" should be provided indicating the destination XBee address and
     * the data frame to send.
     *
     * @see #KEY_ADDRESS
     * @see #KEY_PAYLOAD
     */
    public static final String ACTION_SEND_DATA
            = "com.g2mobility.XBEE_SEND_DATA";
    /**
     * Intent action that enables debugging.
     *
     * @see #ACTION_DEBUG_OFF
     */
    public static final String ACTION_DEBUG_ON
            = "com.g2mobility.XBEE_DEBUG_ON";
    /**
     * Intent action that disables debugging previously enabled by {@link #ACTION_DEBUG_ON}.
     *
     * @see #ACTION_DEBUG_ON
     */
    public static final String ACTION_DEBUG_OFF
            = "com.g2mobility.XBEE_DEBUG_OFF";

    /**
     * Extra key indicating the XBee address. The format is 8 hexadecimals without space.
     */
    public static final String KEY_ADDRESS = "addr";
    /**
     * Extra key indicating the XBee parameter. The parameter is 2 characters.
     */
    public static final String KEY_PARAMETER = "param";
    /**
     * Extra key indicating the XBee parameter value. The value is either hexadecimal or string (for
     * "NI" only).
     */
    public static final String KEY_VALUE = "value";
    /**
     * Extra key indicating the payload data to send.
     */
    public static final String KEY_PAYLOAD = "payload";

    private G2BeeConstants() {
    }

}
