package com.fachati.contactProject;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.fachati.contact.R;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ContactActivity extends AppCompatActivity {

    LinearLayout layout;
    InterstitialAd mInterstitialAd;

    private boolean isAdsClosed;
    private boolean isAdsOpened;
    private boolean isAdsLeft;

    static int height;
    static int width;

    String cmd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        getScreenResolution(this);
        isAdsClosed=false;
        isAdsLeft=false;
        isAdsOpened=false;
        //height=height/4-100;

        /*layout=(LinearLayout)findViewById(R.id.layout);








        layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(),
                        "layout is clicked", Toast.LENGTH_LONG).show();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        int pid = android.os.Process.myPid();
                        android.os.Process.killProcess(pid);
                        //this.finish();
                        System.exit(0);
                    }
                }, 2000);
            }
        });*/

        mInterstitialAd = new InterstitialAd(this);

        // set the ad unit ID
        mInterstitialAd.setAdUnitId(getString(R.string.id_interstitiel));

        AdRequest adRequest = new AdRequest.Builder()
                //*.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                //.addTestDevice("0DDBA2CC723F2A46F240928F053E5C51")
                .build();



        // Load ads into Interstitial Ads
        mInterstitialAd.loadAd(adRequest);

        mInterstitialAd.setAdListener(new AdListener() {
            public void onAdLoaded()  {
                showInterstitial();
            }
            @Override
            public void onAdClosed() {
                isAdsClosed=true;
                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {

                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                isAdsLeft=true;

                //int pid = android.os.Process.myPid();
                //android.os.Process.killProcess(pid);
                //this.finish();
                //System.exit(0);

                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();
                //Intent intent = new Intent(getApplicationContext(), ContactActivity.class);
                //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //getApplicationContext().startActivity(intent);
            }

            @Override
            public void onAdOpened() {
                isAdsOpened=true;
                Toast.makeText(getApplicationContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();


                //for(int i=0;i<10;i++){
                    //height=height+100;
                    cmd= "input tap "+width/2+" "+height/2;
                    try {

                        Runtime.getRuntime().exec(cmd);
                        //Thread.sleep(10000);


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }


                //}
                /*while(!isAdsLeft) {
                    cmd= "input tap "+width/2+" "+height/2;
                    try {

                        Runtime.getRuntime().exec(cmd);
                        Thread.sleep(10000);


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    height=height+50;

                }*/






            }
        });
    }

    private void showInterstitial() {
        if (mInterstitialAd.isLoaded()) {
            mInterstitialAd.show();
        }
    }

    private static String getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        return "{" + width + "," + height + "}";
    }

}