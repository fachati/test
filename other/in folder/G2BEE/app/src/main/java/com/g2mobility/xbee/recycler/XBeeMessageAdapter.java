package com.g2mobility.xbee.recycler;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.GradientDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.ActionMode;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.data.G2BeeHelper;

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
public class XBeeMessageAdapter
        extends RecyclerView.Adapter<XBeeMessageAdapter.XBeeMessageViewHolder> implements View
        .OnLongClickListener {

    private String mAddress;
    private String mLocalType;
    private String mRemoteType;
    private List<RecyclerXBeeMessage> mXBeeMessages;

    private boolean mHexDisplayEnabled;

    private View mSelected;
    private Integer mSelectedPosition;

    private ActionMode mActionMode;
    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback() {

        @Override
        public boolean onCreateActionMode(ActionMode mode,
                Menu menu) { // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.copy:
                    copyXBeeMessage();
                    mode.finish();
                    return true;
                case R.id.delete:
                    deleteXBeeMessage();
                    mode.finish();
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
            if (mSelected != null) {
                onXBeeMessageDeselected(mSelected);
                mSelected = null;
                mSelectedPosition = null;
            }
        }

    };

    public XBeeMessageAdapter(String address, String localType, String remoteType) {
        mAddress = address;
        mLocalType = localType;
        mRemoteType = remoteType;
        mXBeeMessages = new ArrayList<>();
        mHexDisplayEnabled = true;
    }

    @Override
    public int getItemCount() {
        return mXBeeMessages.size();
    }

    @Override
    public void onBindViewHolder(XBeeMessageViewHolder viewHolder, final int i) {
        RecyclerXBeeMessage conversation = mXBeeMessages.get(i);

        Context context = viewHolder.itemView.getContext();
        Resources res = viewHolder.itemView.getResources();

        int color;
        String type = mRemoteType;
        if (conversation.isOutgoing()) {
            type = mLocalType;
        }
        switch (type) {
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
                color = ContextCompat.getColor(context, R.color.grey_400);
        }

        RelativeLayout.LayoutParams avatarTextLp = new RelativeLayout.LayoutParams(
                res.getDimensionPixelSize(R.dimen.avatar_diameter),
                res.getDimensionPixelSize(R.dimen.avatar_diameter));

        RelativeLayout.LayoutParams avatarImageLp = new RelativeLayout.LayoutParams(
                res.getDimensionPixelSize(R.dimen.avatar_diameter),
                res.getDimensionPixelSize(R.dimen.avatar_diameter));

        RelativeLayout.LayoutParams bubbleLp = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT,
                RelativeLayout.LayoutParams.WRAP_CONTENT);
        if (conversation.isOutgoing()) {
            ((GradientDrawable) viewHolder.mAvatarText.getBackground()).setColor(
                    ContextCompat.getColor(context, R.color.black_26));
            viewHolder.mMessage.setTextColor(ContextCompat.getColor(context, R.color.black_87));
            viewHolder.mTime.setTextColor(ContextCompat.getColor(context, R.color.black_26));

            viewHolder.mBubble.setBackgroundResource(R.drawable.msg_bubble_outgoing);
            viewHolder.mBubble.getBackground().setColorFilter(
                    new PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP));

            avatarImageLp.setMarginEnd(res.getDimensionPixelSize(R.dimen.bubble_padding));
            avatarImageLp.addRule(RelativeLayout.ALIGN_PARENT_END);

            avatarTextLp.setMarginEnd(res.getDimensionPixelSize(R.dimen.bubble_padding));
            avatarTextLp.addRule(RelativeLayout.LEFT_OF, R.id.avatar_image);
            avatarTextLp.addRule(RelativeLayout.ALIGN_PARENT_END);

            bubbleLp.setMarginStart(res.getDimensionPixelSize(R.dimen.bubble_margin));
            bubbleLp.addRule(RelativeLayout.LEFT_OF, R.id.avatar_text);
        } else {
            ((GradientDrawable) viewHolder.mAvatarText.getBackground()).setColor(color);
            viewHolder.mMessage.setTextColor(ContextCompat.getColor(context, R.color.white_100));
            viewHolder.mTime.setTextColor(ContextCompat.getColor(context, R.color.white_70));

            viewHolder.mBubble.setBackgroundResource(R.drawable.msg_bubble_incoming);
            viewHolder.mBubble.getBackground().setColorFilter(
                    new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

            avatarImageLp.setMarginStart(res.getDimensionPixelSize(R.dimen.bubble_padding));
            avatarImageLp.addRule(RelativeLayout.ALIGN_PARENT_START);

            avatarTextLp.setMarginStart(res.getDimensionPixelSize(R.dimen.bubble_padding));
            avatarTextLp.addRule(RelativeLayout.RIGHT_OF, R.id.avatar_image);

            bubbleLp.setMarginEnd(res.getDimensionPixelSize(R.dimen.bubble_margin));
            bubbleLp.addRule(RelativeLayout.RIGHT_OF, R.id.avatar_text);
        }

        viewHolder.mAvatarImage.setLayoutParams(avatarImageLp);
        viewHolder.mAvatarText.setLayoutParams(avatarTextLp);
        viewHolder.mBubble.setLayoutParams(bubbleLp);

        viewHolder.mAvatarText.setText(mRemoteType);
        if (mHexDisplayEnabled) {
            viewHolder.mMessage.setText(HexUtils.prettyHexString(conversation.getMessage()));
        } else {
            viewHolder.mMessage.setText(ByteUtils.byteArrayToString(conversation.getMessage()));
        }

        DateFormat dateFormat = android.text.format.DateFormat.getMediumDateFormat(
                viewHolder.itemView.getContext());
        DateFormat timeFormat = android.text.format.DateFormat.getTimeFormat(
                viewHolder.itemView.getContext());
        viewHolder.mTime.setText(String.format(context.getString(R.string.message_time_format),
                dateFormat.format(conversation.getTime()),
                timeFormat.format(conversation.getTime())));

        viewHolder.itemView.setTag(conversation.getTime().getTime());
        viewHolder.mAvatarText.setOnLongClickListener(this);
        viewHolder.mBubble.setOnLongClickListener(this);
    }

    @Override
    public XBeeMessageViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext()).inflate(
                R.layout.item_xbee_message, viewGroup, false);
        return new XBeeMessageViewHolder(itemView);
    }

    @Override
    public boolean onLongClick(View v) {
        v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
        if (mActionMode == null) {
            mActionMode = ((Activity) v.getContext()).findViewById(R.id.toolbar).startActionMode
                    (mActionModeCallback);
        } else if (mSelected != null) {
            if (!mSelected.equals(v.getParent())) {
                onXBeeMessageDeselected(mSelected);
            }
        }
        View itemView = (View) v.getParent();
        mSelected = itemView;

        long timestamp = (long) mSelected.getTag();
        for (int i = 0; i < mXBeeMessages.size(); i++) {
            if (timestamp == mXBeeMessages.get(i).getTime().getTime()) {
                mSelectedPosition = i;
                break;
            }
        }

        onXBeeMessageSelected(itemView);

        return false;
    }

    public boolean cancelActionMode() {
        if (mActionMode != null) {
            mActionMode.finish();
            return true;
        }
        return false;
    }

    public void addMessage(RecyclerXBeeMessage message) {
        for (int i = 0; i < mXBeeMessages.size(); i++) {
            RecyclerXBeeMessage msg = mXBeeMessages.get(i);
            if (msg.getTime().equals(message.getTime())) {
                return;
            }
            if (msg.getTime().before(message.getTime())) {
                mXBeeMessages.add(i, message);
                notifyItemInserted(i);
                return;
            }
        }
        mXBeeMessages.add(message);
        notifyItemInserted(mXBeeMessages.size());
    }

    public void enableHexDisplay(boolean enable) {
        if (enable != mHexDisplayEnabled) {
            mHexDisplayEnabled = enable;
            notifyItemRangeChanged(0, mXBeeMessages.size());
        }
    }

    private void onXBeeMessageSelected(final View v) {
        if (!v.isSelected()) {
            v.setSelected(true);

            final Context context = v.getContext();
            final ImageView avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
            final TextView avatarText = (TextView) v.findViewById(R.id.avatar_text);
            final ViewGroup bubble = (ViewGroup) v.findViewById(R.id.message_bubble);
            final boolean isOutgoing = mXBeeMessages.get(mSelectedPosition).isOutgoing();

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

                    bubble.getBackground().setColorFilter(
                            new PorterDuffColorFilter(ContextCompat.getColor(context, R.color
                                    .indigo_900), PorterDuff.Mode.SRC_ATOP));
                    if (isOutgoing) {
                        TextView message = (TextView) v.findViewById(R.id.message);
                        message.setTextColor(ContextCompat.getColor(context, R.color.white_100));
                        TextView time = (TextView) v.findViewById(R.id.message_time);
                        time.setTextColor(ContextCompat.getColor(context, R.color.white_70));
                    }

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

    private void onXBeeMessageDeselected(final View v) {
        if (v.isSelected()) {
            v.setSelected(false);

            final Context context = v.getContext();
            final ImageView avatarImage = (ImageView) v.findViewById(R.id.avatar_image);
            final TextView avatarText = (TextView) v.findViewById(R.id.avatar_text);
            final ViewGroup bubble = (ViewGroup) v.findViewById(R.id.message_bubble);
            final boolean isOutgoing = mXBeeMessages.get(mSelectedPosition).isOutgoing();

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

                    int color = ContextCompat.getColor(context, R.color.green_400);
                    switch (avatarText.getText().toString()) {
                        case "C":
                            color = ContextCompat.getColor(context, R.color.indigo_500);
                            break;
                        case "R":
                            color = ContextCompat.getColor(context, R.color.blue_500);
                            break;
                        case "E":
                            color = ContextCompat.getColor(context, R.color.teal_500);
                            break;
                    }
                    if (isOutgoing) {
                        TextView message = (TextView) v.findViewById(R.id.message);
                        message.setTextColor(ContextCompat.getColor(context, R.color.black_87));
                        TextView time = (TextView) v.findViewById(R.id.message_time);
                        time.setTextColor(ContextCompat.getColor(context, R.color.black_26));

                        color = ContextCompat.getColor(context, R.color.white_100);
                    }
                    bubble.getBackground().setColorFilter(
                            new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));
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

    private void copyXBeeMessage() {
        ClipboardManager clipboard = (ClipboardManager)
                mSelected.getContext().getSystemService(Context.CLIPBOARD_SERVICE);

        TextView message = (TextView) mSelected.findViewById(R.id.message);
        ClipData clip = ClipData.newPlainText("XBee message", message.getText().toString());
        clipboard.setPrimaryClip(clip);
    }

    private void deleteXBeeMessage() {
        mSelected.setSelected(false);
        final Context context = mSelected.getContext();
        final ImageView avatarImage = (ImageView) mSelected.findViewById(R.id.avatar_image);
        final TextView avatarText = (TextView) mSelected.findViewById(R.id.avatar_text);
        final ViewGroup bubble = (ViewGroup) mSelected.findViewById(R.id.message_bubble);

        avatarImage.setVisibility(View.GONE);
        avatarText.setVisibility(View.VISIBLE);

        int color = ContextCompat.getColor(context, R.color.grey_400);
        switch (avatarText.getText().toString()) {
            case "C":
                color = ContextCompat.getColor(context, R.color.indigo_500);
                break;
            case "R":
                color = ContextCompat.getColor(context, R.color.blue_500);
                break;
            case "E":
                color = ContextCompat.getColor(context, R.color.teal_500);
                break;
        }
        if (mXBeeMessages.get(mSelectedPosition).isOutgoing()) {
            TextView message = (TextView) mSelected.findViewById(R.id.message);
            message.setTextColor(ContextCompat.getColor(context, R.color.black_87));
            TextView time = (TextView) mSelected.findViewById(R.id.message_time);
            time.setTextColor(ContextCompat.getColor(context, R.color.black_26));

            color = ContextCompat.getColor(context, R.color.white_100);
        }
        bubble.getBackground().setColorFilter(
                new PorterDuffColorFilter(color, PorterDuff.Mode.SRC_ATOP));

        G2BeeHelper helper = G2BeeHelper.getInstance(mSelected.getContext());
        helper.deleteXBeeMessage(mAddress, mXBeeMessages.get(mSelectedPosition));

        mXBeeMessages.remove(mSelectedPosition.intValue());
        notifyItemRemoved(mSelectedPosition);
        mSelected = null;
        mSelectedPosition = null;
    }

    public static final class XBeeMessageViewHolder extends RecyclerView.ViewHolder {

        private ImageView mAvatarImage;
        private TextView mAvatarText;
        private ViewGroup mBubble;
        private TextView mMessage;
        private TextView mTime;

        private XBeeMessageViewHolder(View itemView) {
            super(itemView);
            mAvatarImage = (ImageView) itemView.findViewById(R.id.avatar_image);
            mAvatarText = (TextView) itemView.findViewById(R.id.avatar_text);
            mBubble = (ViewGroup) itemView.findViewById(R.id.message_bubble);
            mMessage = (TextView) itemView.findViewById(R.id.message);
            mTime = (TextView) itemView.findViewById(R.id.message_time);

            ((GradientDrawable) mAvatarImage.getBackground()).setColor(ContextCompat.getColor
                    (itemView.getContext(), R.color.indigo_900));
        }

    }

}
