package com.g2mobility.status;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.g2mobility.status.helper.LocaleHelper;
import com.g2mobility.status.tab.AutoResizeTextView;
import com.g2mobility.status.tab.NavigationTabBar;

import java.util.ArrayList;


public class WelcomeActivity extends Activity {

    Button buttonLeft, buttonRight, buttonCenter;


    protected PowerManager.WakeLock mWakeLock;

    private GlobalClass mGlobal = null;

    private NavigationTabBar mNavigationTabBar;

    private LinearLayout mLayoutCenter, mLayoutPC;

    private Status[] mCPStatus;             //tableau de borne[gauche , droite]

    private AutoResizeTextView textStatusB1, textStatusB2;   // textview dans la page de centre (status de chaque borne)

    private TextView textStatus, textBorneDirection, textCommentaire;
    private TextView textTitle, textInstruction, textLeft, textRight;
    private ImageView imageArrowG, imageArrowD;
    private LinearLayout layoutProgress;

    private WebView webView;                                // webview pour afficher la progress bar
    int ecran;                                              // -1 : page central , 0 : gauche , 1 : droite
    int indexTab;
    String languageCode;

    EvseStatusEnum evseLeft, evseRight;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public void MAJInformation2(Intent intent) {

        XBee64BitAddress rAddress = hexStringToXBeeAddress(intent.getStringExtra("address"));
        int indexTab = getIndexFromXbeeAddress(rAddress);
        int status = intent.getIntExtra("UIStatus", -1);         // status de la borne

        if (status != -1) {
            if (indexTab == 0) {
                evseLeft = EvseStatusEnum.getEvseStatusEnumFromId(status);
            } else if (indexTab == 1) {
                evseRight = EvseStatusEnum.getEvseStatusEnumFromId(status);
            }
        }

    }

    public void MAJTab2(int indexTab) {
        Log.e("information", indexTab + "--");
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        if (indexTab == 1) {

            imageArrowD.setVisibility(View.INVISIBLE);
            imageArrowG.setVisibility(View.VISIBLE);
            textBorneDirection.setText(resources.getString(R.string.TEXT_POISITION_LEFT));
            MAJTabInformation(1);

        } else if (indexTab == 2) {
            imageArrowD.setVisibility(View.VISIBLE);
            imageArrowG.setVisibility(View.INVISIBLE);
            textBorneDirection.setText(resources.getString(R.string.TEXT_POISITION_RIGHT));
            MAJTabInformation(2);
        }
    }

    public void MAJInfoCenter2(int indexTab) {
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        if (indexTab == 1) {
            textStatusB1.setTextColor(getResources().getColor(R.color.white));
            if (evseLeft != null) {
                if (evseLeft.isAvailable()) {
                    textStatusB1.setText(resources.getString(R.string.STATUS_LIBRE));
                    textStatusB1.setBackground(getResources().getDrawable(R.drawable.bordergreen));
                } else if (evseLeft.isOccupied()) {
                    textStatusB1.setText(resources.getString(R.string.STATUS_OCCUPE));
                    textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderbleu));
                } else if (evseLeft.isError()) {
                    textStatusB1.setText(resources.getString(R.string.STATUS_PANNE));
                    textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderred));
                } else if (evseLeft.isOut()) {
                    textStatusB1.setText(resources.getString(R.string.STATUS_HORS_SERVICE));
                    textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderpink));
                }
            }

        } else if (indexTab == 2) {
            textStatusB2.setTextColor(getResources().getColor(R.color.white));
            if (evseRight != null) {
                if (evseRight.isAvailable()) {
                    textStatusB2.setText(resources.getString(R.string.STATUS_LIBRE));
                    textStatusB2.setBackground(getResources().getDrawable(R.drawable.bordergreen));
                } else if (evseRight.isOccupied()) {
                    textStatusB2.setText(resources.getString(R.string.STATUS_OCCUPE));
                    textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderbleu));
                } else if (evseRight.isError()) {
                    textStatusB2.setText(resources.getString(R.string.STATUS_PANNE));
                    textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderred));
                } else if (evseRight.isOut()) {
                    textStatusB2.setText(resources.getString(R.string.STATUS_HORS_SERVICE));
                    textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderpink));
                }
            }
        }
    }

    public void MAJTabInformation(int indexTab) {
        Log.e("information", indexTab + "--");
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        if (indexTab == 1 && ecran == 1) {
            if (evseLeft != null) {
                textCommentaire.setText(resources.getString(evseLeft.getTextL1()) + "\n" + resources.getString(evseLeft.getTextL2()));
                textStatus.setBackground(getResources().getDrawable(evseLeft.getColor().getCode()));
                textStatus.setText(resources.getString(evseLeft.getTextStatus()));
                showProgress(evseLeft.getId());
            }

        } else if (indexTab == 2 && ecran == 2) {
            if (evseRight != null) {
                textCommentaire.setText(resources.getString(evseRight.getTextL1()) + "\n" + resources.getString(evseRight.getTextL2()));
                textStatus.setBackground(getResources().getDrawable(evseRight.getColor().getCode()));
                textStatus.setText(resources.getString(evseRight.getTextStatus()));
                showProgress(evseRight.getId());
            }
        }
    }

    public void showProgress(int id) {
        if (id == 46 || id == 40 || id == 30 || id == 31) {
            if (layoutProgress.getVisibility() == View.INVISIBLE)
                layoutProgress.setVisibility(View.VISIBLE);
        } else {
            layoutProgress.setVisibility(View.INVISIBLE);
        }
    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(g2kService.ACTION_SEND_STATUS)) {
                Log.e("receivedInformation", "CMD : " + intent.getStringExtra("cmd") + "\nTAG : " + intent.getStringExtra("tag") + "\nStatus : " + G2EvseStatus.fromCode(intent.getIntExtra("status", -1)) +
                        "\next_status : " + getEXT_STATUS(intent.getIntExtra("ext_status", -1)) + "\nerror_status : " + intent.getIntExtra("error_status", -1) + "\nsessionID : " + intent.getIntExtra("sessionID", -1));

                XBee64BitAddress rAddress = hexStringToXBeeAddress(intent.getStringExtra("address"));
                indexTab = getIndexFromXbeeAddress(rAddress);
                MAJInformation2(intent);
                MAJTabInformation(indexTab + 1);
                MAJInfoCenter2(indexTab + 1);
                ((TextView) findViewById(R.id.textViewStatus)).setText(intent.getIntExtra("UIStatus", -1) + "");
            }
        }
    };


    public XBee64BitAddress hexStringToXBeeAddress(String s) {
        String address = s.replace("0x", "").replace(",", " ");
        return new XBee64BitAddress(address);
    }


    public int getIndexFromXbeeAddress(XBee64BitAddress address64) {
        for (int i = 0; i < mGlobal.getEvseList().length; i++) {
            if (mGlobal.getEvseList()[i].getAddress().equals(address64))
                return i;
        }
        return -1;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        if (Build.VERSION.SDK_INT > 9 && getActionBar() != null) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ActionBar actionBar = getActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(0, 153, 204)));
        }
        verifyStoragePermissions(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();


        if (Build.VERSION.SDK_INT > 20 && this.getActionBar() != null) {
            WelcomeActivity.this.getActionBar().setElevation(0);
            WelcomeActivity.this.getActionBar().setTitle("");
        }

        mGlobal = (GlobalClass) getApplicationContext();

        Intent intent = new Intent(this, g2kService.class);
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(g2kService.ACTION_SEND_STATUS);
        filter.addAction(g2kService.ACTION_START);
        registerReceiver(mReceiver, filter);


        languageCode = "fr";
        ecran = -1;
        mCPStatus = new Status[mGlobal.getEvseList().length]; //initialisation du tab de status

        for (int i = 0; i < mGlobal.getEvseList().length; i++) {
            mCPStatus[i] = new Status(mGlobal.getEvseList()[i].getmBorne());
            Log.e("borne " + i, mGlobal.getEvseList()[i].getmBorne());
        }


        initUI();

        Intent testBlink = new Intent(this, g2kService.class);
        testBlink.setAction(g2kService.ACTION_START);
        startService(testBlink);
    }


    private void initUI() {

        buttonLeft = (Button) findViewById(R.id.buttonLeft);
        buttonRight = (Button) findViewById(R.id.buttonRight);
        buttonCenter = (Button) findViewById(R.id.buttonCenter);
        buttonLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ecran = 1;
                setTab(1);
                MAJTab2(1);
            }
        });

        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ecran = 2;
                setTab(2);
                MAJTab2(2);
            }
        });

        buttonCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setCenterTab();
            }
        });

        Typeface font = Typeface
                .createFromAsset(getAssets(), "Calibri.ttf");
        textTitle = (TextView) findViewById(R.id.textTitle);
        textInstruction = (TextView) findViewById(R.id.textInstruction);
        textLeft = (TextView) findViewById(R.id.text_left);
        textRight = (TextView) findViewById(R.id.text_right);


        textStatusB1 = (AutoResizeTextView) findViewById(R.id.textViewStatusB1);
        textStatusB1.setTypeface(font);
        textStatusB2 = (AutoResizeTextView) findViewById(R.id.textViewStatusB2);
        textStatusB2.setTypeface(font);
        mNavigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        mLayoutCenter = (LinearLayout) findViewById(R.id.layoutCenter);
        mLayoutPC = (LinearLayout) findViewById(R.id.layoutPC);

        textStatus = (TextView) findViewById(R.id.textStatus);
        textStatus.setTypeface(font);
        textBorneDirection = (TextView) findViewById(R.id.textBorneDirection);
        textBorneDirection.setTypeface(font);
        textCommentaire = (TextView) findViewById(R.id.textCommentaire);
        textCommentaire.setTypeface(font);
        imageArrowG = (ImageView) findViewById(R.id.imageViewG);
        imageArrowD = (ImageView) findViewById(R.id.imageViewD);
        layoutProgress = (LinearLayout) findViewById(R.id.layoutProgress);
        ;

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/html.html");


        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();


        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_center), getResources().getColor(R.color.colorg2Bleu), "CENTER"));


        for (int i = 0; i < mGlobal.getEvseList().length; i++) {
            models.add(new NavigationTabBar.Model(
                    getResources().getDrawable(R.drawable.ic_left), getResources().getColor(R.color.colorg2BleuLight), mGlobal.getEvseList()[i].getmBorne()));

        }


        textStatusB1.setBackground(getResources().getDrawable(R.drawable.bordergreen));
        textStatusB2.setBackground(getResources().getDrawable(R.drawable.bordergreen));

        mNavigationTabBar.setModels(models);
        mNavigationTabBar.post(new Runnable() {
            @Override
            public void run() {
                final View bgNavigationTabBar = findViewById(R.id.bg_ntb_horizontal);
                bgNavigationTabBar.getLayoutParams().height = (int) mNavigationTabBar.getBarHeight();
                bgNavigationTabBar.requestLayout();
            }
        });

        mNavigationTabBar.setModelIndex(0);
        setCenterTab();


    }

    public void setTab(int indexTab) {
        mNavigationTabBar.setModelIndex(indexTab);
        mLayoutCenter.setVisibility(View.INVISIBLE);
        mLayoutPC.setVisibility(View.VISIBLE);
    }

    public void setCenterTab() {
        ecran = -1;
        mLayoutCenter.setVisibility(View.VISIBLE);
        mLayoutPC.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    public String getEXT_STATUS(int frame4) {
        String ext_status = null;
        if ((frame4 & 0x01) != 0)
            ext_status = "NEED CH ";
        else if ((frame4 & 0x02) != 0) {
            ext_status = "RESERVED ";
        } else if ((frame4 & 0x10) != 0)
            ext_status = "3PH EV ";
        else if ((frame4 & 0x20) != 0) {
            ext_status = "DOOR_OPENED";
        } else if ((frame4 & 0x08) != 0) {
            ext_status = "BOOT ";
        }
        return ext_status;
    }


    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    public void clickLngFr(View view) {
        Log.e("fr", "fr");
        languageCode = "fr";
        updateViews();
    }

    public void clickLngEsp(View view) {
        Log.e("esp", "esp");
        languageCode = "es";
        updateViews();
    }

    public void clickLngEng(View view) {
        Log.e("eng", "eng");
        languageCode = "en";
        updateViews();
    }

    public void clickLngDe(View view) {
        Log.e("De", "de");
        languageCode = "de";
        updateViews();
    }

    private void updateViews() {
        Context context = LocaleHelper.setLocale(this, languageCode);
        Resources resources = context.getResources();
        textTitle.setText(resources.getString(R.string.TEXT_TITLE));
        textInstruction.setText(resources.getString(R.string.TEXT_INSTRUCTION));
        textRight.setText(resources.getString(R.string.TEXT_RIGHT));
        textLeft.setText(resources.getString(R.string.TEXT_LEFT));
        textStatusB1.setText(resources.getString(R.string.STATUS_UNKNOWN));
        textStatusB2.setText(resources.getString(R.string.STATUS_UNKNOWN));
        MAJInfoCenter2(indexTab + 1);
        if (ecran == 1)
            MAJTab2(1);
        else if (ecran == 2)
            MAJTab2(2);
    }

}