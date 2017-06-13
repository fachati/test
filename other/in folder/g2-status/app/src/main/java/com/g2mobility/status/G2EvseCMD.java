package com.g2mobility.status;


public enum G2EvseCMD {


    UNKNOWN                             (0x0),
    PING                                (0x1),
    AUTH                                (0x2),
    START                               (0x3),
    STOP                                (0x4);


    private int code;

    G2EvseCMD(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseCMD fromCode(int code) {
        for (G2EvseCMD message : G2EvseCMD.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return UNKNOWN;
    }
}