package com.javatechig.robot;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.provider.Browser;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

public class HelloService extends Service {

    private static final String TAG = "Service";
    private Vector<String> listPackage;
    private SharedPreferences pref;
    private SharedPreferences.Editor editor;
    private int preferenceValue;




    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");
        listPackage=new Vector<String>();
        listPackage.add("com.fachati.FirstA");
        /*listPackage.add("com.fachati.application2");
        listPackage.add("com.fachati.application3");
        listPackage.add("com.fachati.application4");
        listPackage.add("com.fachati.application5");*/
        pref = getApplicationContext().getSharedPreferences("MyPref", 0);
        editor = pref.edit();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        Timer timerAsync = new Timer();
        TimerTask timerTaskAsync = new TimerTask() {
            @Override
            public void run() {
                clearCacheChrome();
                changeIP();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                RestartApps("com.fachati.firstA");
            }
        };
        timerAsync.schedule(timerTaskAsync, 0, 300000);


        /*Timer timerAsync = new Timer();
        TimerTask timerTaskAsync = new TimerTask() {
            @Override
            public void run() {
                preferenceValue=pref.getInt("pref",0);
                if(preferenceValue==0 || preferenceValue==5){
                    preferenceValue=1;
                    editor.putInt("pref", preferenceValue);
                    editor.commit();
                    Log.e("preference Value","=======>"+preferenceValue);
                    Log.e("vector Value","=======>"+listPackage.get(preferenceValue-1));
                    clearCacheChrome();

                    RestartApps(listPackage.get(preferenceValue-1));
                }else{
                    preferenceValue=preferenceValue+1;
                    editor.putInt("pref", preferenceValue);
                    editor.commit();
                    Log.e("preference Value","=======>"+preferenceValue);
                    Log.e("vector Value","=======>"+listPackage.get(preferenceValue-1));
                    clearCacheChrome();
                    RestartApps(listPackage.get(preferenceValue-1));

                }

            }
        };
        timerAsync.schedule(timerTaskAsync, 0, 5000);// 5 min ==> 300000*/
        return Service.START_STICKY;
    }


    @Override
    public IBinder onBind(Intent arg0) {
        Log.i(TAG, "Service onBind");
        return null;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "Service onDestroy");
    }


    public void swipe() throws IOException {
        DataOutputStream os;
        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int we1 = metrics.widthPixels/4;
        int we2 = metrics.widthPixels/4*3;
        int he = metrics.heightPixels/2;


        String[] commands = new String[2];
        commands[0]="adb shell";
        commands[1]="input swipe "+we1+" "+he+" "+we2+" "+he;


        Process p = Runtime.getRuntime().exec("su");
        os = new DataOutputStream(p.getOutputStream());
        for (String tmpCmd : commands) {
            os.writeBytes(tmpCmd+"\n");
            Log.e("list",tmpCmd);
        }
        os.writeBytes("exit\n");
        os.flush();
    }

    public void changeIP(){
        try {

            Intent i = this.getPackageManager().getLaunchIntentForPackage("org.torproject.android");
            this.startActivity(i);

            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());


            Thread.sleep(2000);

            swipe();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void RestartApps(String application){
        try {

            Process suProcess = Runtime.getRuntime().exec("su");
            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());


            os.writeBytes("adb shell" + "\n");
            os.flush();

            /*os.writeBytes("am force-stop com.el.fachati.appa\n");
            os.flush();
            os.writeBytes("pm clear com.el.fachati.appa\n");
            os.flush();*/

            os.writeBytes("am force-stop com.android.chrome\n");
            os.flush();
            os.writeBytes("pm clear com.android.chrome\n");
            os.flush();

            os.writeBytes("am force-stop com.android.vending\n");
            os.flush();
            os.writeBytes("pm clear com.android.vending\n");
            os.flush();

            /*os.writeBytes("pm clear com.google.android.gms\n");
            os.flush();*/

            os.close();
            suProcess.waitFor();


            Thread.sleep(2000);
            Intent i = this.getPackageManager().getLaunchIntentForPackage(application);
            this.startActivity(i);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    public void clearCacheChrome(){
        ContentResolver cR = getContentResolver();

        if(Browser.canClearHistory(cR)){
            Browser.clearHistory(cR);
            Browser.clearSearches(cR);
        }
    }

}