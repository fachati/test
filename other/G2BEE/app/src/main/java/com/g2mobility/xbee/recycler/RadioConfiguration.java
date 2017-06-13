package com.g2mobility.xbee.recycler;


import android.os.Parcel;
import android.os.Parcelable;

/**
 * Representation of a XBee AT radio configuration.
 *
 * @author Hanyu Li
 */
public class RadioConfiguration implements Parcelable {

    /**
     * The parameter.
     */
    private String mParameter;
    /**
     * The radio configuration value. {@code null} if not read or unknown.
     */
    private byte[] mValue;

    public RadioConfiguration(String parameter) {
        this(parameter, null);
    }

    public RadioConfiguration(String parameter, byte[] value) {
        mParameter = parameter;
        mValue = value;
    }

    public String getParameter() {
        return mParameter;
    }

    public byte[] getValue() {
        return mValue;
    }

    public void setValue(byte[] value) {
        mValue = value;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mParameter);
        if (mValue != null) {
            dest.writeByte((byte) 1);
            dest.writeByteArray(mValue);
        } else {
            dest.writeByte((byte) 0);
        }
    }

    public static final Creator CREATOR = new Creator() {
        @Override
        public RadioConfiguration createFromParcel(Parcel in) {
            return new RadioConfiguration(in);
        }

        @Override
        public RadioConfiguration[] newArray(int size) {
            return new RadioConfiguration[size];
        }
    };

    private RadioConfiguration(Parcel in) {
        mParameter = in.readString();
        if (in.readByte() > 0) {
            mValue = in.createByteArray();
        }
    }

}
