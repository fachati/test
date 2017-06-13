package com.g2mobility.xbee.recycler;


import com.digi.xbee.api.models.XBee64BitAddress;

import java.util.Date;

public class XBeeConversation {

    private XBee64BitAddress mAddress;
    private String mType;
    private byte[] mMessage;
    private Date mTime;

    public XBee64BitAddress getAddress() {
        return mAddress;
    }

    public void setAddress(XBee64BitAddress address) {
        this.mAddress = address;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
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
