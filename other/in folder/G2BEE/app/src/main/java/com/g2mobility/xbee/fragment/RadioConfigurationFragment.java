package com.g2mobility.xbee.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.g2mobility.xbee.G2BeeActivity;
import com.g2mobility.xbee.R;

/**
 * Abstract fragment for radio configuration.
 *
 * @author Hanyu Li
 */
public abstract class RadioConfigurationFragment extends Fragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_g2bee, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_read_radio_configurations:
                // Read selected XBee radio configurations
                ((G2BeeActivity) getActivity()).readRadioConfigurations();
                return true;
            case R.id.action_software_reset:
                // Do software reset on the selected XBee node
                ((G2BeeActivity) getActivity()).softwareReset();
                return true;
            case R.id.action_restore_defaults:
                // Restore the selected XBee node to factory defaults
                ((G2BeeActivity) getActivity()).restoreDefaults();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract void updateConfigurations();

}
