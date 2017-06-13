package com.g2mobility.xbee.fragment;

import android.animation.Animator;
import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.recycler.XBeeMessageToHintAdapter;

import java.util.List;
import java.util.Locale;


public class MessageToFragment extends Fragment implements TextWatcher, View.OnClickListener {

    private RecyclerView mRecyclerView;
    private XBeeMessageToHintAdapter mAdapter;

    private EditText mEditText;
    private ImageButton mForwardButton;

    private int mOriginalHeightDiff;
    private boolean mIsSoftKeyboardShowed;

    private RecyclerView.OnScrollListener mOnScrollListener = new RecyclerView.OnScrollListener() {

        @Override
        public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
            if (mIsSoftKeyboardShowed
                    && newState != RecyclerView.SCROLL_STATE_IDLE
                    && !mEditText.isFocused()) {
                hideSoftKeyboard();
            }
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mAdapter = new XBeeMessageToHintAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.fragment_message_to, container, false);

        final ActionBar actionBar = ((G2BeeActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.toolbar_message_to);
            actionBar.setDisplayShowCustomEnabled(true);
            mForwardButton = (ImageButton) actionBar.getCustomView().findViewById(R.id
                    .button_forward);
            mForwardButton.setOnClickListener(this);
            mEditText = ((EditText) actionBar.getCustomView().findViewById(R.id
                    .edit_text_message_to));
        }

        mRecyclerView = (RecyclerView) v.findViewById(R.id.recycler_list_xbee);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.clearFocus();
                if (mIsSoftKeyboardShowed) {
                    hideSoftKeyboard();
                }
                return false;
            }

        });

        // Listen to soft keyboard show/hide event
        v.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        int diff = v.getRootView().getHeight() - v.getHeight();
                        if (mOriginalHeightDiff == 0) {
                            mOriginalHeightDiff = diff;
                        }
                        // Soft keyboard showed, scroll the view by diff
                        if (diff > mOriginalHeightDiff) {
                            mRecyclerView.smoothScrollBy(0, diff - mOriginalHeightDiff);
                            mIsSoftKeyboardShowed = true;
                        } else {
                            mIsSoftKeyboardShowed = false;
                        }
                    }

                });

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(getString(R.string
                .drawer_xbee_nodes));
        if (fragment != null) {
            List<G2BeeDevice> devices = ((XBeeNodesFragment) fragment).getXBeeDevices();
            for (G2BeeDevice device : devices) {
                mAdapter.addXBeeDevice(device);
            }
        }

        ((G2BeeActivity) getActivity()).animateToggleToBackArrow(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                hideSoftKeyboard();
                if (actionBar != null) {
                    actionBar.setDisplayShowCustomEnabled(false);
                }

                String title = getString(R.string.drawer_messaging);
                Fragment fragment = getFragmentManager().findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new MessagingFragment();
                }
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, fragment, title)
                        .addToBackStack(title)
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .commit();
            }

        });


        return v;
    }

    @Override
    public void onResume() {
        super.onResume();

        mEditText.setText("");
        mEditText.requestFocus();
        mEditText.addTextChangedListener(this);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() == 16) {
            showForwardButton();
        } else {
            hideForwardButton();
        }
    }

    private void showForwardButton() {
        if (mForwardButton.getVisibility() != View.VISIBLE) {
            int cx = (mForwardButton.getLeft() + mForwardButton.getRight()) / 2;
            int cy = (mForwardButton.getTop() + mForwardButton.getBottom()) / 2;
            int radius = Math.max(mForwardButton.getWidth(), mForwardButton.getHeight());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Animator anim = ViewAnimationUtils.createCircularReveal(mForwardButton, cx, cy, 0,
                        radius);
                mForwardButton.setVisibility(View.VISIBLE);
                anim.start();
            } else {
                mForwardButton.setVisibility(View.VISIBLE);
            }
        }
    }

    private void hideForwardButton() {
        if (mForwardButton.getVisibility() == View.VISIBLE) {
            int cx = (mForwardButton.getLeft() + mForwardButton.getRight()) / 2;
            int cy = (mForwardButton.getTop() + mForwardButton.getBottom()) / 2;
            int radius = Math.max(mForwardButton.getWidth(), mForwardButton.getHeight());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Animator anim = ViewAnimationUtils.createCircularReveal(mForwardButton, cx, cy,
                        radius, 0);
                mForwardButton.setVisibility(View.VISIBLE);
                anim.addListener(new Animator.AnimatorListener() {

                    @Override
                    public void onAnimationStart(Animator animation) {
                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {
                        mForwardButton.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {
                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {
                    }

                });
                anim.start();
            } else {
                mForwardButton.setVisibility(View.INVISIBLE);
            }

        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    @Override
    public void onClick(View v) {
        String address = mEditText.getText().toString().toUpperCase(Locale.getDefault());
        if (address.length() == 16) {
            hideSoftKeyboard();
            String xbeeType = mAdapter.getXBeeNodeType(address);

            Bundle data = new Bundle();
            data.putString(v.getResources().getString(R.string.key_xbee_address), address);
            data.putString(v.getResources().getString(R.string.key_xbee_type), xbeeType);

            Fragment fragment = new XBeeMessageFragment();
            fragment.setArguments(data);
            FragmentManager fragmentManager = ((Activity) v.getContext()).
                    getFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.content_frame, fragment)
                    .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                    .commit();
        }
    }

}
