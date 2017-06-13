package com.g2mobility.status;


import android.Manifest;
import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.CountDownTimer;
import android.os.PowerManager;
import android.os.StrictMode;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.g2mobility.status.tab.AutoResizeTextView;
import com.g2mobility.status.tab.NavigationTabBar;

import java.util.ArrayList;
import java.util.Date;

/*Status                  Session                                           Ligne 1                                 Ligne 2

99 : FAIL                                                                   Borne hors service                      Borne hors service

98 : OFF                                                                    Borne en maintenance                    Borne en maintenance

0 : IDLE                null                                                Borne libre                             Veuillez badger pour vous recharger

7 : PREPARING           null                                                Borne libre                             Veuillez badger pour vous recharger

6 : EV  NOT CONNECTED   null                                                non prévu EV NOT CONNECTED wo SESSION   non prévu EV NOT CONNECTED wo SESSION

2 : EV CONNECTED        null                                                non prévu EV CONNECTED wo SESSION       non prévu EV CONNECTED wo SESSION

5 : PRECHARGING         null                                                non prévu PRECHARGING wo SESSION        non prévu PRECHARGING wo SESSION

4 : CHARGING            null                                                non prévu CHARGING wo SESSION           non prévu CHARGING wo SESSION

8 : FINISHING           null                                                Session de charge terminée              Veuillez débrancher et fermer la porte

0 : IDLE                not null                                            non prévu IDLE W SESSION                non prévu IDLE W SESSION

7 : PREPARING           not null                                            Utilisateur autorisé                    Veuillez ouvrir la porte et brancher

6 : EV  NOT CONNECTED   not null                                            Utilisateur autorisé                    Veuillez brancher le véhicule

2 : EV CONNECTED        not null                                            En attente de demande de charge du véhicule Veuillez badger pour terminer la session

5 : PRECHARGING         not null                                            Charge en cours                         Veuillez badger pour terminer la session

4 : CHARGING            not null                                            Charge en cours                         Veuillez badger pour terminer la session

8 : FINISHING           not null                                            non prévu FINISHING W SESSION           non prévu FINISHING W SESSION*/



public class WelcomeActivity extends Activity {


    protected PowerManager.WakeLock mWakeLock;

    private GlobalClass mGlobal =null;

    private NavigationTabBar mNavigationTabBar;


    // mLayoutCenter : page centre
    // mLayoutPC : page de point de charge
    private LinearLayout mLayoutCenter, mLayoutPC;

    private Status []mCPStatus;             //tableau de borne[gauche , droite]
    private CountDownTimer mTimerCentral;   //timer central (15 seconde) pour revenir a la page de centre
    private CountDownTimer mTimerAuth;      // timer d'authentification (8 seconde)

    private AutoResizeTextView textStatusB1,textStatusB2;   // textview dans la page de centre (status de chaque borne)

    private TextView textStatus,textBorneDirection,textCommentaire;
    private ImageView imageArrowG,imageArrowD;
    private LinearLayout layoutProgress;

    private G2EvseCMD cmd;                                  // cmd : ping,auth,start,stop

    private WebView webView;                                // webview pour afficher la progress bar
    private String L1,L2,L3;
    int ecran;                                              // -1 : page central , 0 : gauche , 1 : droite


    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }



    // modifier les information de session
    public void MAJInformation(Intent intent){

        XBee64BitAddress rAddress=hexStringToXBeeAddress(intent.getStringExtra("address"));
        int indexTab=getindexFromXbeeAddress(rAddress);
        String rCMD=intent.getStringExtra("cmd");           // cmd : PING || AUTH || START || STOP
        int status=intent.getIntExtra("status",-1);         // status de la borne
        int rExtStatus=intent.getIntExtra("ext_status",-1); // ext status : DOOR OPENED || BOOT || ...
        int rError=intent.getIntExtra("error_status",-1);
        int rSessionID=intent.getIntExtra("sessionID",-1);
        int proto=intent.getIntExtra("proto",-1);
        String rTag=intent.getStringExtra("tag");
        Date time = new Date();
        time.setTime(intent.getLongExtra("time", -1));


        Status rStatus=mCPStatus[indexTab];

        G2EvseCMD cmd=G2EvseCMD.valueOf(rCMD);

        if(cmd.equals(G2EvseCMD.PING)){
            rStatus.setInformationPing(G2EvseStatus.fromCode(status),rError,rExtStatus);
            rStatus.setSession(rSessionID, rTag, proto, time);//time in start session



        }else if(cmd.equals(G2EvseCMD.AUTH)){
            rStatus.setAuth(true);
            rStatus.setAuthInProgress( rTag );

        }else if(cmd.equals(G2EvseCMD.START)){
            rStatus.setSession(rSessionID, rTag, proto, time);

        }else if(cmd.equals(G2EvseCMD.STOP)){
            rStatus.setSession(rSessionID, rTag, proto, time);
        }
    }



    public void MAJTab(int indexTab){
        Log.e("information",indexTab+"--");

        if(indexTab==0 && indexTab==ecran){

            imageArrowD.setVisibility(View.INVISIBLE);
            imageArrowG.setVisibility(View.VISIBLE);
            textBorneDirection.setText("Point de charge gauche");

        }else if(indexTab==1 && indexTab==ecran){
            imageArrowD.setVisibility(View.VISIBLE);
            imageArrowG.setVisibility(View.INVISIBLE);
            textBorneDirection.setText("Point de charge Droite");
        }

        MAJ(indexTab);

    }

    public void MAJInfoCenter(int indexTab){

        if(indexTab==0){
            textStatusB1.setTextColor(getResources().getColor(R.color.white));
            if(mCPStatus[indexTab].isEvseHorsService()){
                textStatusB1.setText("HORS\nSERVICE");
                textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderpink));
            }else if(mCPStatus[indexTab].isEvseEnPanne()){
                textStatusB1.setText("EN\nPANNE");
                textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderred));
            }else if(mCPStatus[indexTab].getSession()!=null){
                textStatusB1.setText("OCCUPE");
                textStatusB1.setBackground(getResources().getDrawable(R.drawable.borderbleu));
            }else if(mCPStatus[indexTab].getSession()==null){
                textStatusB1.setText("LIBRE");
                textStatusB1.setBackground(getResources().getDrawable(R.drawable.bordergreen));
            }


        }else if(indexTab==1){
            textStatusB2.setTextColor(getResources().getColor(R.color.white));
            if(mCPStatus[indexTab].isEvseHorsService()){
                textStatusB2.setText("HORS\nSERVICE");
                textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderpink));
            }else if(mCPStatus[indexTab].isEvseEnPanne()){
                textStatusB2.setText("EN\nPANNE");
                textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderred));
            }else if(mCPStatus[indexTab].getSession()!=null){
                textStatusB2.setText("OCCUPE");
                textStatusB2.setBackground(getResources().getDrawable(R.drawable.borderbleu));
            }else if(mCPStatus[indexTab].getSession()==null){
                textStatusB2.setText("LIBRE");
                textStatusB2.setBackground(getResources().getDrawable(R.drawable.bordergreen));
            }
        }
    }

    public void MAJ(int indexTab){

        if (cmd == G2EvseCMD.AUTH){
            setAutorisationEnCours();

        }else if(cmd == G2EvseCMD.PING){

            if(mCPStatus[indexTab].getSession()!=null){
                if(mCPStatus[indexTab].getStatut() == G2EvseStatus.EVNOTCONNECTED){
                    L1="";
                    L2="";
                    L3="branchez votre véhicule";
                    if(indexTab==ecran)
                        setSessionDemaree(L1,L2,L3);
                }else if(mCPStatus[indexTab].getStatut() == G2EvseStatus.EVNOTCONNECTED && mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED)){
                    L1="";
                    L2="Refermer la porte\n";
                    L3="branchez votre véhicule";
                    if(indexTab==ecran)
                        setSessionDemaree(L1,L2,L3);
                }else if(mCPStatus[indexTab].getStatut() == G2EvseStatus.EVCONNECTED && mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED)){
                    L1="";
                    L2="Refermer la porte\n";
                    L3="";
                    if(indexTab==ecran)
                        setSessionDemaree(L1,L2,L3);
                }else if (mCPStatus[indexTab].getStatut() == G2EvseStatus.PRECHARGING || mCPStatus[indexTab].getStatut() == G2EvseStatus.CHARGING
                        || (mCPStatus[indexTab].getStatut() == G2EvseStatus.EVCONNECTED && !mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED))){
                    if(indexTab==ecran)
                        setRechargeEnCours();
                }
            }



            if(mCPStatus[indexTab].getSession()==null && !mCPStatus[indexTab].isAuth()){
                if(mCPStatus[indexTab].getStatut() == G2EvseStatus.FINISHING){
                    L1="Ouvrez la porte et débranchez votre câble\n";
                    L2="Refermer la porte";
                    if(indexTab==ecran)
                        setSessionTerminée(L1,L2);
                }else if(mCPStatus[indexTab].getStatut() == G2EvseStatus.IDLE && mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED)){
                    L2="Refermer la porte";
                    L1="";
                    if(indexTab==ecran)
                        setSessionTerminée(L1,L2);
                }
            }

        }
    }



    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getAction().equals(g2kService.ACTION_SEND_STATUS)) {


                Log.e("receivedInformation","CMD : "+intent.getStringExtra("cmd")+"\nTAG : "+intent.getStringExtra("tag")+"\nStatus : "+G2EvseStatus.fromCode(intent.getIntExtra("status",-1))+
                        "\next_status : "+getEXT_STATUS(intent.getIntExtra("ext_status",-1))+"\nerror_status : "+intent.getIntExtra("error_status",-1)+"\nsessionID : "+intent.getIntExtra("sessionID",-1));


                XBee64BitAddress rAddress=hexStringToXBeeAddress(intent.getStringExtra("address"));
                int indexTab=getindexFromXbeeAddress(rAddress);
                MAJInformation(intent);

                cmd = G2EvseCMD.valueOf(intent.getStringExtra("cmd"));
                MAJTab( indexTab );//0-1
                MAJInfoCenter(indexTab);


                if (cmd ==  G2EvseCMD.AUTH || cmd == G2EvseCMD.START || cmd == G2EvseCMD.STOP) {
                    setTab(indexTab + 1);
                    ecran=indexTab;
                }

                if (cmd == G2EvseCMD.AUTH){
                    startTimerAuthentification(indexTab);
                }


                if (cmd == G2EvseCMD.PING || (G2EvseResultStatus.isStartStopTimerCentral(mCPStatus[indexTab])) || cmd == G2EvseCMD.AUTH || cmd == G2EvseCMD.STOP || cmd == G2EvseCMD.START) {
                    if (G2EvseResultStatus.isStartTimer(mCPStatus[indexTab]))
                        startTimer(10000);
                    else if (mTimerCentral != null ) {
                       stopTimerCentral();
                    }
                }

                if((G2EvseResultStatus.isIDLE(mCPStatus[indexTab])) && !mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED) && ecran==indexTab ){
                    Log.e("center","center1");
                    if(!mCPStatus[indexTab].isAuth()){
                            stopTimerAuth();
                            stopTimerCentral();
                            if(mNavigationTabBar.getModelIndex()!=0 ){
                                mNavigationTabBar.setModelIndex(0);
                                setCenterTab();
                            }
                    }
                }

                mCPStatus[indexTab].setPreparingInSession(false);

            }
        }
    };

    public void stopTimerCentral(){
        if (mTimerCentral != null ) {
            mTimerCentral.cancel();
            mTimerCentral = null;
        }
    }

    public void stopTimerAuth(){
        if(mTimerAuth !=null) {
            mTimerAuth.cancel();
            mTimerAuth = null;
        }
    };


    public XBee64BitAddress hexStringToXBeeAddress(String s) {
        String address=s.replace("0x" ,"").replace(","," ");
        return new XBee64BitAddress(address);
    }


    public int getindexFromXbeeAddress(XBee64BitAddress address64){
        for(int i=0;i<mGlobal.getEvseList().length;i++){
            if(mGlobal.getEvseList()[i].getAddress().equals(address64))
                return i;
        }
        return -1;
    }

    public void startTimer(int timeMilliSecond){

        if(mNavigationTabBar.getModelIndex()!=0){
            if(mTimerCentral !=null)
                return;

            Log.e("timerCentral","start");
            mTimerCentral = new CountDownTimer(timeMilliSecond,100){
                public void onTick(long millisUntilFinished){

                }
                public void onFinish(){
                    if(mNavigationTabBar.getModelIndex()!=0){
                        mNavigationTabBar.setModelIndex(0);
                            setCenterTab();
                    }
                    Log.e("finish","finish");
                }
            }.start();
        }
    }



    public void startTimerAuthentification(final int indexTab){
            Log.e("startTimerAnnim","startTimerAnnim");

            mTimerAuth =null;
            if(mTimerAuth !=null) {
                mTimerAuth.cancel();
                mTimerAuth = null;
            }


        mTimerAuth = new CountDownTimer(9000,100){

            public void onTick(long millisUntilFinished){

            }


            public void onFinish(){
                mCPStatus[indexTab].setAuthInProgress(null);
                mCPStatus[indexTab].setAuth(false);

                if(mCPStatus[indexTab].getSession()!=null){
                    Log.e("sessionID******************succes",mCPStatus[indexTab].toString());//isAuth=false;


                }else if(mCPStatus[indexTab].getSession()==null && (!mCPStatus[indexTab].isStatus(G2EvseStatus.IDLE)) ){//&&  !mCPStatus[indexTab].isStatus(G2EvseExtStatus.DOOR_OPENED
                    Log.e("sessionID******************ouvrer la porte","yes");//isAuth=false;
                    L1="Ouvrez la porte et branchez le câble (à fond)\n";
                    L2="Refermer la porte\n";
                    L3="branchez votre véhicule";
                    mCPStatus[indexTab].setPreparingInSession(true);
                    if(indexTab==ecran)
                        setSessionDemaree(L1,L2,L3);
                }else{
                    Log.e("sessionID******************failed",mCPStatus[indexTab].toString());//isAuth=false;
                    stopTimerAuth();
                    stopTimerCentral();
                    if(mNavigationTabBar.getModelIndex()!=0){
                        mNavigationTabBar.setModelIndex(0);
                        setCenterTab();
                    }
                }
            }
        }.start();
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        if (Build.VERSION.SDK_INT > 9 && getActionBar()!=null) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
            ActionBar actionBar = getActionBar();
            actionBar.setBackgroundDrawable(new ColorDrawable(Color.rgb(0,153,204)));
        }
        verifyStoragePermissions(this);

        final PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        this.mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        this.mWakeLock.acquire();



        if(Build.VERSION.SDK_INT > 20 && this.getActionBar()!=null){
            WelcomeActivity.this.getActionBar().setElevation(0);
            WelcomeActivity.this.getActionBar().setTitle("");
        }

        mGlobal = (GlobalClass) getApplicationContext();

        Intent intent = new Intent(this, g2kService.class);
        startService(intent);

        IntentFilter filter = new IntentFilter();
        filter.addAction(g2kService.ACTION_SEND_STATUS);
        filter.addAction(g2kService.ACTION_START);
        registerReceiver(mReceiver, filter);



        ecran=-1;
        mCPStatus=new Status[mGlobal.getEvseList().length]; //initialisation du tab de status

        for(int i=0;i<mGlobal.getEvseList().length;i++){
            mCPStatus[i]=new Status(mGlobal.getEvseList()[i].getmBorne());
            Log.e("borne "+i,mGlobal.getEvseList()[i].getmBorne());
        }


        initUI();

        Intent testBlink = new Intent(this, g2kService.class);
        testBlink.setAction(g2kService.ACTION_START);
        startService(testBlink);

    }



    private void initUI() {

        Typeface font = Typeface
                .createFromAsset(getAssets(), "Calibri.ttf");
        textStatusB1=(AutoResizeTextView)findViewById(R.id.textViewStatusB1);textStatusB1.setTypeface(font);
        textStatusB2=(AutoResizeTextView)findViewById(R.id.textViewStatusB2);textStatusB2.setTypeface(font);
        mNavigationTabBar = (NavigationTabBar) findViewById(R.id.ntb_horizontal);
        mLayoutCenter=(LinearLayout)findViewById(R.id.layoutCenter);
        mLayoutPC =(LinearLayout)findViewById(R.id.layoutPC);

        textStatus=(TextView)findViewById(R.id.textStatus);textStatus.setTypeface(font);
        textBorneDirection=(TextView)findViewById(R.id.textBorneDirection);textBorneDirection.setTypeface(font);
        textCommentaire=(TextView)findViewById(R.id.textCommentaire);textCommentaire.setTypeface(font);
        imageArrowG=(ImageView)findViewById(R.id.imageViewG);
        imageArrowD=(ImageView)findViewById(R.id.imageViewD);
        layoutProgress=(LinearLayout)findViewById(R.id.layoutProgress);;

        webView=(WebView)findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        webView.getSettings().setDomStorageEnabled(true);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.loadUrl("file:///android_asset/html.html");



        final ArrayList<NavigationTabBar.Model> models = new ArrayList<>();


        models.add(new NavigationTabBar.Model(
                getResources().getDrawable(R.drawable.ic_center), getResources().getColor(R.color.colorg2Bleu), "CENTER"));


        for(int i=0;i<mGlobal.getEvseList().length;i++){
            models.add(new NavigationTabBar.Model(
                    getResources().getDrawable(R.drawable.ic_left), getResources().getColor(R.color.colorg2BleuLight), mGlobal.getEvseList()[i].getmBorne()));

        }


        textStatusB1.setBackground(getResources().getDrawable(R.drawable.bordergreen));
        textStatusB2.setBackground(getResources().getDrawable(R.drawable.bordergreen));

        mNavigationTabBar.setModels(models);
        mNavigationTabBar.post(new Runnable() {
            @Override
            public void run() {
                final View bgNavigationTabBar = findViewById(R.id.bg_ntb_horizontal);
                bgNavigationTabBar.getLayoutParams().height = (int) mNavigationTabBar.getBarHeight();
                bgNavigationTabBar.requestLayout();
            }
        });

        mNavigationTabBar.setModelIndex(0);
        setCenterTab();


    }

    public void setTab(int indexTab){
            mNavigationTabBar.setModelIndex(indexTab);
            mLayoutCenter.setVisibility(View.INVISIBLE);
            mLayoutPC.setVisibility(View.VISIBLE);
    }

    public void setCenterTab(){
        ecran=-1;
        mLayoutCenter.setVisibility(View.VISIBLE);
        mLayoutPC.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        this.mWakeLock.release();
        super.onDestroy();
    }

    public String getEXT_STATUS(int frame4){
        String ext_status=null;
        if((frame4 & 0x01) !=0 )
            ext_status="NEED CH ";
        else if((frame4 & 0x02) !=0 ) {
            ext_status ="RESERVED ";
        }
        else if((frame4 & 0x10) !=0 )
            ext_status="3PH EV ";
        else if((frame4 & 0x20) !=0 ) {
            ext_status ="DOOR_OPENED";
        }
        else if((frame4 & 0x08) !=0 ) {
            ext_status = "BOOT ";
        }
        return ext_status;
    }


    public void setAutorisationRefusee(){
        textStatus.setText("Autorisation refusée");
        textStatus.setBackground(getResources().getDrawable(R.drawable.borderred));
        layoutProgress.setVisibility(View.GONE);
        textCommentaire.setText("");
    }

    public void setRechargeEnCours(){
        textStatus.setText("recharge en cours");
        textStatus.setBackground(getResources().getDrawable(R.drawable.bordercian));
        layoutProgress.setVisibility(View.VISIBLE);
        textCommentaire.setText("Veuillez badger pour arrêter");
    }


    public void setBadgezANouveau(){
        textStatus.setText("Badgez à nouveau");
        textStatus.setBackground(getResources().getDrawable(R.drawable.bordergreen));
        layoutProgress.setVisibility(View.GONE);
        textCommentaire.setText("Pour commencer votre session de recharge\nVeuillez badger du côté correspondant à votre stationnement");
    }

    public void setSessionTerminée(String L1,String L2){
        textStatus.setText("Session terminée");
        textStatus.setBackground(getResources().getDrawable(R.drawable.bordergreen));
        layoutProgress.setVisibility(View.INVISIBLE);
        textCommentaire.setText(L1+L2);
    }

    public void setAutorisationEnCours(){
        textStatus.setText("Autorisation en cours ...");
        textStatus.setBackground(getResources().getDrawable(R.drawable.bordergreen));
        layoutProgress.setVisibility(View.VISIBLE);
        textCommentaire.setText("Veuillez patienter");
    }

    public void setSessionDemaree(String L1,String L2,String L3){
        textStatus.setText("Session démarrée");
        textStatus.setBackground(getResources().getDrawable(R.drawable.borderbleu));
        layoutProgress.setVisibility(View.INVISIBLE);
        textCommentaire.setText(L1+L2+L3);
    }


}
