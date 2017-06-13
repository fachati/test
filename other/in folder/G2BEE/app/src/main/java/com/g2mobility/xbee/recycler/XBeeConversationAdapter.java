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
import com.g2mobility.xbee.data.G2BeeHelper;
import com.g2mobility.xbee.fragment.XBeeMessageFragment;

import java.text.DateFormat;
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
public class XBeeConversationAdapter
        extends RecyclerView.Adapter<XBeeConversationAdapter.XBeeConversationViewHolder> {

    private List<XBeeConversation> mXBeeConversations;

    public XBeeConversationAdapter() {
        mXBeeConversations = new ArrayList<>();
    }

    @Override
    public int getItemCount() {
        return mXBeeConversations.size();
    }

    @Override
    public void onBindViewHolder(final XBeeConversationViewHolder viewHolder, final int i) {
        final XBeeConversation conversation = mXBeeConversations.get(i);

        Context context = viewHolder.itemView.getContext();

        int color;
        switch (conversation.getType()) {
            case "C":
                color = ContextCompat.getColor(context, R.color.indigo_500);
                break;
            case "R":
                color = ContextCompat.getColor(context, R.color.blue_500);
                break;
            case "E":
                color = ContextCompat.getColor(context, R.color.teal_500);
                break;
            default:
                color = ContextCompat.getColor(context, R.color.black_26);
        }

        ((GradientDrawable) viewHolder.mAvatar.getBackground()).setColor(color);
        viewHolder.mAvatar.setText(conversation.getType());
        viewHolder.mXBeeAddress.setText(HexUtils.prettyHexString(conversation.getAddress()
                .getValue()));
        viewHolder.mMessage.setText(HexUtils.prettyHexString(conversation.getMessage()));

        final DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(
                viewHolder.itemView.getContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(viewHolder.itemView
                .getContext());
        viewHolder.mTime.setText(String.format(context.getString(R.string.message_time_format),
                dateFormat.format(conversation.getTime()),
                timeFormat.format(conversation.getTime())));

        viewHolder.itemView.setTag(i);

        viewHolder.reset();
        viewHolder.mUpperLayer.setTag(conversation.getAddress());
        viewHolder.mUpperLayer.findViewById(R.id.rippleLayout).setOnTouchListener(
                new SwipeDeleteTouchListener(viewHolder.itemView,
                        new SwipeDeleteTouchListener.DismissCallbacks() {

                            @Override
                            public boolean canDismiss() {
                                return true;
                            }


                            @Override
                            public void onDismiss(View downView) {
                                G2BeeHelper.getInstance(downView.getContext()).deleteConversation(
                                        (String) downView.getTag());
                                removeConversation((XBee64BitAddress) downView.getTag());
                            }

                        }, new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        Bundle data = new Bundle();
                        data.putString(v.getResources().getString(R.string.key_xbee_address),
                                conversation.getAddress().toString());
                        data.putString(v.getResources().getString(R.string.key_xbee_type),
                                conversation.getType());

                        Fragment fragment = new XBeeMessageFragment();
                        fragment.setArguments(data);
                        FragmentManager fragmentManager = ((Activity) v.getContext()).
                                getFragmentManager();
                        fragmentManager.beginTransaction()
                                .replace(R.id.content_frame, fragment)
                                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                                .commit();
                    }

                }));
    }

    @Override
    public XBeeConversationViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_xbee_conversation, viewGroup, false);
        return new XBeeConversationViewHolder(itemView);
    }

    public void addConversation(XBeeConversation conversation) {
        int insertPos = 0;
        for (int i = 0; i < mXBeeConversations.size(); i++) {
            XBeeConversation conv = mXBeeConversations.get(i);
            if (conv.getAddress() != null
                    && conv.getAddress().equals(conversation.getAddress())) {
                conv.setMessage(conversation.getMessage());
                conv.setTime(conversation.getTime());
                conv.setType(conversation.getType());
                notifyItemChanged(i);
                return;
            } else if (conv.getTime().after(conversation.getTime())) {
                insertPos = i;
            }
        }
        mXBeeConversations.add(insertPos, conversation);
        notifyItemInserted(insertPos);
    }

    public void removeConversation(XBee64BitAddress address) {
        for (int i = 0; i < mXBeeConversations.size(); i++) {
            if (address.equals(mXBeeConversations.get(i).getAddress())) {
                mXBeeConversations.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    public static final class XBeeConversationViewHolder extends RecyclerView.ViewHolder {

        private ViewGroup mUpperLayer;
        private TextView mAvatar;
        private TextView mXBeeAddress;
        private TextView mMessage;
        private TextView mTime;

        private XBeeConversationViewHolder(View itemView) {
            super(itemView);
            mUpperLayer = (ViewGroup) itemView.findViewById(R.id.upperLayer);
            mAvatar = (TextView) itemView.findViewById(R.id.avatar_text);
            mXBeeAddress = (TextView) itemView.findViewById(R.id.address);
            mMessage = (TextView) itemView.findViewById(R.id.message);
            mTime = (TextView) itemView.findViewById(R.id.time);
        }

        private void reset() {
            mUpperLayer.setX(0);
            mUpperLayer.setAlpha(1);
            mUpperLayer.setBackgroundColor(ContextCompat.getColor(mUpperLayer.getContext(),
                    R.color.background));
        }

    }

}
