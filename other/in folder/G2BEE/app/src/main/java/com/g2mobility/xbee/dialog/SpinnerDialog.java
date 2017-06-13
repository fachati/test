package com.g2mobility.xbee.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.recycler.RadioConfiguration;

/**
 * A dialog with a spinner to choose the radio configuration.
 *
 * @author Hanyu Li
 */
public class SpinnerDialog implements RadioConfigurationDialog, MaterialDialog
        .SingleButtonCallback {

    private Context mContext;
    private RadioConfiguration mConfiguration;
    private String mCommandName;
    private String mDescription;
    private String mRange;
    private int[] mValues;

    private MaterialDialog mDialog;

    private Spinner mSpinner;

    public SpinnerDialog(Context context, RadioConfiguration configuration,
            String commandName, String description, String range) {
        mContext = context;
        mConfiguration = configuration;
        mCommandName = commandName;
        mDescription = description;
        mRange = range;

        String value = mContext.getString(R.string.unknown_value);
        byte[] byteValues = configuration.getValue();
        if (byteValues != null) {
            value = HexUtils.byteArrayToHexString(byteValues);
        }

        final String[] choices = mRange.split(",");

        mValues = new int[choices.length];

        int intValue;
        try {
            intValue = Integer.parseInt(value, 16);
        } catch (NumberFormatException e) {
            intValue = -1;
        }

        int choice = -1;
        for (int i = 0; i < choices.length; i++) {
            mValues[i] = Integer.parseInt(choices[i].substring(0, choices[i].indexOf(" ")));
            if (mValues[i] == intValue) {
                choice = i;
            }
        }

        String stringValue = value;
        if (choice > -1) {
            stringValue = choices[choice];
        } else {
            choice = 0;
        }

        mDialog = new MaterialDialog.Builder(mContext)
                .title(mCommandName)
                .customView(R.layout.dialog_spinner, true)
                .positiveText(R.string.button_write)
                .negativeText(R.string.button_cancel)
                .onPositive(this)
                .onNegative(this)
                .autoDismiss(false)
                .build();

        TextView content = (TextView) mDialog.getCustomView().findViewById(R.id.content);
        content.setText(String.format(mContext.getString(R.string.command_description),
                stringValue) + "\n\n" + mDescription);
        content.setLineSpacing(0f, 1.3f);

        mSpinner = (Spinner) mDialog.getCustomView().findViewById(R.id.spinner_value);
        mSpinner.setAdapter(new ArrayAdapter<>(mContext,
                android.R.layout.simple_spinner_dropdown_item, choices));
        mSpinner.setSelection(choice);
    }

    public void show() {
        mDialog.show();
    }

    public void setOnDismissListener(DialogInterface.OnDismissListener listener) {
        mDialog.setOnDismissListener(listener);
    }

    @Override
    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
        switch (which) {
            case POSITIVE:
                int choice = mSpinner.getSelectedItemPosition();
                int valueToWrite = mValues[choice];
                ((G2BeeActivity) mContext).writeValue(new RadioConfiguration(
                        mConfiguration.getParameter(), ByteUtils.intToByteArray(valueToWrite)));
                dialog.dismiss();
                break;
            case NEUTRAL:
                break;
            case NEGATIVE:
                dialog.dismiss();
                break;
        }
    }
}
