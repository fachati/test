package com.g2mobility.status;

import java.util.Date;

/**
 * Created by fachati on 18/04/16.
 */
public class G2EvseSession {

    private String tag;
    public int id;
    private Date startTime;
    private int protocole;

    G2EvseSession( int id, String tag, int protocole, Date date ) {
        this.id = id;
        this.tag = tag;
        this.protocole=protocole;
        this.startTime=date;
    }

    @Override
    public String toString() {
        return "G2EvseSession{" +
                "tag='" + tag + '\'' +
                ", id=" + id +
                ", startTime=" + startTime +
                ", protocole=" + protocole +
                '}';
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProtocole() {
        return protocole;
    }

    public void setProtocole(int protocole) {
        this.protocole = protocole;
    }
}