package com.javatechig.robot;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.javatechig.serviceexample.R;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static android.content.ContentValues.TAG;


public class HelloActivity extends Activity {

    private String tempFile;
    private String propReplaceFile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hello);

        PowerManager.WakeLock screenLock = ((PowerManager)getSystemService(POWER_SERVICE)).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG");
        screenLock.acquire();
        screenLock.release();

        Intent intent = new Intent(HelloActivity.this, HelloService.class);
        startService(intent);

        /*propReplaceFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/propreplace.txt";
        createTempFile();
        populateList();

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        saveProp();*/
        //addListenerOnButton();

    }


    private void createTempFile() {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock4 /system\n");
            os.writeBytes("cp -f /system/build.prop " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp\n");
            os.writeBytes("chmod 777 " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp\n");
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        tempFile = Environment.getExternalStorageDirectory().getAbsolutePath() + "/buildprop.tmp";
    }

    public void populateList() {
        final Properties prop = new Properties();
        File file = new File(tempFile);
        try {
            prop.load(new FileInputStream(file));
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
        }

        final String[] pTitle = prop.keySet().toArray(new String[0]);
        final List<String> pDesc = new ArrayList<String>();
        for (int i = 0; i < pTitle.length; i++) {
            pDesc.add(prop.getProperty(pTitle[i]));
        }

        ArrayList<Map<String, String>> list = buildData(pTitle, pDesc);
        for (int i=0;i<list.size();i++){
            Log.e("list :"+i,list.get(i).toString());
        }
    }

    private ArrayList<Map<String, String>> buildData(String[] t, List<String> d) {
        ArrayList<Map<String, String>> list = new ArrayList<Map<String, String>>();

        for (int i = 0; i < t.length; ++i) {
            list.add(putData(t[i], d.get(i)));
        }
        return list;
    }

    private HashMap<String, String> putData(String title, String description) {
        HashMap<String, String> item = new HashMap<String, String>();

        item.put("title", title);
        item.put("description", description);

        return item;
    }

    private void saveProp(){
        final Properties prop = new Properties();

        try {
            FileInputStream in = new FileInputStream(new File(tempFile));
            prop.load(in);
            in.close();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
        }

        prop.setProperty("ro.product.locale", "en-FR");

        try {
            FileOutputStream out = new FileOutputStream(new File(tempFile));
            prop.store(out, null);
            out.close();

            replaceInFile(new File(tempFile));
            transferFileToSystem();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "Error: " + e, Toast.LENGTH_SHORT).show();
        }
    }

    private void replaceInFile(File file) throws IOException {
        File tmpFile = new File(propReplaceFile);
        FileWriter fw = new FileWriter(tmpFile);
        Reader fr = new FileReader(file);
        BufferedReader br = new BufferedReader(fr);

        while (br.ready()) {
            fw.write(br.readLine().replaceAll("\\\\", "") + "\n");
        }

        fw.close();
        br.close();
        fr.close();
    }

    private void transferFileToSystem() {
        Process process = null;
        DataOutputStream os = null;

        try {
            process = Runtime.getRuntime().exec("su");
            os = new DataOutputStream(process.getOutputStream());
            os.writeBytes("mount -o remount,rw -t yaffs2 /dev/block/mtdblock4 /system\n");
            os.writeBytes("mv -f /system/build.prop /system/build.prop.bak\n");
            os.writeBytes("busybox cp -f " + propReplaceFile + " /system/build.prop\n");
            os.writeBytes("chmod 644 /system/build.prop\n");

            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                process.destroy();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        Toast.makeText(getApplicationContext(), "Edit saved and a backup was made at " + Environment.getExternalStorageDirectory().getAbsolutePath() + "/build.prop.bak", Toast.LENGTH_SHORT).show();
    }


    String[] commands = { "ip link set wlan0 down" , "ip link set wlan0 address 48:59:29:f3:9c:dc" , "ip link set wlan0 up", "ip link" };
    public void addListenerOnButton() {

                try {
                    RunAsRoot(commands);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }

    }

    public void RunAsRoot(String[] cmds) throws IOException{
        Process p = Runtime.getRuntime().exec( "su" );
        DataOutputStream os = new DataOutputStream(p.getOutputStream());
        for (String tmpCmd : cmds) {
            os.writeBytes(tmpCmd+ " \n " );
        }
        BufferedReader in = new BufferedReader( new InputStreamReader(p.getInputStream()));
        String line = null;
        while ((line = in.readLine()) != null) {
            Log.i(line,line);
        }
        os.writeBytes( "exit \n " );
        os.flush();
    }

    public void readMacAddress(){
        try {
            BufferedReader br = new BufferedReader(new FileReader("/sys/class/net/wlan0/address"));
            Log.i(TAG, "mac addr: " + br.readLine());
            br.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}


