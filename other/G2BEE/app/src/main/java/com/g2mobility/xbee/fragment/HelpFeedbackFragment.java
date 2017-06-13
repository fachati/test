package com.g2mobility.xbee.fragment;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.style.ImageSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import com.g2mobility.xbee.R;

/**
 * Fragment for help and feedback.
 *
 * @author Hanyu Li
 */
public class HelpFeedbackFragment extends Fragment implements View.OnClickListener {

    private ViewGroup groupXBeeNodes;
    private ViewGroup groupNodeDiscovery;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_help_feedback, container, false);
        ViewGroup list = (ViewGroup) view.findViewById(R.id.help_list);
        groupXBeeNodes = (ViewGroup) inflater.inflate(R.layout.help_xbee_nodes, list, false);
        list.addView(groupXBeeNodes, 1);

        TextView xbeeNodes = (TextView) view.findViewById(R.id.xbee_nodes);
        xbeeNodes.setOnClickListener(this);

        groupNodeDiscovery = (ViewGroup) inflater.inflate(R.layout.help_node_discovery, list,
                false);
        list.addView(groupNodeDiscovery, 3);

        TextView nodeDiscovery = (TextView) view.findViewById(R.id.node_discovery);
        nodeDiscovery.setOnClickListener(this);

        ((TextView) view.findViewById(R.id.what_is_xbee_1)).setMovementMethod(
                LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.what_is_xbee_2)).setMovementMethod(
                LinkMovementMethod.getInstance());
        ((TextView) view.findViewById(R.id.device_addressing_3)).setMovementMethod(
                LinkMovementMethod.getInstance());

        String howToDiscover2Text = getString(R.string.how_to_discover_2);
        TextView howToDiscover2 = (TextView) view.findViewById(R.id.how_to_discover_2);
        SpannableString ss = new SpannableString(howToDiscover2Text);

        Drawable icon = ContextCompat.getDrawable(getActivity(), R.drawable.ic_xbee_nodes);
        icon.setBounds(0, 0, icon.getIntrinsicWidth(), icon.getIntrinsicHeight());
        ImageSpan spanIcon = new ImageSpan(icon, ImageSpan.ALIGN_BASELINE);
        ss.setSpan(spanIcon, ss.toString().indexOf("[icon]"), ss.toString().indexOf("[icon]") + 6,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
        ImageSpan spanFab = new ImageSpan(getActivity(), R.drawable.fab_help);
        ss.setSpan(spanFab, ss.toString().indexOf("[fab]"), ss.toString().indexOf("[fab]") + 5,
                Spannable.SPAN_INCLUSIVE_EXCLUSIVE);

        howToDiscover2.setText(ss);

        return view;
    }

    @Override
    public void onClick(View v) {
        View viewGroup = groupXBeeNodes;
        switch (v.getId()) {
            case R.id.xbee_nodes:
                viewGroup = groupXBeeNodes;
                break;
            case R.id.node_discovery:
                viewGroup = groupNodeDiscovery;
                break;
        }
        final View target = viewGroup;
        if (target.getVisibility() == View.VISIBLE) {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.expand_less);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    target.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            target.startAnimation(anim);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable
                    (getActivity(), R.drawable.ic_expand_more), null, null, null);
        } else {
            Animation anim = AnimationUtils.loadAnimation(getActivity(), R.anim.expand_more);
            anim.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {
                    target.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationEnd(Animation animation) {
                }

                @Override
                public void onAnimationRepeat(Animation animation) {
                }
            });
            target.startAnimation(anim);
            ((TextView) v).setCompoundDrawablesWithIntrinsicBounds(ContextCompat.getDrawable
                    (getActivity(), R.drawable.ic_expand_less), null, null, null);
        }
    }

}
