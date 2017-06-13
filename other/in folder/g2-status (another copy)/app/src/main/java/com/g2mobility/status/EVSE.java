package com.g2mobility.status;

import com.digi.xbee.api.models.XBee64BitAddress;

public class EVSE {

    private XBee64BitAddress address;
    private String mBorne;

    public EVSE(XBee64BitAddress address, String mBorne) {
        this.address = address;
        this.mBorne = mBorne;
    }

    public XBee64BitAddress getAddress() {
        return address;
    }

    public String getmBorne() {
        return mBorne;
    }

    @Override
    public String toString() {
        return "EVSE{" +
                "address=" + address +
                ", mBorne='" + mBorne + '\'' +
                '}';
    }
}
