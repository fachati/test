package com.fachati.firstA;



import android.*;
import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.ads.identifier.AdvertisingIdClient;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


public class WelcomeActivity extends AppCompatActivity {

    private String propReplaceFile;
    private String tempFile;
    Uri URI = Uri
            .parse("content://com.google.android.gsf.gservices");
    private static final String ID_KEY = "android_id";

    String getAndroidGSFId(Context ctx) {
        String[] params = { ID_KEY };
        Cursor c = ctx.getContentResolver()
                .query(URI, null, null, params, null);

        if (!c.moveToFirst() || c.getColumnCount() < 2)
            return null;

        try {
            return Long.toHexString(Long.parseLong(c.getString(1)));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);



        String device_id = Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ANDROID_ID);

        /*AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    AdvertisingIdClient.Info adInfo = AdvertisingIdClient.getAdvertisingIdInfo(WelcomeActivity.this);
                    String adId = adInfo != null ? adInfo.getId() : null;
                    Log.e(" ads id",adId);
                } catch (IOException | GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException exception) {
                    // Error handling if needed
                }
            }
        });*/








        Log.e(" get location",getLocation()+" ---- "+IsReachable(this));
        Log.e(" gsf id",getAndroidGSFId(this));
        Log.e(" Mac address",readMacAddress());
        String deviceId = md5(device_id).toUpperCase();
        Log.e("device test id",device_id+" -- "+deviceId);

        Log.i("TAG", "SERIAL: " + Build.SERIAL);
        Log.i("TAG","MODEL: " + Build.MODEL);
        Log.i("TAG","ID: " + Build.ID);
        Log.i("TAG","Manufacture: " + Build.MANUFACTURER);
        Log.i("TAG","brand: " + Build.BRAND);
        Log.i("TAG","type: " + Build.TYPE);
        Log.i("TAG","user: " + Build.USER);
        Log.i("TAG","BASE: " + Build.VERSION_CODES.BASE);

        Log.i("TAG","INCREMENTAL " + Build.VERSION.INCREMENTAL);
        Log.i("TAG","SDK  " + Build.VERSION.SDK);
        Log.i("TAG","BOARD: " + Build.BOARD);
        Log.i("TAG","BRAND " + Build.BRAND);
        Log.i("TAG","HOST " + Build.HOST);
        Log.i("TAG","FINGERPRINT: "+Build.FINGERPRINT);
        Log.i("TAG","Version Code: " + Build.VERSION.RELEASE);




        if(Build.VERSION.SDK_INT> 22 ){
            checkPermission();
            checkPermissionIMEI();
        }else{
            Log.e("permission","ok");
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String device_imei =telephonyManager.getDeviceId();
            Log.e(" device imei",device_imei+"==");

        }

        if (IsReachable(this)==true && checkInternet()){
            Log.e("connexion","ok");
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent i = new Intent(WelcomeActivity.this, ContactActivity.class);
                    startActivity(i);
                    finish();
                }
            }, 3000);
        }else{
            Log.e("connexion","ko");

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy_HH:mm:ss");
            String currentDateandTime = sdf.format(new Date());
            writeinFile(currentDateandTime +"no connexion\n");
            reboot();

        }
    }

    public String md5(String s) {
        try {
            // Create MD5 Hash
            MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
            digest.update(s.getBytes());
            byte messageDigest[] = digest.digest();

            // Create Hex String
            StringBuffer hexString = new StringBuffer();
            for (int i=0; i<messageDigest.length; i++)
                hexString.append(Integer.toHexString(0xFF & messageDigest[i]));
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
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



    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermission() {
        if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            WelcomeActivity.this.requestPermissions(new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            return;
        }
    }

    @TargetApi(Build.VERSION_CODES.M)
    private void checkPermissionIMEI() {
        if (checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            WelcomeActivity.this.requestPermissions(new String[]{android.Manifest.permission.READ_PHONE_STATE}, 100);
            return;
        }else{
            TelephonyManager telephonyManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
            String device_imei =telephonyManager.getDeviceId();
            Log.e(" device imei",device_imei+"==");
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Log.e("permission","ok");
                } else {
                    WelcomeActivity.this.finish();
                    System.exit(0);
                }
            }
        }
    }

    public static boolean IsReachable(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        final NetworkInfo netInfo = connMgr.getActiveNetworkInfo();
        boolean isReachable = false;

        if (netInfo != null && netInfo.isConnected()) {
            try {
                URL url = new URL("http://www.google.com");
                HttpURLConnection urlc = (HttpURLConnection) url.openConnection();
                urlc.setRequestProperty("User-Agent", "Android Application");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(10 * 1000);
                urlc.connect();
                isReachable = (urlc.getResponseCode() == 200);
            } catch (IOException e) {
            }
        }

        return isReachable;
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
        return ip +" - "+country + " - "+currentDateandTime+"\n";
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

    public boolean checkInternet() {
        ConnectivityManager ConnectionManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = ConnectionManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected() == true) {
            return true;
        }else {
            return false;
        }
    }



    ///////////////////////////////////////////////////////
    public String readMacAddress(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("/sys/class/net/wlan0/address"));
            String mac=br.readLine();
            br.close();
            return  mac;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }




}
