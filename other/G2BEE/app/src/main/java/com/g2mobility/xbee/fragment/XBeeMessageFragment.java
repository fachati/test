package com.g2mobility.xbee.fragment;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.digi.xbee.api.utils.ByteUtils;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.data.G2BeeHelper;
import com.g2mobility.xbee.recycler.RecyclerXBeeMessage;
import com.g2mobility.xbee.recycler.XBeeMessageAdapter;
import com.g2mobility.xbee.recycler.XBeeNodeItemDecoration;

import java.util.Date;
import java.util.List;

/**
 * Fragment for XBee messaging.
 *
 * @author Hanyu Li
 */
public class XBeeMessageFragment extends Fragment implements View.OnClickListener,
        CompoundButton.OnCheckedChangeListener {

    private RecyclerView mRecyclerView;
    private XBeeMessageAdapter mAdapter;

    private G2BeeHelper mHelper;

    private EditText mEditText;

    private SwitchCompat mSwitch;
    private TextView mTextViewAsciiOnOff;

    private int mOriginalHeightDiff;
    private boolean mIsSoftKeyboardShowed;

    private String mSelectedNodeType = "?";

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }

    };

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

    private InputFilter mHexFilter = new InputFilter() {

        @Override
        public CharSequence filter(CharSequence source, int start, int end, Spanned dest,
                int dstart, int dend) {
            char[] acceptedChars = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9'
                    , 'A', 'B', 'C', 'D', 'E', 'F', ' '};

            for (int index = start; index < end; index++) {
                if (!new String(acceptedChars).contains(String.valueOf(source.charAt(index)))) {
                    return "";
                }
            }
            return null;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Fragment fragment = getActivity().getFragmentManager().findFragmentByTag(
                getString(R.string.drawer_xbee_nodes));
        if (fragment != null) {
            int selectedPosition = ((XBeeNodesFragment) fragment).getSelectedPosition();
            G2BeeDevice device = ((XBeeNodesFragment) fragment).getXBeeDevices().get
                    (selectedPosition);
            if (device != null && device.getFirmwareVersion() != null) {
                switch (device.getFirmwareVersion()) {
                    case "21A7":
                        mSelectedNodeType = "C";
                        break;
                    case "23A7":
                    case "4059":
                        mSelectedNodeType = "R";
                        break;
                    default:
                        mSelectedNodeType = "E";
                        break;
                }
            }
            mSelectedNodeType = "C";
        }

        mHelper = G2BeeHelper.getInstance(getActivity());
        mAdapter = new XBeeMessageAdapter(getArguments().getString(
                getString(R.string.key_xbee_address)), mSelectedNodeType,
                getArguments().getString(getString(R.string.key_xbee_type)));

        getActivity().registerReceiver(mReceiver, new IntentFilter(G2BeeActivity
                .ACTION_ON_RESPONSE));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        final View view = inflater.inflate(R.layout.fragment_xbee_message, container, false);

        String xbeeAddress = getArguments().getString(getString(R.string.key_xbee_address));

        final ActionBar actionBar = ((G2BeeActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setCustomView(R.layout.toolbar_xbee_message);
            actionBar.setDisplayShowCustomEnabled(true);

            if (xbeeAddress != null) {
                TextView title = (TextView) actionBar.getCustomView()
                        .findViewById(android.R.id.title);
                title.setText(xbeeAddress);
            }
        }

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recycler_xbee_message);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        llm.setReverseLayout(true);

        mRecyclerView.addItemDecoration(new XBeeNodeItemDecoration());
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.setLayoutManager(llm);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.addOnScrollListener(mOnScrollListener);
        mRecyclerView.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mEditText.clearFocus();
                return mAdapter.cancelActionMode();
            }

        });

        mEditText = (EditText) view.findViewById(R.id.edit_text_message);

        ImageButton buttonSend = (ImageButton) view.findViewById(R.id.button_send);
        buttonSend.setOnClickListener(this);

        // Listen to soft keyboard show/hide event
        view.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {

                    @Override
                    public void onGlobalLayout() {
                        int diff = view.getRootView().getHeight() - view.getHeight();
                        if (mOriginalHeightDiff == 0) {
                            mOriginalHeightDiff = diff;
                        }
                        // Soft keyboard showed, scroll the view by diff
                        if (diff > mOriginalHeightDiff) {
                            mRecyclerView.smoothScrollBy(0, diff - mOriginalHeightDiff);
                            if (!mIsSoftKeyboardShowed) {
                                mAdapter.cancelActionMode();
                            }
                            mIsSoftKeyboardShowed = true;
                        } else {
                            mIsSoftKeyboardShowed = false;
                        }
                    }

                });
        mRecyclerView.scrollToPosition(0);

        if (actionBar != null) {
            mTextViewAsciiOnOff = (TextView) actionBar.getCustomView()
                    .findViewById(R.id.label_ascii_on_off);
            mSwitch = (SwitchCompat) actionBar.getCustomView().findViewById(R.id.switch_display);
            mSwitch.setOnCheckedChangeListener(this);
            mSwitch.setChecked(false);
        }

        enableHexDisplay(true);

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

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putString(getString(R.string.key_xbee_address),
                getArguments().getString(getString(R.string.key_xbee_address)));
        savedInstanceState.putString(getString(R.string.key_xbee_type),
                getArguments().getString(getString(R.string.key_xbee_type)));
    }

    @Override
    public void onResume() {
        super.onResume();

        List<RecyclerXBeeMessage> messages = mHelper.getMessages(getArguments().getString(
                getString(R.string.key_xbee_address)));
        for (RecyclerXBeeMessage message : messages) {
            mAdapter.addMessage(message);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void enableHexDisplay(boolean enabled) {
        if (enabled) {
            mEditText.setFilters(new InputFilter[]{mHexFilter});
            mEditText.setInputType(InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        } else {
            mEditText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            mEditText.setFilters(new InputFilter[]{});
        }
        mAdapter.enableHexDisplay(enabled);
    }

    @Override
    public void onClick(View v) {
        mAdapter.cancelActionMode();
        byte[] input;

        String inputString = mEditText.getText().toString();
        if (!mSwitch.isChecked()) {
            inputString = inputString.replaceAll("\\s+", "");
            input = HexUtils.hexStringToByteArray(inputString);
        } else {
            input = ByteUtils.stringToByteArray(inputString);
        }
        Log.e("TEST", "SENDING " + HexUtils.prettyHexString(input));

        if (input != null && input.length > 0) {
            mEditText.setText("");

            sendMessage(input);
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            enableHexDisplay(false);
            mTextViewAsciiOnOff.setText(R.string.label_ascii_on);
            mEditText.setHint(R.string.hint_ascii);
        } else {
            enableHexDisplay(true);
            mTextViewAsciiOnOff.setText(R.string.label_ascii_off);
            mEditText.setHint(R.string.hint_hex);
        }
    }

    private void hideSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity()
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(mEditText.getWindowToken(), 0);
    }

    private void sendMessage(byte[] input) {
        ((G2BeeActivity) getActivity()).sendMessage(getArguments().getString(
                getString(R.string.key_xbee_address)), input);

        RecyclerXBeeMessage message = new RecyclerXBeeMessage();
        message.setIsOutgoing(true);
        message.setMessage(input);
        message.setTime(new Date());

        mHelper.saveMessage(getArguments().getString(getString(R.string.key_xbee_address)),
                mSelectedNodeType, input, message.getTime(), true);

        mAdapter.addMessage(message);
    }

}
