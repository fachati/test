package com.g2mobility.status;

/**
 * Created by fachati on 21/03/17.
 */

public enum G2EvseLevel {

    UNDEF(5),
    ADMIN(0),
    OFFLINE(1),

    CALIBRATION(3),

    ERROR_L2(53),
    ERROR_L1(51),

    USER_L1(2),
    USER_L2(44),

    RUN_19(19);


    private int code;

    G2EvseLevel(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseLevel fromCode(int code) {
        for (G2EvseLevel message : G2EvseLevel.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return UNDEF;
    }
}
