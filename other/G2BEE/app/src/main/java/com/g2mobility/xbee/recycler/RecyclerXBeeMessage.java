package com.g2mobility.xbee.recycler;


import java.util.Date;

public class RecyclerXBeeMessage {

    private boolean mIsOutgoing;
    private byte[] mMessage;
    private Date mTime;

    public boolean isOutgoing() {
        return mIsOutgoing;
    }

    public void setIsOutgoing(boolean isOutgoing) {
        this.mIsOutgoing = isOutgoing;
    }

    public byte[] getMessage() {
        return mMessage;
    }

    public void setMessage(byte[] message) {
        this.mMessage = message;
    }

    public Date getTime() {
        return mTime;
    }

    public void setTime(Date time) {
        this.mTime = time;
    }

}
