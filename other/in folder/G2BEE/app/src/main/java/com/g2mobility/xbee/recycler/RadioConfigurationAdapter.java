package com.g2mobility.xbee.recycler;

import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.dialog.ASCIIEditTextDialog;
import com.g2mobility.xbee.dialog.EditTextDialog;
import com.g2mobility.xbee.dialog.RadioConfigurationDialog;
import com.g2mobility.xbee.dialog.ReadOnlyDialog;
import com.g2mobility.xbee.dialog.SliderDialog;
import com.g2mobility.xbee.dialog.SpinnerDialog;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for recycle view of radio configurations.
 *
 * @author Hanyu Li
 */
public class RadioConfigurationAdapter extends RecyclerView.Adapter<RadioConfigurationAdapter
        .RadioConfigurationViewHolder> implements View.OnClickListener, DialogInterface
        .OnDismissListener {

    private int mCommandNames;
    private int mDescriptions;
    private int mInputTypes;
    private int mRanges;
    private int mAvatarColor;
    private List<RadioConfiguration> mConfigurations;

    private boolean mIsShowingDialog = false;

    public RadioConfigurationAdapter(int commandNames, int descriptions, int inputTypes, int ranges,
            int avatarColor) {
        mConfigurations = new ArrayList<>();
        mCommandNames = commandNames;
        mDescriptions = descriptions;
        mInputTypes = inputTypes;
        mRanges = ranges;
        mAvatarColor = avatarColor;
    }

    @Override
    public int getItemCount() {
        return mConfigurations.size();
    }

    @Override
    public void onBindViewHolder(RadioConfigurationViewHolder viewHolder, int i) {
        viewHolder.itemView.setTag(i);
        Resources res = viewHolder.itemView.getResources();

        RadioConfiguration conf = mConfigurations.get(i);
        byte[] value = conf.getValue();

        String stringValue = res.getString(R.string.unknown_value);
        if (value != null) {
            String inputType = res.getStringArray(mInputTypes)[i];
            switch (inputType) {
                case "SPINNER":
                    int choice = 0;
                    String[] choices = res.getStringArray(mRanges)[i].split(",");
                    final int[] values = new int[choices.length];
                    int intValue = ByteUtils.byteArrayToInt(value);

                    for (int j = 0; j < choices.length; j++) {
                        values[j] = Integer.parseInt(choices[j].substring(0,
                                choices[j].indexOf(" ")));
                        if (values[j] == intValue) {
                            choice = j;
                        }
                    }
                    stringValue = choices[choice];
                    break;
                case "ASCII EDIT TEXT":
                    stringValue = ByteUtils.byteArrayToString(value);
                    break;
                default:
                    stringValue = HexUtils.byteArrayToHexString(value);
                    break;
            }
        }

        viewHolder.mAvatar.setText(conf.getParameter());
        ((GradientDrawable) viewHolder.mAvatar.getBackground()).setColor(mAvatarColor);

        viewHolder.mCommand.setText(viewHolder.mCommand.getResources()
                .getStringArray(mCommandNames)[i]);

        viewHolder.mValue.setText(stringValue);
    }

    @Override
    public RadioConfigurationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_radio_configuration, viewGroup, false);
        itemView.setOnClickListener(this);
        return new RadioConfigurationViewHolder(itemView);
    }

    @Override
    public void onClick(final View v) {
        if (!mIsShowingDialog) {
            mIsShowingDialog = true;

            int position = (int) v.getTag();
            Resources res = v.getResources();
            String inputType = res.getStringArray(mInputTypes)[position];
            RadioConfiguration configuration = mConfigurations.get(position);
            String commandName = res.getStringArray(mCommandNames)[position];
            String description = res.getStringArray(mDescriptions)[position];
            String range = res.getStringArray(mRanges)[position];

            RadioConfigurationDialog dialog;
            switch (inputType) {
                case "READ ONLY":
                    dialog = new ReadOnlyDialog(v.getContext(), configuration, commandName,
                            description, range);
                    break;
                case "SPINNER":
                    dialog = new SpinnerDialog(v.getContext(), configuration, commandName,
                            description, range);
                    break;
                case "EDIT TEXT":
                    dialog = new EditTextDialog(v.getContext(), configuration, commandName,
                            description, range);
                    break;
                case "ASCII EDIT TEXT":
                    dialog = new ASCIIEditTextDialog(v.getContext(), configuration,
                            commandName, description, range);
                    break;
                default:
                    dialog = new SliderDialog(v.getContext(), configuration, commandName,
                            description, range);
                    break;
            }
            dialog.setOnDismissListener(this);
            dialog.show();
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        mIsShowingDialog = false;
    }

    public void setConfiguration(RadioConfiguration configuration) {
        for (int i = 0; i < mConfigurations.size(); i++) {
            if (mConfigurations.get(i).getParameter().equals(configuration.getParameter())) {
                mConfigurations.get(i).setValue(configuration.getValue());
                notifyItemChanged(i);
                return;
            }
        }
        mConfigurations.add(configuration);
        notifyItemInserted(mConfigurations.size() - 1);
    }

    public static final class RadioConfigurationViewHolder extends RecyclerView.ViewHolder {

        private TextView mAvatar;
        private TextView mCommand;
        private TextView mValue;

        private RadioConfigurationViewHolder(View itemView) {
            super(itemView);
            mAvatar = (TextView) itemView.findViewById(R.id.avatar_text);
            mCommand = (TextView) itemView.findViewById(R.id.command);
            mValue = (TextView) itemView.findViewById(R.id.value);
        }

    }

}
