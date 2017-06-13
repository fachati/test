package com.fachati.application1;

import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.text.format.Formatter;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Enumeration;
import java.util.logging.Logger;

public class WelcomeActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);


        /*Log.e("get location",getLocation());
        getLocation();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(WelcomeActivity.this, ContactActivityA1.class);
                startActivity(i);
                finish();
            }
        }, 3000);*/

        if(Build.VERSION.SDK_INT> 22 ){
            checkPermission();
        }else{
            final TelephonyManager tm =(TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

            String deviceid = tm.getDeviceId();
            Log.e("device id",deviceid+"---------------------------------------------------");
            Log.e("device id",md5(deviceid)+"--------------------------------------------45-***-");

        }




    }

    public static final String md5(final String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest
                    .getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i = 0; i < messageDigest.length; i++) {
                String h = Integer.toHexString(0xFF & messageDigest[i]);
                while (h.length() < 2)
                    h = "0" + h;
                hexString.append(h);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            //Logger.logStackTrace(TAG,e);
        }
        return "";
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            WelcomeActivity.this.requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 100);
            return;
        }else{
            final TelephonyManager tm =(TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

            String deviceid = tm.getDeviceId();
            Log.e("device id",deviceid+"---------------------------------------------------");
            Log.e("device id",md5(deviceid)+"---------------------------------------3453------***-");
            Log.e("device id",md5("A8BEF947BED69CBE")+"-------------------------------453453--------------***-");
            Log.e("device id",md5("3201000D8F530E5E")+"-------------------------------453453--------------***-");
            Log.e("device id",md5("074F124C0C8CE93C")+"-------------------------------453453--------------***-");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    final TelephonyManager tm =(TelephonyManager)getBaseContext().getSystemService(Context.TELEPHONY_SERVICE);

                    String deviceid = tm.getDeviceId();
                    Log.e("device id",deviceid+"---------------------------------------------------");
                    Log.e("device id",md5("A8BEF947BED69CBE")+"-------------------------------453453--------------***-");
                    Log.e("device id",md5("3201000D8F530E5E")+"-------------------------------453453--------------***-");
                    Log.e("device id",md5("074F124C0C8CE93C")+"-------------------------------453453--------------***-");
                } else {
                    WelcomeActivity.this.finish();
                    System.exit(0);
                }
            }
        }
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
        return ip +" - "+country;
    }
}
