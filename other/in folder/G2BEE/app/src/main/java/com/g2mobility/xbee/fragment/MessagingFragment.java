package com.g2mobility.xbee.fragment;

import android.annotation.TargetApi;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.data.G2BeeHelper;
import com.g2mobility.xbee.recycler.XBeeConversation;
import com.g2mobility.xbee.recycler.XBeeConversationAdapter;

import java.util.List;

/**
 * Fragment for XBee messaging.
 *
 * @author Hanyu Li
 */
public class MessagingFragment extends Fragment implements View.OnClickListener {

    private XBeeConversationAdapter mAdapter;

    private G2BeeHelper mHelper;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            onResume();
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);

        mAdapter = new XBeeConversationAdapter();
        mHelper = G2BeeHelper.getInstance(getActivity());
        getActivity().registerReceiver(mReceiver, new IntentFilter(G2BeeActivity
                .ACTION_ON_RESPONSE));
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);

        View view = inflater.inflate(R.layout.fragment_messaging, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_messaging);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);

        recyclerView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });

        FloatingActionButton fabAdd = (FloatingActionButton) view.findViewById(R.id.fab_add);
        fabAdd.setOnClickListener(this);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        List<XBeeConversation> conversations = mHelper.getConversations();
        for (XBeeConversation conv : conversations) {
            mAdapter.addConversation(conv);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.fab_add:
                MessageToFragment messageToFragment = new MessageToFragment();
                FragmentManager fragmentManager = getFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.content_frame, messageToFragment, "messageTo")
                        .addToBackStack("messageTo")
                        .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
                        .commit();
                break;
        }
    }

}
