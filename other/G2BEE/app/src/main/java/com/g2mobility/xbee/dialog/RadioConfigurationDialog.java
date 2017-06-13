package com.g2mobility.xbee.dialog;

import android.content.DialogInterface;


public interface RadioConfigurationDialog {

    public void show();

    public void setOnDismissListener(DialogInterface.OnDismissListener listener);

}
