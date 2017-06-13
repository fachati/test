package com.g2mobility.status;

import java.util.Date;

public class Status {
    private G2EvseStatus statut;
    private G2EvseSession session;
    private int error;
    private int extStatus;
    private String evseName;
    private String authInProgress = null;
    private boolean isAuth=false;
    private boolean isPreparingInSession=false;

    public Status(String evseName) {
        this.evseName = evseName;
        this.statut = G2EvseStatus.UNKNOWN;
        extStatus = 0;
        error = 0;
        session = null;
    }

    public boolean isAuth() {
        return isAuth;
    }

    public void setAuth(boolean auth) {
        isAuth = auth;
    }

    public Status(G2EvseStatus statut, G2EvseSession session) {
        this.statut = statut;
        this.session = session;
    }

    public boolean isPreparingInSession() {
        return isPreparingInSession;
    }

    public void setPreparingInSession(boolean preparingInSession) {
        isPreparingInSession = preparingInSession;
    }

    @Override
    public String toString() {
        return "Status{" +
                "statut=" + statut +
                ", session=" + session +
                ", error=" + error +
                ", extStatus=" + extStatus +
                ", evseName='" + evseName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Status)) return false;

        Status status = (Status) o;

        if (error != status.error) return false;
        if (extStatus != status.extStatus) return false;
        if (statut != status.statut) return false;
        if (session != status.session) return false;

        return true;

    }

    @Override
    public int hashCode() {
        int result = statut != null ? statut.hashCode() : 0;
        result = 31 * result + (session != null ? session.hashCode() : 0);
        result = 31 * result + error;
        result = 31 * result + extStatus;
        result = 31 * result + (evseName != null ? evseName.hashCode() : 0);
        return result;
    }



    public boolean isAuthInProgress() {
        if(authInProgress!=null)
            return true;
        else
            return false;
    }

    public void setAuthInProgress( String aip ) {
        this.authInProgress = aip;
    }

    public void setSession(int sessionId, String tag, int protocole, Date time) {


        if ( sessionId == -1 )
            this.session = null;
        else if ( this.session == null )
            this.session = new G2EvseSession(sessionId, tag, protocole, time);
        else {
            this.session.setId(sessionId);
            this.session.setTag(tag);
            this.session.setProtocole(protocole);
            this.session.setStartTime(time);
        }
    }

    public G2EvseStatus getStatut() {
        return statut;
    }



    public String getEvseName() {
        return evseName;
    }



    public G2EvseSession getSession() {
        return session;
    }


    public void setInformationPing(G2EvseStatus statut, int error, int ext_status) {
        this.statut = statut;
        this.error=error;
        this.extStatus=ext_status;
    }

    public boolean isStatus(G2EvseStatus extStatus){
        return this.statut == extStatus;
    }

    public boolean isStatus(G2EvseExtStatus extStatus){
        return (this.extStatus & extStatus.getCode()) != 0;
    }

    public boolean isError(G2EvseError error){
        return (this.error & error.getCode()) != 0;
    }

    public boolean isEvseHorsService(){
        if(statut == G2EvseStatus.FAIL || isError(G2EvseError.LINE_FAIL)
                || isError(G2EvseError.REL_FAIL) || isError(G2EvseError.NEUTRAL_FAIL)
                || isError(G2EvseError.METER_FAIL) || isError(G2EvseError.RFID_FAIL))
            return true;
        return false;
    }

    public boolean isEvseEnPanne(){
        if(statut == G2EvseStatus.FAIL)
            return true;
        return false;
    }

    public String getAuthTag() {
        return authInProgress;
    }
}

