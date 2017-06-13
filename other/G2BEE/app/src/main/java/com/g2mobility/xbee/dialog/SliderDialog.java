package com.g2mobility.xbee.dialog;

import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.widget.SeekBar;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.recycler.RadioConfiguration;
import com.rengwuxian.materialedittext.MaterialEditText;

/**
 * A dialog with a slider to adjust the radio configuration.
 *
 * @author Hanyu Li
 */
public class SliderDialog implements RadioConfigurationDialog, MaterialDialog.SingleButtonCallback {

    private Context mContext;
    private RadioConfiguration mConfiguration;
    private String mCommandName;
    private String mDescription;
    private String mRange;
    private int mMin;

    private MaterialDialog mDialog;

    private SeekBar mSlider;
    private MaterialEditText mEditText;

    public SliderDialog(Context context, RadioConfiguration configuration,
            String commandName, String description, String range) {
        mContext = context;
        mConfiguration = configuration;
        mCommandName = commandName;
        mDescription = description;
        mRange = range;

        String value = mContext.getString(R.string.unknown_value);
        byte[] byteValues = mConfiguration.getValue();
        if (byteValues != null) {
            value = HexUtils.byteArrayToHexString(byteValues);
        }

        String[] ranges = mRange.split(",");
        mMin = Integer.parseInt(ranges[0], 16);
        int max = Integer.parseInt(ranges[1], 16);
        int defaultValue = Integer.parseInt(ranges[2], 16);

        final int size;
        if ((max >> 24) > 0) {
            size = 4;
        } else if ((max >> 16) > 0) {
            size = 3;
        } else if ((max >> 8) > 0) {
            size = 2;
        } else {
            size = 1;
        }

        int intValue;
        try {
            intValue = Integer.parseInt(value, 16);
        } catch (NumberFormatException e) {
            intValue = defaultValue;
        }
        byteValues = new byte[size];
        for (int i = 0; i < size; i++) {
            byteValues[i] = (byte) (((intValue) >> (size - i - 1) * 8) & 0xFF);
        }

        mDialog = new MaterialDialog.Builder(mContext)
                .title(mCommandName)
                .customView(R.layout.dialog_slider, true)
                .positiveText(R.string.button_write)
                .negativeText(R.string.button_cancel)
                .onPositive(this)
                .onNegative(this)
                .autoDismiss(false)
                .build();

        TextView content = (TextView) mDialog.getCustomView().findViewById(R.id.content);
        content.setText(String.format(mContext.getString(R.string.command_description),
                value) + "\n\n" + mDescription);
        content.setLineSpacing(0f, 1.3f);

        mSlider = (SeekBar) mDialog.getCustomView().findViewById(R.id.slider_value);
        mSlider.setMax(max - mMin);
        mSlider.setProgress(intValue - mMin);

        mEditText = (MaterialEditText) mDialog.getCustomView().findViewById(R.id.edit_text_value);
        mEditText.setText(HexUtils.byteArrayToHexString(byteValues));
        mEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        mEditText.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                String value = s.toString();
                try {
                    int intValue = Integer.parseInt(value, 16);
                    mSlider.setProgress(intValue - mMin);
                } catch (NumberFormatException e) {
                    // Ignore number format error
                }
            }
        });

        mSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!mEditText.isFocused()) {
                    byte[] value = new byte[size];
                    for (int i = 0; i < size; i++) {
                        value[i] = (byte) (((progress + mMin) >> (size - i - 1) * 8) & 0xFF);
                    }
                    mEditText.setText(HexUtils.byteArrayToHexString(value));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mEditText.clearFocus();
                seekBar.requestFocus();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
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
                int valueToWrite = mSlider.getProgress() + mMin;
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
