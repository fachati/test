package com.fachati.RestartApps;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.PowerManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import java.io.File;
import java.io.IOException;

public class WelcomeActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        Intent intent = new Intent(WelcomeActivity.this, Service.class);
        startService(intent);
        /*getScreenResolution(this);
        deleteCache(this);




        final Context ctx=this;
        final Handler handler = new Handler();
// Define the code block to be executed
       Runnable runnableCode = new Runnable() {
            @Override
            public void run() {

                Intent i = ctx.getPackageManager().getLaunchIntentForPackage("com.fachati.TestAdds");
                ctx.startActivity(i);


                // Repeat this the same runnable code block again another 2 seconds
                handler.postDelayed(this, 300000);
            }
        };
// Start the initial runnable task by posting through the handler
        handler.post(runnableCode);*/




    }


    public static void deleteCache(Context context) {
        try {
            File dir = context.getCacheDir();
            File file=new File("/data/user/0/com.android.vending/cache");
            Log.e("file dire ",dir.getAbsolutePath());
            deleteDir(file);
        } catch (Exception e) {}
    }

    public static boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                Log.e("tyest","sddsf");
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
            return dir.delete();
        } else if(dir!= null && dir.isFile()) {
            return dir.delete();
        } else {
            return false;
        }
    }

    private static String getScreenResolution(Context context)
    {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics metrics = new DisplayMetrics();
        display.getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        System.out.println("{" + width + "," + height + "}");
        return "{" + width + "," + height + "}";
    }



}
