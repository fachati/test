package com.g2mobility.xbee.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.utils.ByteUtils;
import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.recycler.RadioConfiguration;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * A dialog fragment to edit an ASCII radio configuration.
 *
 * @author Hanyu Li
 */
public class ASCIIEditTextDialog extends MaterialDialog.ButtonCallback implements
        RadioConfigurationDialog, MaterialDialog.SingleButtonCallback {

    private Context mContext;
    private RadioConfiguration mConfiguration;
    private String mCommandName;
    private String mDescription;
    private String mRange;

    private MaterialDialog mDialog;

    private MaterialEditText mEditText;

    public ASCIIEditTextDialog(Context context, RadioConfiguration configuration,
            String commandName, String description, String range) {
        mContext = context;
        mConfiguration = configuration;
        mCommandName = commandName;
        mDescription = description;
        mRange = range;

        String value = mContext.getString(R.string.unknown_value);
        byte[] byteValues = mConfiguration.getValue();
        if (byteValues != null) {
            value = ByteUtils.byteArrayToString(byteValues);
        }

        mDialog = new MaterialDialog.Builder(mContext)
                .title(mCommandName)
                .customView(R.layout.dialog_edit_text, true)
                .positiveText(R.string.button_write)
                .negativeText(R.string.button_cancel)
                .onPositive(this)
                .onNegative(this)
                .callback(this)
                .autoDismiss(false)
                .build();

        TextView content = (TextView) mDialog.getCustomView().findViewById(R.id.content);
        content.setText(String.format(mContext.getString(R.string.command_description),
                value) + "\n\n" + mDescription);
        content.setLineSpacing(0f, 1.3f);

        mEditText = (MaterialEditText) mDialog.getCustomView().findViewById(R.id.edit_text_value);
        mEditText.setText(value);
        mEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditText.setSelection(mEditText.getText().length());
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
                String valueString = mEditText.getText().toString();
                byte[] valueToWrite = ByteUtils.stringToByteArray(valueString);
                ((G2BeeActivity) mContext).writeValue(new RadioConfiguration(mConfiguration
                        .getParameter(),
                        valueToWrite));
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
