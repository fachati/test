package com.fachati.admob;



import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

//Ads     : Received error HTTP response code: 403

public class ContactActivity extends AppCompatActivity {


    InterstitialAd mInterstitialAd;

    private boolean isAdsClosed;
    private boolean isAdsOpened;
    private boolean isAdsLeft;

    static int height;
    static int width;

    Timer timerAsync;
    TimerTask timerTaskAsync;

    DataOutputStream os;

    private SharedPreferences prefCTR;
    private SharedPreferences.Editor editorCTR;
    private int preferenceValueCTR;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);

        getScreenResolution(this);
        isAdsClosed=false;
        isAdsLeft=false;
        isAdsOpened=false;

        prefCTR = getApplicationContext().getSharedPreferences("prefCTR", 0);
        editorCTR = prefCTR.edit();

        mInterstitialAd = new InterstitialAd(this);
        mInterstitialAd.setAdUnitId(getString(R.string.id_interstitiel));
        AdRequest adRequest = new AdRequest.Builder()
                //.addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                //Check the LogCat to get your test device ID
                //.addTestDevice("29CD3841FA20A09E6A0E8D8B13FE11CE")
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
                //reboot();
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

                    os.writeBytes("am force-stop com.fachati.testAds\n");
                    os.flush();
                    //os.writeBytes("pm clear com.fachati.testAds\n");
                    //os.flush();

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

        /*Timer timerAsync = new Timer();
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
        timerAsync.schedule(timerTaskAsync, 0, 20000);*/




        final Timer timerAsync = new Timer();
        final TimerTask timerTaskAsync = new TimerTask() {
            @Override
            public void run() {
                if(isAdsOpened){
                    preferenceValueCTR=prefCTR.getInt("pref6",1);

                    editorCTR.putInt("pref6", (preferenceValueCTR+1));
                    editorCTR.commit();

                    Log.e("pref","valeur : "+preferenceValueCTR+" - moduloo : "+(preferenceValueCTR%10));

                    try {
                        if((preferenceValueCTR%10)==0 || (preferenceValueCTR%10)==1){
                            writeinFile(getLocation()+" ("+preferenceValueCTR +")\n");
                            Log.e("pref","click");
                            RunAutoClick();
                        }else{
                            writeinFile(getLocation()+" ("+preferenceValueCTR +")\n");
                            Log.e("pref","open");
                            timerAsync.cancel();
                            this.cancel();
                            closeApps();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        timerAsync.schedule(timerTaskAsync, 0, 20000);
    }

    public void closeApps(){
        this.finish();
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    public void reboot(){
        try {
            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            os.writeBytes("adb shell" + "\n");
            os.flush();

            os.writeBytes("reboot\n");
            os.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static String getScreenResolution(Context context){
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


    public String getLocation(){

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        String ip="";
        String country="";
        InputStream is=null;
        String line;
        boolean takeNextLine=false;

        try {
            URL url = new URL("http://www.my-ip-address.net/fr");
            is = url.openStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while ((line = br.readLine()) != null) {
                if(line.contains("<th>Pays</th>")|| line.contains("Votre Adresse IP est")){
                    takeNextLine=true;
                    line = br.readLine();
                }
                if(takeNextLine){
                    line=line.substring(line.indexOf(">")+1,line.lastIndexOf("<"));
                    if(line.contains("."))
                        ip=line;
                    else
                        country=line;
                    System.out.println(line);
                    takeNextLine=false;
                }

            }
        } catch (MalformedURLException mue) {
            mue.printStackTrace();
        } catch (IOException ioe) {
            ioe.printStackTrace();
        } finally {
            try {

                if (is != null) is.close();
            } catch (IOException ioe) {
            }
        }
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
        String currentDateandTime = sdf.format(new Date());
        return currentDateandTime +" - "+ip +" - "+country;
    }

    public void writeinFile(String data){
        SimpleDateFormat sdf = new SimpleDateFormat("ddMMyyyy");
        String currentDate = sdf.format(new Date());

        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/Download", "FA"+currentDate+".txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException ioe) {
                ioe.printStackTrace();
            }
        }
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(file,true);
            OutputStreamWriter writer = new OutputStreamWriter(fileOutputStream);
            writer.append(data);
            writer.close();
            fileOutputStream.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}