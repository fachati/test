package com.g2mobility.status;

import android.content.Context;

public enum G2EvseResultStatus {


    RESULT_1(new Status(G2EvseStatus.FAIL,null),new TextResult(getContext().getString(R.string.text_html_FAIL2),getContext().getString(R.string.text_html_FAIL2))),
    RESULT_2(new Status(G2EvseStatus.OFF,null),new TextResult(getContext().getString(R.string.text_html_OFF2),getContext().getString(R.string.text_html_OFF2))),

    RESULT_3(new Status(G2EvseStatus.FAIL,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_FAIL2),getContext().getString(R.string.text_html_FAIL2))),
    RESULT_4(new Status(G2EvseStatus.OFF,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_OFF2),getContext().getString(R.string.text_html_OFF2))),

    RESULT_5(new Status(G2EvseStatus.IDLE,null),new TextResult(getContext().getString(R.string.text_html_IDLE_FALSE),getContext().getString(R.string.text_html_IDLE_FALSE2))),
    RESULT_6(new Status(G2EvseStatus.PREPARING,null),new TextResult(getContext().getString(R.string.text_html_IDLE_FALSE),getContext().getString(R.string.text_html_IDLE_FALSE2))),
    RESULT_7(new Status(G2EvseStatus.EVNOTCONNECTED,null),new TextResult(getContext().getString(R.string.text_html_ENC_FALSE2),getContext().getString(R.string.text_html_ENC_FALSE2))),
    RESULT_8(new Status(G2EvseStatus.EVCONNECTED,null),new TextResult(getContext().getString(R.string.text_html_EC_FALSE2),getContext().getString(R.string.text_html_EC_FALSE2))),
    RESULT_9(new Status(G2EvseStatus.PRECHARGING,null),new TextResult(getContext().getString(R.string.text_html_PRECHARGING_FALSE),getContext().getString(R.string.text_html_PRECHARGING_FALSE))),
    RESULT_10(new Status(G2EvseStatus.CHARGING,null),new TextResult(getContext().getString(R.string.text_html_CHARGING_FALSE),getContext().getString(R.string.text_html_CHARGING_FALSE))),
    RESULT_11(new Status(G2EvseStatus.FINISHING,null),new TextResult(getContext().getString(R.string.text_html_FINISHING_FALSE),getContext().getString(R.string.text_html_FINISHING_FALSE2))),

    RESULT_12(new Status(G2EvseStatus.IDLE,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_IDLE_TRUE2),getContext().getString(R.string.text_html_IDLE_TRUE2))),
    RESULT_13(new Status(G2EvseStatus.PREPARING,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_test_PREPARING_TRUE),getContext().getString(R.string.text_test_PREPARING_TRUE2))),
    RESULT_14(new Status(G2EvseStatus.EVNOTCONNECTED,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_ENC_TRUE),getContext().getString(R.string.text_html_ENC_TRUE2))),
    RESULT_15(new Status(G2EvseStatus.EVCONNECTED,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_EC_TRUE),getContext().getString(R.string.text_html_EC_TRUE2))),
    RESULT_16(new Status(G2EvseStatus.PRECHARGING,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_PRECHARGING_TRUE),getContext().getString(R.string.text_html_PRECHARGING_TRUE2))),
    RESULT_17(new Status(G2EvseStatus.CHARGING,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_PRECHARGING_TRUE),getContext().getString(R.string.text_html_PRECHARGING_TRUE2))),
    RESULT_18(new Status(G2EvseStatus.FINISHING,new G2EvseSession(0,"",0,null)),new TextResult(getContext().getString(R.string.text_html_FINISHING_TRUE),getContext().getString(R.string.text_html_FINISHING_TRUE2)));

    private Status status;
    private TextResult result;
    /*private static String htmlLigne1="<h2><font color=\"#FFFFFF\">"+"####"+"</font></h2>";
    private static String htmlLigne2="<p><font color=\"#FFFFFF\">"+"####"+"</font></p>";
    private static String htmlLigne3="####";*/

    private static String htmlLigne1="####";
    private static String htmlLigne2="####";
    private static String htmlLigne3="####";



    public static Context getContext(){
        return GlobalClass.getAppContext();
    }


    public Status getStatus() {
        return status;
    }


    public void setStatus(Status status) {
        this.status = status;
    }

    public TextResult getResult() {
        return result;
    }


    @Override
    public String toString() {
        return "G2EvseResultStatus{" +
                "status=" + status +
                ", result=" + result +
                '}';
    }

    G2EvseResultStatus(Status status, TextResult result) {
        this.status = status;
        this.result = result;
    }

    public static TextResult updateLigne3(Status status, G2EvseResultStatus evseResult){

        TextResult result=new TextResult("","","");
        if(status.isStatus(G2EvseExtStatus.RESERVED)){
            result.setLigne1("Borne réservée");

        }else{
            result.setLigne1(evseResult.getResult().getLigne1());
            result.setLigne2(evseResult.getResult().getLigne2());
        }

        if(status.isAuthInProgress()) {
            result.setLigne3(getContext().getString(R.string.text_info_Autorisation ) + status.getAuthTag());

        }else if(status.isStatus(G2EvseExtStatus.DOOR_OPENED) && (status.getStatut() == G2EvseStatus.IDLE || status.getStatut() == G2EvseStatus.FINISHING)){
            result.setLigne3(getContext().getString(R.string.text_info_Debranche));

        }else if(status.isStatus(G2EvseExtStatus.DOOR_OPENED) && (status.getStatut() == G2EvseStatus.PREPARING || status.getStatut() == G2EvseStatus.EVCONNECTED)){
            result.setLigne3(getContext().getString(R.string.text_info_Branche));

        }else
            result.setLigne3("");

        return result;
    }

    //TODO : ver reservation
    public static TextResult getResultFromStatus(Status status) {

        String l1 = "",l2 = "",l3 = "";
        for (G2EvseResultStatus s : G2EvseResultStatus.values()) {

            if(s.getStatus().getStatut() == status.getStatut() ){
                if(s.getStatus().getSession() != null && status.getSession()!=null) {
                    TextResult result=updateLigne3(status,s);
                    l1=result.getLigne1();
                    l2=result.getLigne2();
                    l3=result.getLigne3();

                }else if(s.getStatus().getSession() == null && status.getSession() == null) {
                    TextResult result=updateLigne3(status,s);
                    l1=result.getLigne1();
                    l2=result.getLigne2();
                    l3=result.getLigne3();
                }
            }
        }
        return setHtmlInResult(new TextResult(l1,l2,l3));
    }

    public static TextResult setHtmlInResult(TextResult result){
        result.setLigne1(htmlLigne1.replace("####",result.getLigne1()));
        result.setLigne2(htmlLigne2.replace("####",result.getLigne2()));
        result.setLigne3(htmlLigne3.replace("####",result.getLigne3()));
        return result;
    }

    public static boolean isStartTimer(Status status) {

        return (status.isStatus(G2EvseStatus.IDLE) && !status.isStatus(G2EvseExtStatus.DOOR_OPENED))
                || status.isStatus(G2EvseStatus.CHARGING)
                || (status.isStatus(G2EvseStatus.PREPARING) && status.getSession()==null && status.isPreparingInSession())
                || (status.isStatus(G2EvseStatus.EVCONNECTED) && !status.isStatus(G2EvseExtStatus.DOOR_OPENED))
                || status.isStatus(G2EvseStatus.PRECHARGING);
    }

    public static boolean isStartStopTimerCentral(Status status) {

        return status.isStatus(G2EvseStatus.EVNOTCONNECTED) || status.isStatus(G2EvseStatus.FINISHING)
                || (status.isStatus(G2EvseStatus.PREPARING));
    }

    public static boolean isIDLE(Status status) {

        return status.isStatus(G2EvseStatus.IDLE);
    }
}



