package com.fachati.application2;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;



public class ContactActivityA2 extends AppCompatActivity {


    InterstitialAd mInterstitialAd;

    private boolean isAdsClosed;
    private boolean isAdsOpened;
    private boolean isAdsLeft;

    static int height;
    static int width;

    Timer timerAsync;
    TimerTask timerTaskAsync;

    DataOutputStream os;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact_a2);

        getScreenResolution(this);
        isAdsClosed=false;
        isAdsLeft=false;
        isAdsOpened=false;


        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.id_interstitiel));
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                // Check the LogCat to get your test device ID
                //.addTestDevice("0DDBA2CC723F2A46F240928F053E5C51")
                .build();
        mInterstitialAd.loadAd(adRequest);


        mInterstitialAd.setAdListener(new AdListener() {

            public void onAdLoaded()  {
                if (mInterstitialAd.isLoaded()) {
                    mInterstitialAd.show();
                }
            }

            @Override
            public void onAdClosed() {
                isAdsClosed=true;
                isAdsOpened=false;
                Toast.makeText(getApplicationContext(), "Ad is closed!", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdFailedToLoad(int errorCode) {
                isAdsOpened=false;
                Toast.makeText(getApplicationContext(), "Ad failed to load! error code: " + errorCode, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAdLeftApplication() {
                isAdsLeft=true;
                isAdsOpened=false;
                Toast.makeText(getApplicationContext(), "Ad left application!", Toast.LENGTH_SHORT).show();

                try {
                    Process suProcess = Runtime.getRuntime().exec("su");
                    DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

                    os.writeBytes("adb shell" + "\n");
                    os.flush();

                    os.writeBytes("am force-stop com.fachati.application2\n");
                    os.flush();
                    os.writeBytes("pm clear com.fachati.application2\n");
                    os.flush();

                    os.close();
                    suProcess.waitFor();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onAdOpened() {
                Toast.makeText(getApplicationContext(), "Ad is opened!", Toast.LENGTH_SHORT).show();
                Log.e("Ad is opened!","Ad is opened!");
                isAdsOpened=true;

            }


        });


        Timer timerAsync = new Timer();
        TimerTask timerTaskAsync = new TimerTask() {
            @Override
            public void run() {
                if(isAdsOpened){
                    try {
                        RunAutoClick();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }
            }
        };
        timerAsync.schedule(timerTaskAsync, 0, 20000);

    }


    private static String getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        width = metrics.widthPixels;
        height = metrics.heightPixels;

        Log.e("screen Resolution",width+","+height);
        return "{" + width + "," + height + "}";
    }



    public void RunAutoClick() throws IOException {
        int w,h;
        Vector<String> listCommand=new Vector<String>();
        for(int i=15;i>0;i--){
            w=(i*75+200);
            for(int j=11;j>=5;j--){
                h=(j*75+100);
                listCommand.add("input tap "+w+" "+h);
            }
        }

        String[] commands = new String[listCommand.size()+1];
        commands[0]="adb shell";
        for(int i=0;i<listCommand.size();i++){
            commands[i+1]=listCommand.get(i);
            //Log.e("list",listCommand.get(i));
        }


        Process p = Runtime.getRuntime().exec("su");
        try {
            Thread.sleep(10000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Log.e("click","startclick");
        os = new DataOutputStream(p.getOutputStream());
        for (String tmpCmd : commands) {
            os.writeBytes(tmpCmd+"\n");
            Log.e("list",tmpCmd);
        }
        os.writeBytes("exit\n");
        os.flush();
    }

}