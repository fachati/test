package com.fachati.RestartApps;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

public class Service extends android.app.Service {

    private static final String TAG = "Service";


    @Override
    public void onCreate() {
        Log.i(TAG, "Service onCreate");

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.i(TAG, "Service onStartCommand");

        final Context ctx=this;
        final Handler handler = new Handler();
        Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                System.out.println("***************************** im here");
                Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.fachati.TestAdds");
                ctx.startActivity(i);


                handler.postDelayed(this, 10000);
            }
        };
        handler.post(runnableCode);

        return android.app.Service.START_STICKY;
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
}