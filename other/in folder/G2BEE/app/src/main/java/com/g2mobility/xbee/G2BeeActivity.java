package com.g2mobility.xbee;


import android.animation.Animator;
import android.animation.ValueAnimator;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Parcelable;
import android.os.RemoteException;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.api.G2Bee;
import com.g2mobility.xbee.api.G2BeeDevice;
import com.g2mobility.xbee.api.listeners.IG2BeeDataReceiveListener;
import com.g2mobility.xbee.api.models.G2BeeMessage;
import com.g2mobility.xbee.data.G2BeeHelper;
import com.g2mobility.xbee.fragment.AddressingFragment;
import com.g2mobility.xbee.fragment.DiagnosticsFragment;
import com.g2mobility.xbee.fragment.HelpFeedbackFragment;
import com.g2mobility.xbee.fragment.IOSettingsFragment;
import com.g2mobility.xbee.fragment.MessagingFragment;
import com.g2mobility.xbee.fragment.NetworkingFragment;
import com.g2mobility.xbee.fragment.RFInterfacingFragment;
import com.g2mobility.xbee.fragment.RadioConfigurationFragment;
import com.g2mobility.xbee.fragment.SecurityFragment;
import com.g2mobility.xbee.fragment.SerialInterfacingFragment;
import com.g2mobility.xbee.fragment.SettingsFragment;
import com.g2mobility.xbee.fragment.SleepModesFragment;
import com.g2mobility.xbee.fragment.XBeeNodesFragment;
import com.g2mobility.xbee.recycler.RadioConfiguration;
import com.g2mobility.xbee.recycler.RecyclerXBeeMessage;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Main activity of G²Bee application.
 * <p/>
 * This activity uses the classic Material Design navigation drawer pattern. When a drawer item is
 * clicked, the activity will load the corresponding fragment and display it.
 * <p/>
 * To interact with the XBee module, the activity will bind to {@link G2BeeService} at start-up. The
 * XBee node target is selected in {@link XBeeNodesFragment} which allows to do a node discovery of
 * the current PAN and select a XBee node, local or remote.
 *
 * @author Hanyu Li
 */
public class G2BeeActivity extends AppCompatActivity implements NavigationView
        .OnNavigationItemSelectedListener {
    private static final String TAG = "G2Bee";

    public static final String ACTION_ON_RESPONSE
            = "com.g2mobility.XBEE_ON_RESPONSE";

    /**
     * Radio configurations cache.
     */
    private Map<String, RadioConfiguration> mRadioConfigurations = new HashMap<>();

    private static final long DRAWER_CLOSE_DELAY_MS = 250;

    private final Handler mDrawerActionHandler = new Handler();
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mItemId;

    /**
     * {@link IBinder} returned when bound to {@link G2BeeService}.
     */
    private G2Bee mG2Bee;
    private IG2BeeDataReceiveListener mListener = new IG2BeeDataReceiveListener.Stub() {
        @Override
        public void dataReceived(final G2BeeMessage message) throws RemoteException {
            mPool.submit(new Runnable() {
                @Override
                public void run() {
                    onMessage(message);
                }
            });
        }
    };

    private ExecutorService mPool = Executors.newSingleThreadExecutor();

    private boolean mIsShowingDialog = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_g2bee);

        int itemChecked = R.id.drawer_xbee_nodes;
        if (savedInstanceState != null
                && savedInstanceState.containsKey(getString(R.string.key_checked_item))) {
            // Load previously checked item
            itemChecked = savedInstanceState.getInt(getString(R.string.key_checked_item));
        }
        if (savedInstanceState != null
                && savedInstanceState.containsKey(getString(R.string.key_radio_conf))) {
            // Load saved radio configurations
            List<Parcelable> list = savedInstanceState
                    .getParcelableArrayList(getString(R.string.key_radio_conf));
            if (list != null) {
                for (Parcelable conf : list) {
                    RadioConfiguration radioConf = (RadioConfiguration) conf;
                    mRadioConfigurations.put(radioConf.getParameter(), radioConf);
                }
            }
        }

        // Replace action bar with support tool bar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbar.setTitleTextColor(ContextCompat.getColor(this, R.color.white_100));
        setSupportActionBar(toolbar);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);
        // select the correct nav menu item
        navigationView.getMenu().findItem(itemChecked).setChecked(true);

        // Initiates drawer layout and animateToggleToBackArrow
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, toolbar,
                R.string.open, R.string.close);
        mDrawerToggle.setDrawerIndicatorEnabled(true);
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();

        navigate(itemChecked);

        mG2Bee = new G2Bee(this, TAG);
        mG2Bee.addDataListener(mListener);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);

        savedInstanceState.putInt(getString(R.string.key_checked_item), mItemId);
        savedInstanceState.putParcelableArrayList(getString(R.string.key_radio_conf),
                new ArrayList<Parcelable>(mRadioConfigurations.values()));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        mItemId = menuItem.getItemId();
        menuItem.setChecked(true);
        mDrawerLayout.closeDrawers();
        mDrawerActionHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                navigate(mItemId);
            }
        }, DRAWER_CLOSE_DELAY_MS);
        return true;
    }

    private void navigate(int itemId) {
        FragmentManager manager = getFragmentManager();
        Fragment fragment;
        String title;
        switch (itemId) {
            case R.id.drawer_messaging:
                title = getString(R.string.drawer_messaging);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new MessagingFragment();
                }
                break;
            case R.id.drawer_networking:
                title = getString(R.string.drawer_networking);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new NetworkingFragment();
                }
                break;
            case R.id.drawer_addressing:
                title = getString(R.string.drawer_addressing);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new AddressingFragment();
                }
                break;
            case R.id.drawer_security:
                title = getString(R.string.drawer_security);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new SecurityFragment();
                }
                break;
            case R.id.drawer_rf_interfacing:
                title = getString(R.string.drawer_rf_interfacing);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new RFInterfacingFragment();
                }
                break;
            case R.id.drawer_sleep_modes:
                title = getString(R.string.drawer_sleep_modes);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new SleepModesFragment();
                }
                break;
            case R.id.drawer_serial_interfacing:
                title = getString(R.string.drawer_serial_interfacing);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new SerialInterfacingFragment();
                }
                break;
            case R.id.drawer_io_settings:
                title = getString(R.string.drawer_io_settings);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new IOSettingsFragment();
                }
                break;
            case R.id.drawer_diagnostics:
                title = getString(R.string.drawer_diagnostics);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new DiagnosticsFragment();
                }
                break;
            case R.id.drawer_settings:
                title = getString(R.string.drawer_settings);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new SettingsFragment();
                }
                break;
            case R.id.drawer_help_feedback:
                title = getString(R.string.drawer_help_feedback);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new HelpFeedbackFragment();
                }
                break;
            case R.id.drawer_xbee_nodes:
            default:
                title = getString(R.string.drawer_xbee_nodes);
                fragment = manager.findFragmentByTag(title);
                if (fragment == null) {
                    fragment = new XBeeNodesFragment();
                }
                break;
        }

        // Create a new fragment and specify the planet to show based on position
        // Insert the fragment by replacing any existing fragment
        FragmentManager fragmentManager = getFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment, title)
                .addToBackStack(title)
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

        setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.support.v7.appcompat.R.id.home) {
            return mDrawerToggle.onOptionsItemSelected(item);
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets a {@link RadioConfiguration} to local cache. This is called when reading radio
     * configurations.
     *
     * @param configuration the radio configuration read to set
     */
    public void setRadioConfiguration(RadioConfiguration configuration) {
        mRadioConfigurations.put(configuration.getParameter(), configuration);
    }

    /**
     * Reports the {@link RadioConfiguration} of an AT command previously read on the selected XBee
     * node.
     *
     * @param parameter the XBee device parameter
     * @return the {@link RadioConfiguration} associated. The value of the radio configuration is
     * null if it is never loaded.
     */
    public RadioConfiguration getRadioConfiguration(String parameter) {
        return mRadioConfigurations.get(parameter);
    }

    /**
     * Reads selected XBee node's radio configurations.
     */
    public void readRadioConfigurations() {
        // A flag to prevent multiple reading at the same time.
        if (mIsShowingDialog) {
            return;
        }

        lockScreenOrientation();
        mIsShowingDialog = true;
        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .content(R.string.reading)
                .cancelable(false)
                .progress(false, 100, false)
                .negativeText(R.string.button_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                    }
                })
                .autoDismiss(false)
                .build();

        // The reading process is in background
        final AsyncTask<Void, Integer, Boolean> task = new AsyncTask<Void, Integer, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                XBee64BitAddress address = null;

                // Get selected XBee node information from the XBeeNodesFragment,
                // which could be found by tag
                Fragment fragment = getFragmentManager().findFragmentByTag(getString(R.string
                        .drawer_xbee_nodes));
                if (fragment != null) {
                    address = ((XBeeNodesFragment) fragment).getSelectedAddress();
                }

                if (address == null) {
                    return false;
                }

                // Prepare AT commands to send
                String[] networking = getResources().getStringArray(R.array
                        .xbee_networking_commands);
                String[] addressing = getResources().getStringArray(R.array
                        .xbee_addressing_commands);
                String[] security = getResources().getStringArray(R.array
                        .xbee_security_commands);
                String[] rfInterfacing = getResources().getStringArray(R.array
                        .xbee_rf_interfacing_commands);
                String[] sleepModes = getResources().getStringArray(R.array
                        .xbee_sleep_modes_commands);
                String[] serialInterfacing = getResources().getStringArray(R.array
                        .xbee_serial_interfacing_commands);
                String[] ioSettings = getResources().getStringArray(R.array
                        .xbee_io_settings_commands);
                String[] diagnostics = getResources().getStringArray(R.array
                        .xbee_diagnostics_commands);

                String[] parameters = new String[networking.length + addressing.length +
                        security.length + rfInterfacing.length + sleepModes.length +
                        serialInterfacing.length + ioSettings.length + diagnostics.length];
                int position = 0;
                System.arraycopy(networking, 0, parameters, position, networking.length);
                position += networking.length;
                System.arraycopy(addressing, 0, parameters, position, addressing.length);
                position += addressing.length;
                System.arraycopy(security, 0, parameters, position, security.length);
                position += security.length;
                System.arraycopy(rfInterfacing, 0, parameters, position, rfInterfacing.length);
                position += rfInterfacing.length;
                System.arraycopy(sleepModes, 0, parameters, position, sleepModes.length);
                position += sleepModes.length;
                System.arraycopy(serialInterfacing, 0, parameters, position,
                        serialInterfacing.length);
                position += serialInterfacing.length;
                System.arraycopy(ioSettings, 0, parameters, position, ioSettings.length);
                position += ioSettings.length;
                System.arraycopy(diagnostics, 0, parameters, position, diagnostics.length);

                for (int i = 0; i < parameters.length; i++) {
                    if (isCancelled()) {
                        progressDialog.dismiss();
                        return true;
                    }
                    String parameter = parameters[i];
                    RadioConfiguration configuration = new RadioConfiguration(parameter);
                    try {
                        configuration.setValue(mG2Bee.getParameter(address, parameter));
                    } catch (XBeeException ignored) {
                    }
                    // Cache the reading results
                    mRadioConfigurations.put(parameter, configuration);
                    // Progress bar progress
                    publishProgress(100 * (i + 1) / parameters.length);
                }

                return true;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                progressDialog.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (!success) {
                    progressDialog.setContent(getString(R.string.cannot_read));
                    progressDialog.setActionButton(DialogAction.NEGATIVE,
                            R.string.button_ok);
                } else {
                    progressDialog.dismiss();
                }
                // Update radio configuration fragments with newly read values
                updateRadioConfigurationFragments();
            }
        };
        progressDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                // Don't forget to cancel the flag
                mIsShowingDialog = false;
                task.cancel(true);
                unlockScreenOrientation();
            }
        });
        progressDialog.show();
        task.execute();
    }

    /**
     * Writes parameter value to the selected XBee node.
     *
     * @param configuration {@link RadioConfiguration} to write
     */
    public void writeValue(final RadioConfiguration configuration) {
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                lockScreenOrientation();
            }

            @Override
            protected Boolean doInBackground(Void... params) {
                XBee64BitAddress address = null;
                Fragment fragment = getFragmentManager().findFragmentByTag(getString(R.string
                        .drawer_xbee_nodes));
                if (fragment != null) {
                    address = ((XBeeNodesFragment) fragment).getSelectedAddress();
                }
                if (address != null) {
                    try {
                        mG2Bee.setParameter(address, configuration.getParameter(), configuration
                                .getValue());
                        byte[] value = mG2Bee.getParameter(address, configuration.getParameter());
                        configuration.setValue(value);
                        mRadioConfigurations.put(configuration.getParameter(), configuration);
                        return true;
                    } catch (XBeeException e) {
                        Log.e(TAG, "Error occurred while setting parameter", e);
                    }
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    updateRadioConfigurationFragments();
                }
                unlockScreenOrientation();
            }
        }.execute();
    }

    /**
     * Updates all radio configuration fragments by calling
     * {@link RadioConfigurationFragment#updateConfigurations()}
     * abstract method. Each radio configuration fragment must implement this method to update the
     * interface with new values.
     */
    private void updateRadioConfigurationFragments() {
        List<String> fragmentTags = new ArrayList<>();
        fragmentTags.add(getString(R.string.drawer_networking));
        fragmentTags.add(getString(R.string.drawer_addressing));
        fragmentTags.add(getString(R.string.drawer_security));
        fragmentTags.add(getString(R.string.drawer_rf_interfacing));
        fragmentTags.add(getString(R.string.drawer_sleep_modes));
        fragmentTags.add(getString(R.string.drawer_serial_interfacing));
        fragmentTags.add(getString(R.string.drawer_io_settings));
        fragmentTags.add(getString(R.string.drawer_diagnostics));

        for (String tag : fragmentTags) {
            Fragment fragment = getFragmentManager().findFragmentByTag(tag);
            if (fragment != null && fragment instanceof
                    RadioConfigurationFragment) {
                ((RadioConfigurationFragment) fragment).updateConfigurations();
            }
        }
    }

    /**
     * Do a software reset on selected XBee node. A dialog will be showed to confirm the action.
     */
    public void softwareReset() {
        // A flag to prevent multiple reading at the same time.
        if (mIsShowingDialog) {
            return;
        }

        lockScreenOrientation();
        mIsShowingDialog = true;
        final MaterialDialog progressDialog = new MaterialDialog.Builder(this)
                .content(R.string.software_resetting)
                .cancelable(false)
                .progress(true, 0)
                .build();

        final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                XBee64BitAddress address = null;

                // Get selected XBee node information from the XBeeNodesFragment,
                // which could be found by tag
                Fragment fragment = getFragmentManager().findFragmentByTag(getString(R
                        .string.drawer_xbee_nodes));
                if (fragment != null) {
                    // The first node is always the local one
                    address = ((XBeeNodesFragment) fragment).getSelectedAddress();
                }
                if (address == null) {
                    return false;
                }
                try {
                    mG2Bee.executeParameter(address, "FR");
                    Thread.sleep(2000);
                    return true;
                } catch (XBeeException | InterruptedException e) {
                    Log.e(TAG, "Software reset failed", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                MaterialDialog doneDialog = new MaterialDialog.Builder(G2BeeActivity.this)
                        .title(R.string.action_software_reset)
                        .content(success ? R.string.software_reset_success :
                                R.string.software_reset_failed)
                        .positiveText(R.string.button_ok)
                        .build();
                doneDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        unlockScreenOrientation();
                    }
                });
                doneDialog.show();
            }
        };

        new MaterialDialog.Builder(this)
                .title(R.string.action_software_reset)
                .content(R.string.software_reset_confirm)
                .positiveText(R.string.button_reset)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        progressDialog.show();
                        task.execute();
                    }
                })
                .negativeText(R.string.button_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        materialDialog.dismiss();
                        unlockScreenOrientation();
                    }
                })
                .autoDismiss(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        unlockScreenOrientation();
                    }
                })
                .show();
    }

    /**
     * Restore selected XBee to factory defaults. Show a dialog to confirm.
     */
    public void restoreDefaults() {
        // A flag to prevent multiple reading at the same time.
        if (mIsShowingDialog) {
            return;
        }

        lockScreenOrientation();
        mIsShowingDialog = true;
        final MaterialDialog progressDialog = new MaterialDialog
                .Builder(G2BeeActivity.this)
                .content(R.string.restoring_defaults)
                .cancelable(false)
                .progress(true, 0)
                .build();

        final AsyncTask<Void, Void, Boolean> task = new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... params) {
                XBee64BitAddress address = null;

                // Get selected XBee node information from the XBeeNodesFragment,
                // which could be found by tag
                Fragment fragment = getFragmentManager().findFragmentByTag(getString(
                        R.string.drawer_xbee_nodes));
                if (fragment != null) {
                    // The first node is always the local one
                    address = ((XBeeNodesFragment) fragment).getSelectedAddress();
                }
                if (address == null) {
                    return false;
                }
                try {
                    mG2Bee.executeParameter(address, "RE");
                    Thread.sleep(2000);
                    return true;
                } catch (XBeeException | InterruptedException e) {
                    Log.e(TAG, "Restore defaults failed", e);
                }
                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {
                progressDialog.dismiss();
                MaterialDialog doneDialog = new MaterialDialog.Builder(G2BeeActivity.this)
                        .title(R.string.action_restore_defaults)
                        .content(success ? R.string.restore_defaults_success :
                                R.string.restore_defaults_failed)
                        .positiveText(R.string.button_ok)
                        .build();
                doneDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        unlockScreenOrientation();
                    }
                });
                doneDialog.show();
            }
        };
        new MaterialDialog.Builder(this)
                .title(R.string.action_restore_defaults)
                .content(R.string.restore_defaults_confirm)
                .positiveText(R.string.button_restore)
                .onPositive(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        materialDialog.dismiss();
                        progressDialog.show();
                        task.execute();
                    }
                })
                .negativeText(R.string.button_cancel)
                .onNegative(new MaterialDialog.SingleButtonCallback() {
                    @Override
                    public void onClick(@NonNull MaterialDialog materialDialog,
                            @NonNull DialogAction dialogAction) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        materialDialog.dismiss();
                        unlockScreenOrientation();
                    }
                })
                .autoDismiss(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        // Don't forget to cancel the flag
                        mIsShowingDialog = false;
                        unlockScreenOrientation();
                    }
                })
                .show();
    }

    public void animateToggleToBackArrow(final View.OnClickListener listener) {
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        mDrawerToggle.setToolbarNavigationClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
                mDrawerToggle.setDrawerIndicatorEnabled(true);

                ValueAnimator animator = ValueAnimator.ofFloat(1, 0);
                animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                    @Override
                    public void onAnimationUpdate(ValueAnimator animation) {
                        mDrawerToggle.onDrawerSlide(mDrawerLayout,
                                (Float) animation.getAnimatedValue());
                    }

                });
                animator.setDuration(300);
                animator.start();
                listener.onClick(v);
            }

        });
        ValueAnimator animator = ValueAnimator.ofFloat(0, 1);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mDrawerToggle.onDrawerSlide(mDrawerLayout, (float) animation.getAnimatedValue());
            }

        });
        animator.addListener(new Animator.AnimatorListener() {

            @Override
            public void onAnimationStart(Animator animation) {
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                mDrawerToggle.setHomeAsUpIndicator(R.drawable.ic_arrow_back);
                mDrawerToggle.setDrawerIndicatorEnabled(false);
            }

            @Override
            public void onAnimationCancel(Animator animation) {
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
            }

        });
        animator.setDuration(300);
        animator.start();
    }

    public void sendMessage(String address, byte[] input) {
        try {
            mG2Bee.sendDataAsync(new XBee64BitAddress(address), input);
        } catch (XBeeException e) {
            Log.e(TAG, "Cannot send request", e);
        }
    }

    private void onMessage(G2BeeMessage message) {
        String remoteAddress = HexUtils.byteArrayToHexString(message.getDevice()
                .getXbee64BitAddress().getValue());
        String remoteType = "?";

        RecyclerXBeeMessage xbeeMessage = new RecyclerXBeeMessage();
        xbeeMessage.setMessage(message.getData());
        xbeeMessage.setTime(new Date());
        xbeeMessage.setIsOutgoing(false);

        FragmentManager fragmentManager = getFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(
                getString(R.string.drawer_xbee_nodes));
        if (fragment != null) {
            List<G2BeeDevice> devices = ((XBeeNodesFragment) fragment).getXBeeDevices();
            for (G2BeeDevice device : devices) {
                if (device.getXbee64BitAddress().equals(message.getDevice().getXbee64BitAddress())
                        && device.getFirmwareVersion() != null) {
                    switch (device.getFirmwareVersion()) {
                        case "21A7":
                            remoteType = "C";
                            break;
                        case "23A7":
                        case "4059":
                            remoteType = "R";
                            break;
                        default:
                            remoteType = "E";
                            break;
                    }
                    break;
                }
            }
        }
        if (remoteType.equals("?")) {
            remoteType = G2BeeHelper.getInstance(this).getXBeeNodeType(remoteAddress);
        }

        G2BeeHelper.getInstance(this).saveMessage(remoteAddress, remoteType, xbeeMessage
                        .getMessage(),
                xbeeMessage.getTime(), xbeeMessage.isOutgoing());
        sendBroadcast(new Intent(ACTION_ON_RESPONSE));
    }

    public void lockScreenOrientation() {
        if (android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            int currentOrientation = getResources().getConfiguration().orientation;
            if (currentOrientation == Configuration.ORIENTATION_PORTRAIT) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            } else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
        }
    }

    public void unlockScreenOrientation() {
        if (android.provider.Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) == 1) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        }
    }

}
