package com.g2mobility.xbee.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.recycler.RadioConfiguration;

/**
 * A dialog to display a read only radio configuration.
 *
 * @author Hanyu Li
 */
public class ReadOnlyDialog implements RadioConfigurationDialog {

    private Context mContext;
    private RadioConfiguration mConfiguration;
    private String mCommandName;
    private String mDescription;

    private MaterialDialog mDialog;

    public ReadOnlyDialog(Context context, RadioConfiguration configuration,
            String commandName, String description, String range) {
        mContext = context;
        mConfiguration = configuration;
        mCommandName = commandName;
        mDescription = description;

        String value = mContext.getString(R.string.unknown_value);
        byte[] byteValues = mConfiguration.getValue();
        if (byteValues != null) {
            value = HexUtils.byteArrayToHexString(byteValues);
            if (mCommandName.equals("%V")) {
                value += " (" + (ByteUtils.byteArrayToInt(byteValues) * 1200 / 1024) + "mV)";
            }
        }

        mDialog = new MaterialDialog.Builder(mContext)
                .title(mCommandName)
                .customView(R.layout.dialog_read_only, true)
                .positiveText(R.string.button_ok)
                .autoDismiss(true)
                .build();

        TextView content = (TextView) mDialog.getCustomView().findViewById(R.id.content);
        content.setText(String.format(mContext.getString(R.string.command_description), value) +
                "\n\n" + mDescription);
        content.setLineSpacing(0f, 1.3f);
    }

    public void show() {
        mDialog.show();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mDialog.setOnDismissListener(listener);
    }

}
