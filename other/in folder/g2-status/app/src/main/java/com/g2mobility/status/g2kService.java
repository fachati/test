package com.g2mobility.status;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;
import com.digi.xbee.api.exceptions.XBeeException;
import com.digi.xbee.api.models.XBee64BitAddress;
import com.g2mobility.xbee.api.G2Bee;
import com.g2mobility.xbee.api.listeners.IG2BeeDataReceiveListener;
import com.g2mobility.xbee.api.models.G2BeeMessage;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class g2kService extends Service{

    private G2Bee mG2Bee;
    private String TAG="g2KService";
    private GlobalClass mGlobal = null;

    public static final String ACTION_SEND_STATUS ="com.g2mobility.SEND_STATUS";
    public static final String ACTION_START = "com.g2mobility.start";

    public static volatile ScheduledExecutorService sPool = Executors.newScheduledThreadPool(2);

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "g2KService start");

        mGlobal = (GlobalClass) getApplicationContext();

        mG2Bee = new G2Bee(this, TAG);


    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        if (intent != null && intent.getAction() != null) {
            if (intent.getAction().equals(ACTION_START)) {
                sPool.submit(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(3000);
                            mG2Bee.addDataListener(mListener);
                            blink(mGlobal.getEvseList()[0].getAddress(), (byte) 0x05);//"address": "0013A20040 B0CE39"
                            blink(mGlobal.getEvseList()[1].getAddress(), (byte) 0x05);//"address": "0013A20040 B656F7"

                            //sendSetStationAddress();
                            sendConfiguration();

                        } catch (Exception e) {
                            Log.e(TAG, "Exception: Discover", e);
                        }
                    }
                });
            }

        }
        return START_STICKY;
    }


    private void blink(XBee64BitAddress address, byte time) {
        try {
            if (time > 255) {
                time = (byte) 255;
            }

            byte[] payload = new byte[]{
                    (byte) 0xF2, 0x00, 0x27, time
            };
            mG2Bee.sendDataAsync(address, payload);

        }  catch (XBeeException e) {
            e.printStackTrace();//0x4D, 0x58, 0x31, 0x38, 0x30, 0x39, 0x30, 0x39
        }
    }

    public String showTab(int[] tab) {
        String SA = "";
        for (int i:tab)
            SA = SA + " " + Integer.toHexString(i);
        return SA;
    }

    public int[] byteToInt(byte[] data){
        int[] result=new int[data.length];
        for(int i=0;i<data.length;i++){
            result[i]=data[i]&255;
        }
        return result;
    }


    public String toUpperCase(String tag){
        String[] tabTag=tag.split(" ");
        String tag2="";
        for(int i=0;i<tabTag.length;i++){
            if(tabTag[i].length()==1){
                tag2=tag2+" 0"+tabTag[i];
            }else if(tabTag.length>1){
                tag2=tag2+" "+tabTag[i];
            }
        }
        return tag2.toUpperCase();
    }


    private IG2BeeDataReceiveListener mListener = new IG2BeeDataReceiveListener.Stub() {
        @Override
        public void dataReceived(final G2BeeMessage message) throws RemoteException {

            final int[] data = byteToInt(message.getData());
            Intent  intentSendInformation = new Intent(ACTION_SEND_STATUS);
                Log.e("recievenreceive",showTab(data)+"\n"+message.getDevice().getXbee64BitAddress());
                intentSendInformation.putExtra("address", message.getDevice().getXbee64BitAddress().toString());// set address borne

            if(data[1]==0x10) {
                Log.e("receive status****",getUiState(data)+"");
                intentSendInformation.putExtra("UIStatus",getUiState(data));
                sendBroadcast(intentSendInformation);
            }else if(data[1]==0xFE){
                    Log.e("receive++++++++",showTab(data));
                //---------------------------------------------------------------------------------- PING   f3 fe 0 0 0 20 6 e 60 0 8(4) 1 e6 1 4 0 f0 cc 8c 34
                if(data[5]== 0x20) {

                    int [] dataPING=new int[data.length-6];
                    System.arraycopy(data,6,dataPING,0,dataPING.length);
                    Log.e("dataPing",showTab(dataPING));

                    intentSendInformation.putExtra("cmd", "PING");
                    intentSendInformation.putExtra("status", dataPING[0]);
                    intentSendInformation.putExtra("error_status", dataPING[3]);
                    intentSendInformation.putExtra("ext_status", dataPING[4]);

                    if(dataPING.length>6){
                        intentSendInformation.putExtra("sessionID",dataPING[6]);
                        intentSendInformation.putExtra("occupied",dataPING[5]);    //01 11 10 00 occupied
                        intentSendInformation.putExtra("proto",dataPING[9]);
                        intentSendInformation.putExtra("tag", getTAG(dataPING,10,dataPING[8]));

                    }else{
                        intentSendInformation.putExtra("sessionID",-1);
                    }

                    sendBroadcast(intentSendInformation);
                }

                //---------------------------------------------------------------------------------- SESSION

            }else if(data[1]==0x80){ // 80
                //---------------------------------------------------------------------------------- AUTH
                Log.e("receive---------",showTab(data));

                if(data[5]==0xe0){ //e0
                    intentSendInformation.putExtra("cmd", "AUTH");
                    intentSendInformation.putExtra("tag",getTAG(data,8,data[6]));

                    //------------------------------------------------------------------------------ START
                }else if(data[5]==0xe2) { //e2  f3 80 0 0 0 e2 e6 1 4 0 f0 cc 8c 34 0 0 1a b

                    intentSendInformation.putExtra("cmd", "START");
                    intentSendInformation.putExtra("sessionID",data[6]);
                    intentSendInformation.putExtra("tag",getTAG(data,10,data[8]));
                    intentSendInformation.putExtra("time",new Date().getTime());
                    intentSendInformation.putExtra("plug",data[7]);

                    //------------------------------------------------------------------------------ STOP
                }else if(data[5]==0xe3){ //e3   f3 80 0 0 0 e3 e5 1 4 0 f0 cc 8c 34 0 0 1a b


                    intentSendInformation.putExtra("plug",data[7]);
                    intentSendInformation.putExtra("cmd", "STOP");
                    intentSendInformation.putExtra("time",new Date().getTime());

                }
                sendBroadcast(intentSendInformation);
            }


        }

    };


    private void sendSetStationAddress(){
        Log.e("startSaveLogTest","startSaveLogTest");
        try {
            byte[] setStationAddress = new byte[14];
            byte[] addresselocal = mG2Bee.getLocalXBeeDevices().get(0).getXbee64BitAddress().getValue();
            System.arraycopy(new byte[]{
                    (byte) 0xF3, 0x00, (byte) 0xFE, 0x00, 0x00, 0x23
            }, 0, setStationAddress, 0, 6);

            System.arraycopy(addresselocal, 0, setStationAddress, 6, 8);

            mG2Bee.sendData(mGlobal.getEvseList()[0].getAddress(), setStationAddress);
            mG2Bee.sendData(mGlobal.getEvseList()[1].getAddress(), setStationAddress);


        }  catch (XBeeException e) {
            e.printStackTrace();
        }
    }

    private void sendConfiguration(){
        //mXBeeManager.sendAsynchronous(new ZNetTxRequest(address, new int[]{0xF3, 0x00, 0xFD, 0x00, 0x00, 0x01, 0x4D, 0x58, 0x31, 0x38, 0x30, 0x39, 0x30, 0x39}));
        //F3 10 FD 00 00 01 55 40 55 52 01   00 13 A2 00 40 01 02 03 siza 19
        //F3 10 FD 00 00 01 55 52 01
        try {
            byte[] preConf=new byte[]{(byte) 0xF3, 0x00, (byte) 0xFD, 0x00, 0x00, 0x01, 0x4D, 0x58, 0x31, 0x38, 0x30, 0x39, 0x30, 0x39};

            byte[] setConfiguration = new byte[19];
            byte[] addressScreen = mG2Bee.getLocalXBeeDevices().get(0).getXbee64BitAddress().getValue();

            System.arraycopy(new byte[]{
                    (byte) 0xF3, 0x10, (byte) 0xFD, 0x00, 0x00, 0x01 , 0x55 ,(byte) 0x40
            }, 0, setConfiguration, 0, 8);

            System.arraycopy(addressScreen, 0, setConfiguration, 8, 8);

            System.arraycopy(new byte[]{
                    0x55, 0x52, 0x01
            }, 0, setConfiguration, 16, 3);

            Log.e(TAG,setConfiguration.toString());


            mG2Bee.sendData(mGlobal.getEvseList()[0].getAddress(), preConf);
            mG2Bee.sendData(mGlobal.getEvseList()[1].getAddress(), preConf);

            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mG2Bee.sendData(mGlobal.getEvseList()[0].getAddress(), setConfiguration);
            mG2Bee.sendData(mGlobal.getEvseList()[1].getAddress(), setConfiguration);


        }  catch (XBeeException e) {
            e.printStackTrace();
        }
    }

    private int getUiState(int []data){
        return data[6];

    }


    public String getTAG(int []data,int startIndex, int lengthTag){
        String tag="";

           for(int i=0;i<lengthTag;i++){
                tag=tag+" " +Integer.toHexString(data[i+startIndex]);
            }

        return toUpperCase(tag);
    }

    /*
          01 00 20 04 00 CE AE 0B AB
          setproperty : 4D 58 31 38 30 39 30 39 43 45 01
     */
}
