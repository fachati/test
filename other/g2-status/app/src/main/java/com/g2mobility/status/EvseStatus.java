package com.g2mobility.status;

/**
 * Created by fachati on 21/03/17.
 */

public class EvseStatus {

    private int id;
    private String textL1;
    private String textL2;
    private G2EvseLevel level;



    public EvseStatus(int id, G2EvseLevel level, String textL1,String textL2) {
        this.id = id;
        this.textL1 = textL1;
        this.level = level;
        this.textL2 = textL2;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTextL1() {
        return textL1;
    }

    public void setTextL1(String textL1) {
        this.textL1 = textL1;
    }

    public String getTextL2() {
        return textL2;
    }

    public void setTextL2(String textL2) {
        this.textL2 = textL2;
    }

    public G2EvseLevel getLevel() {
        return level;
    }

    public void setLevel(G2EvseLevel level) {
        this.level = level;
    }

    @Override
    public String toString() {
        return "EvseStatus{" +
                "id='" + id + '\'' +
                ", textL1='" + textL1 + '\'' +
                ", textL2='" + textL2 + '\'' +
                ", level=" + level +
                '}';
    }
}
