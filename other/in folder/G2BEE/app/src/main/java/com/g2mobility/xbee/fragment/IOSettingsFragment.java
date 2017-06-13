package com.g2mobility.xbee.fragment;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;
import com.g2mobility.xbee.recycler.RadioConfiguration;
import com.g2mobility.xbee.recycler.RadioConfigurationAdapter;
import com.g2mobility.xbee.recycler.RadioConfigurationItemDecoration;

/**
 * Fragment for I/O settings radio configuration.
 *
 * @author Hanyu Li
 */
public class IOSettingsFragment extends RadioConfigurationFragment {

    private RadioConfigurationAdapter mAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mAdapter = new RadioConfigurationAdapter(
                R.array.xbee_io_settings_command_names,
                R.array.xbee_io_settings_descriptions,
                R.array.xbee_io_settings_input_types,
                R.array.xbee_io_settings_ranges,
                ContextCompat.getColor(getActivity(), R.color.red_600));
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_radio_configuration, container, false);

        RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id
                .recycler_radio_configuration);
        LinearLayoutManager llm = new LinearLayoutManager(getActivity());
        llm.setOrientation(LinearLayoutManager.VERTICAL);

        String[] commands = getResources().getStringArray(R.array.xbee_io_settings_commands);

        recyclerView.addItemDecoration(new RadioConfigurationItemDecoration(commands.length));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(llm);
        recyclerView.setAdapter(mAdapter);

        for (String command : commands) {
            RadioConfiguration conf = ((G2BeeActivity) getActivity())
                    .getRadioConfiguration(command);
            if (conf == null) {
                conf = new RadioConfiguration(command);
            }
            mAdapter.setConfiguration(conf);
        }

        return view;
    }

    @Override
    public void updateConfigurations() {
        String[] commands = getResources().getStringArray(R.array.xbee_io_settings_commands);
        for (String command : commands) {
            RadioConfiguration conf = ((G2BeeActivity) getActivity())
                    .getRadioConfiguration(command);
            if (conf == null) {
                conf = new RadioConfiguration(command);
            }
            mAdapter.setConfiguration(conf);
        }
    }

}
