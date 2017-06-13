package com.g2mobility.status;


public enum G2EvseError {

    UNKNOWN                             (0x00),
    REL_FAIL                            (0x01),//
    LINE_FAIL                           (0x02),//
    PILOT_ERR                           (0x04),
    OVERCONS                            (0x08),
    NEUTRAL_FAIL                        (0x10),//
    UNDER_VOLT                          (0x20),
    METER_FAIL                          (0X40),//
    RFID_FAIL                           (0x80);//




    private int code;

    G2EvseError(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseError fromCode(int code) {
        for (G2EvseError message : G2EvseError.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return UNKNOWN;
    }
}

