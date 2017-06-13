package com.g2mobility.xbee;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.support.v4.content.LocalBroadcastManager;

/**
 * Broadcast receiver that receives broadcast action and processes XBee commands.
 *
 * @author Hanyu Li
 */
public class G2BeeReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent != null && intent.getAction() != null) {
            switch (intent.getAction()) {
                case G2BeeConstants.ACTION_DEBUG_ON:
                case G2BeeConstants.ACTION_DEBUG_OFF:
                case G2BeeConstants.ACTION_DISCOVER:
                case G2BeeConstants.ACTION_EXECUTE_PARAMETER:
                case G2BeeConstants.ACTION_GET_PARAMETER:
                case G2BeeConstants.ACTION_GRANT_PERMISSION:
                case G2BeeConstants.ACTION_LIST_DEVICE:
                case G2BeeConstants.ACTION_SEND_DATA:
                case G2BeeConstants.ACTION_SET_PARAMETER:
                    intent.setClass(context, G2BeeService.class);
                    context.startService(intent);
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED:
                case UsbManager.ACTION_USB_DEVICE_DETACHED:
                    intent.setClass(context, G2BeeService.class);
                    context.startService(intent);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                    break;
            }
        }
    }

}
