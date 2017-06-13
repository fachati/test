package com.g2mobility.xbee.recycler;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.RemoteG2BeeDevice;

import java.util.ArrayList;
import java.util.List;

/**
 * RecyclerView adapter for the {@link com.g2mobility.xbee.fragment.XBeeNodesFragment}.
 * <p/>
 * This adapter uses view holder to hold inflated recycler view items and binds them to {@link
 * RemoteG2BeeDevice}.
 *
 * @author Hanyu Li
 */
public class XBeeDeviceAdapter extends RecyclerView.Adapter<XBeeDeviceAdapter.XBeeNodeViewHolder>
        implements View.OnClickListener {

    private final List<G2BeeDevice> mXBeeDevices;

    private View mSelectedView;
    private XBee64BitAddress mSelectedAddress;

    public XBeeDeviceAdapter() {
        mXBeeDevices = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mXBeeDevices.size();
    }

    @Override
    public void onBindViewHolder(XBeeNodeViewHolder viewHolder, int i) {
        Context context = viewHolder.itemView.getContext();
        Resources res = viewHolder.itemView.getResources();

        String type = res.getString(R.string.node_type_unknown);
        String avatarText = "?";
        String address = res.getString(R.string.node_address_unknown);

        G2BeeDevice device = mXBeeDevices.get(i);

        if (device.getFirmwareVersion() != null) {
            switch (device.getFirmwareVersion()) {
                case "21A7":
                    type = res.getString(R.string.node_type_coordinator);
                    avatarText = "C";
                    break;
                case "23A7":
                case "4059":
                    type = res.getString(R.string.node_type_router);
                    avatarText = "R";
                    break;
                default:
                    type = res.getString(R.string.node_type_end_device);
                    avatarText = "E";
                    break;
            }
        }

        ((GradientDrawable) viewHolder.mAvatarImage.getBackground()).setColor(
                ContextCompat.getColor(context, R.color.blue_500));
        ((GradientDrawable) viewHolder.mAvatarText.getBackground()).setColor(
                ContextCompat.getColor(context, R.color.black_26));
        viewHolder.mAvatarText.setText(avatarText);

        if (device.getXbee64BitAddress() != null) {
            address = HexUtils.prettyHexString(device.getXbee64BitAddress().getValue());
        }
        if (!device.isRemote()) {
            address += " (" + res.getString(R.string.node_address_local) + ")";
        }
        if (device.getNodeID() != null
                && !device.getNodeID().isEmpty()
                && !device.getNodeID().equals(" ")) {
            address += "\nNI: " + device.getNodeID();
        }
        viewHolder.mNodeType.setText(type);
        viewHolder.mAddress.setText(address);

        viewHolder.itemView.setTag(device.getXbee64BitAddress());
        if (mSelectedAddress != null) {
            if (mSelectedAddress.equals(device.getXbee64BitAddress())) {
                mSelectedView = viewHolder.itemView;
                mSelectedView.setSelected(true);
                viewHolder.mAvatarText.setVisibility(View.GONE);
                viewHolder.mAvatarText.setRotationY(0);
                viewHolder.mAvatarImage.setVisibility(View.VISIBLE);
                viewHolder.mAvatarImage.setRotationY(0);
                viewHolder.mAvatarImage.setImageLevel(10000);
            } else {
                viewHolder.mAvatarImage.setVisibility(View.GONE);
                viewHolder.mAvatarText.setVisibility(View.VISIBLE);
                viewHolder.mAvatarImage.setRotationY(0);
                viewHolder.mAvatarText.setRotationY(0);
            }
        }
    }

    @Override
    public XBeeNodeViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.card_xbee_node, viewGroup, false);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            ((CardView) itemView).setCardElevation(itemView.getResources().getDimension(
                    R.dimen.card_elevation_low));
        }
        itemView.setOnClickListener(this);
        return new XBeeNodeViewHolder(itemView);
    }

    public G2BeeDevice getDevice(int position) {
        if (mXBeeDevices.size() > position) {
            return mXBeeDevices.get(position);
        }
        return null;
    }

    public int getSelectedPosition() {
        int position = 0;
        if (mSelectedAddress != null) {
            for (int i = 0; i < mXBeeDevices.size(); i++) {
                G2BeeDevice device = mXBeeDevices.get(i);
                if (device.getXbee64BitAddress() != null
                        && device.getXbee64BitAddress().equals(mSelectedAddress)) {
                    position = i;
                    break;
                }
            }
        }
        return position;
    }

    public XBee64BitAddress getSelectedAddress() {
        return mSelectedAddress;
    }

    public void setSelection(XBee64BitAddress selectedAddress) {
        if (mSelectedAddress != null
                && (selectedAddress == null
                || !mSelectedAddress.equals(selectedAddress))) {
            onXBeeDeselected(mSelectedView);
        }

        if (selectedAddress == null
                && mSelectedAddress == null) {
            return;
        } else if (selectedAddress != null
                && mSelectedAddress != null
                && selectedAddress.equals(mSelectedAddress)) {
            return;
        }

        mSelectedAddress = selectedAddress;
        int position = 0;
        if (selectedAddress != null) {
            for (int i = 0; i < mXBeeDevices.size(); i++) {
                G2BeeDevice device = mXBeeDevices.get(i);
                if (device.getXbee64BitAddress() != null
                        && device.getXbee64BitAddress().equals(mSelectedAddress)) {
                    position = i;
                    break;
                }
            }
        }
        notifyItemChanged(position);
    }

    public void setLocalDevices(List<G2BeeDevice> localDevices) {
        for (G2BeeDevice localDevice : localDevices) {
            boolean found = false;
            for (G2BeeDevice device : mXBeeDevices) {
                if (device.getXbee64BitAddress().equals(localDevice.getXbee64BitAddress())) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                int insertPosition = 0;
                for (int i = 0; i < mXBeeDevices.size(); i++) {
                    G2BeeDevice device = mXBeeDevices.get(i);
                    if (!device.isRemote()
                            && device.getXbee64BitAddress().toString().compareTo(localDevice
                            .getXbee64BitAddress().toString()) < 0) {
                        insertPosition = i + 1;
                    }
                }
                mXBeeDevices.add(insertPosition, localDevice);
                notifyItemInserted(insertPosition);
            }
        }
        List<XBee64BitAddress> toRemove = new ArrayList<>();
        for (G2BeeDevice device : mXBeeDevices) {
            if (!device.isRemote()) {
                boolean found = false;
                for (G2BeeDevice localDevice : localDevices) {
                    if (device.getXbee64BitAddress().equals(localDevice.getXbee64BitAddress())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    toRemove.add(device.getXbee64BitAddress());
                }
            }
        }
        for (XBee64BitAddress deviceToRemove : toRemove) {
            removeDevice(deviceToRemove);
        }
        if (mSelectedAddress == null
                && !mXBeeDevices.isEmpty()) {
            mSelectedAddress = mXBeeDevices.get(0).getXbee64BitAddress();
        }
    }

    public void addRemoteDevice(RemoteG2BeeDevice remoteDevice) {
        int position = -1;
        for (int i = 0; i < mXBeeDevices.size(); i++) {
            G2BeeDevice device = mXBeeDevices.get(i);
            if (device.getXbee64BitAddress().equals(remoteDevice.getXbee64BitAddress())) {
                if (device.isRemote()) {
                    position = i;
                    break;
                } else {
                    return;
                }
            }
        }
        if (position > -1) {
            mXBeeDevices.set(position, remoteDevice);
            notifyItemChanged(position);
        } else {
            mXBeeDevices.add(remoteDevice);
            notifyItemInserted(mXBeeDevices.size() - 1);
        }
    }

    public void removeDevice(XBee64BitAddress address) {
        int removedPosition = -1;
        for (int i = 0; i < mXBeeDevices.size(); i++) {
            if (mXBeeDevices.get(i).getXbee64BitAddress().equals(address)) {
                removedPosition = i;
                break;
            }
        }
        if (removedPosition > -1) {
            if (mSelectedAddress != null
                    && mSelectedAddress.equals(address)) {
                if (mSelectedView != null) {
                    onXBeeDeselected(mSelectedView);
                }
                mSelectedAddress = null;
            }
            mXBeeDevices.remove(removedPosition);
            notifyItemRemoved(removedPosition);
        }
        if (mSelectedAddress == null
                && !mXBeeDevices.isEmpty()) {
            mSelectedAddress = mXBeeDevices.get(0).getXbee64BitAddress();
            notifyItemChanged(0);
        }
    }

    @Override
    public void onClick(final View v) {
        if (mSelectedView != null
                && !mSelectedView.equals(v)) {
            onXBeeDeselected(mSelectedView);
        }
        onXBeeSelected(v);
    }

    private void onXBeeSelected(final View v) {
        if (!v.isSelected()) {
            v.setSelected(true);
            mSelectedAddress = (XBee64BitAddress) v.getTag();
            mSelectedView = v;

            final TextView avatarText = (TextView) v.findViewById(R.id.avatar_text);
            final ImageView avatarImage = (ImageView) v.findViewById(R.id.avatar_image);

            ObjectAnimator animatorOut = (ObjectAnimator) AnimatorInflater.loadAnimator(
                    v.getContext(), R.animator.avatar_rotation_out);
            animatorOut.setTarget(avatarText);
            animatorOut.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    avatarText.setVisibility(View.GONE);
                    avatarImage.setVisibility(View.VISIBLE);
                    avatarText.setRotationY(0);

                    ObjectAnimator set = (ObjectAnimator) AnimatorInflater.loadAnimator(
                            v.getContext(), R.animator.ic_selected_zoom);
                    set.setTarget(avatarImage.getDrawable());
                    set.start();
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            });

            ObjectAnimator animatorIn = (ObjectAnimator) AnimatorInflater.loadAnimator(
                    v.getContext(), R.animator.avatar_rotation_in);
            animatorIn.setTarget(avatarImage);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animatorOut, animatorIn);
            set.start();
        }
    }

    private void onXBeeDeselected(final View v) {
        if (v != null && v.isSelected()) {
            v.setSelected(false);
            mSelectedView = null;

            final TextView avatarText = (TextView) v.findViewById(R.id.avatar_text);
            final ImageView avatarImage = (ImageView) v.findViewById(R.id.avatar_image);

            ObjectAnimator animatorOut = (ObjectAnimator) AnimatorInflater.loadAnimator(
                    v.getContext(), R.animator.avatar_rotation_out);
            animatorOut.setTarget(avatarImage);
            animatorOut.addListener(new Animator.AnimatorListener() {

                @Override
                public void onAnimationStart(Animator animation) {
                }

                @Override
                public void onAnimationEnd(Animator animation) {
                    avatarImage.setVisibility(View.GONE);
                    avatarText.setVisibility(View.VISIBLE);
                    avatarImage.setRotationY(0);
                }

                @Override
                public void onAnimationCancel(Animator animation) {
                }

                @Override
                public void onAnimationRepeat(Animator animation) {
                }

            });

            ObjectAnimator animatorIn = (ObjectAnimator) AnimatorInflater.loadAnimator(
                    v.getContext(), R.animator.avatar_rotation_in);
            animatorIn.setTarget(avatarText);

            AnimatorSet set = new AnimatorSet();
            set.playSequentially(animatorOut, animatorIn);
            set.start();
        }
    }

    public static final class XBeeNodeViewHolder extends RecyclerView.ViewHolder {

        private ImageView mAvatarImage;
        private TextView mAvatarText;
        private TextView mNodeType;
        private TextView mAddress;

        private XBeeNodeViewHolder(View itemView) {
            super(itemView);
            mAvatarImage = (ImageView) itemView.findViewById(R.id.avatar_image);
            mAvatarText = (TextView) itemView.findViewById(R.id.avatar_text);
            mNodeType = (TextView) itemView.findViewById(R.id.node_type);
            mAddress = (TextView) itemView.findViewById(R.id.node_address);
        }

    }

}
