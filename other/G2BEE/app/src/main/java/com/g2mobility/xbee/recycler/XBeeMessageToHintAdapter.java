package com.g2mobility.xbee.recycler;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.fragment.XBeeMessageFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the {@link com.g2mobility.xbee.fragment.XBeeNodesFragment}.
 * <p/>
 * This adapter uses view holder to hold inflated recycler view items and binds them to {@link
 * com.rapplogic.xbee.api.zigbee.NodeDiscover}.
 *
 * @author Hanyu Li
 */
public class XBeeMessageToHintAdapter
        extends RecyclerView.Adapter<XBeeMessageToHintAdapter.XBeeHintViewHolder>
        implements View.OnClickListener {

    private List<G2BeeDevice> mXBeeDevices;

    public XBeeMessageToHintAdapter() {
        mXBeeDevices = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mXBeeDevices.size();
    }

    @Override
    public void onBindViewHolder(XBeeHintViewHolder viewHolder, final int i) {
        G2BeeDevice device = mXBeeDevices.get(i);

        Context context = viewHolder.itemView.getContext();

        int color = ContextCompat.getColor(context, R.color.black_26);
        String type = "?";
        if (device.getFirmwareVersion() != null) {
            switch (device.getFirmwareVersion()) {
                case "21A7":
                    color = ContextCompat.getColor(context, R.color.indigo_500);
                    type = "C";
                    break;
                case "23A7":
                case "4059":
                    color = ContextCompat.getColor(context, R.color.blue_500);
                    type = "R";
                    break;
                default:
                    color = ContextCompat.getColor(context, R.color.teal_500);
                    type = "E";
                    break;
            }
        }

        ((GradientDrawable) viewHolder.mAvatar.getBackground()).setColor(color);
        viewHolder.mAvatar.setText(type);

        viewHolder.mAddress.setText(HexUtils.byteArrayToHexString(device.getXbee64BitAddress()
                .getValue()));
        if (device.getNodeID() != null
                && !device.getNodeID().isEmpty()
                && !device.getNodeID().equals(" ")) {
            viewHolder.mNodeIdentifier.setText(device.getNodeID());
        } else {
            viewHolder.mNodeIdentifier.setVisibility(View.GONE);
        }

        viewHolder.itemView.setOnClickListener(this);
        viewHolder.itemView.setTag(i);
    }

    @Override
    public XBeeHintViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_xbee_hint, viewGroup, false);
        return new XBeeHintViewHolder(itemView);
    }

    @Override
    public void onClick(View v) {
        int position = (int) v.getTag();

        String type = "?";
        switch (mXBeeDevices.get(position).getFirmwareVersion()) {
            case "21A7":
                type = "C";
                break;
            case "23A7":
            case "4059":
                type = "R";
                break;
            default:
                type = "E";
                break;
        }

        Bundle data = new Bundle();
        data.putString(v.getResources().getString(R.string.key_xbee_address),
                HexUtils.byteArrayToHexString(mXBeeDevices.get(position).getXbee64BitAddress()
                        .getValue()));

        data.putString(v.getResources().getString(R.string.key_xbee_type), type);

        Fragment fragment = new XBeeMessageFragment();
        fragment.setArguments(data);
        FragmentManager fragmentManager = ((Activity) v.getContext()).
                getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();
    }

    public void addXBeeDevice(G2BeeDevice xbeeDevice) {
        for (G2BeeDevice device : mXBeeDevices) {
            if (xbeeDevice.getXbee64BitAddress().equals(device.getXbee64BitAddress())) {
                return;
            }
        }
        mXBeeDevices.add(xbeeDevice);
        notifyItemInserted(mXBeeDevices.size() - 1);
    }

    public String getXBeeNodeType(String address) {
        XBee64BitAddress address64 = new XBee64BitAddress(address);
        for (G2BeeDevice node : mXBeeDevices) {
            if (node.getXbee64BitAddress().equals(address64)) {
                if (node.getFirmwareVersion() != null) {
                    switch (node.getFirmwareVersion()) {
                        case "21A7":
                            return "C";
                        case "23A7":
                        case "4059":
                            return "R";
                        default:
                            return "E";
                    }
                }
            }
        }
        return "?";
    }

    public static final class XBeeHintViewHolder extends RecyclerView.ViewHolder {

        private TextView mAvatar;
        private TextView mAddress;
        private TextView mNodeIdentifier;

        private XBeeHintViewHolder(View itemView) {
            super(itemView);
            mAvatar = (TextView) itemView.findViewById(R.id.avatar_text);
            mAddress = (TextView) itemView.findViewById(R.id.hint_xbee_address);
            mNodeIdentifier = (TextView) itemView.findViewById(R.id.hint_node_identifier);
        }

    }

}
