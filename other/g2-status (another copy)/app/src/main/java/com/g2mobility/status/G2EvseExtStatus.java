package com.g2mobility.status;

public enum G2EvseExtStatus {

    NEED_CH                            (0x01),
    PH_EV                              (0x10),
    DOOR_OPENED                        (0x20),
    RESERVED                           (0x02),
    BOOT                               (0x08);

    private int code;

    G2EvseExtStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseExtStatus fromCode(int code) {
        for (G2EvseExtStatus message : G2EvseExtStatus.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return BOOT;
    }
}
