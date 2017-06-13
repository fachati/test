package com.g2mobility.status;


public enum G2EvseStatus {

    UNKNOWN                 (0xFF),
    FAIL                    (0x99),
    OFF                     (0x98),

    FINISHING               (0x08),
    IDLE                    (0x00),
    PREPARING               (0x07),
    EVNOTCONNECTED          (0x06),
    EVCONNECTED             (0x02),
    PRECHARGING             (0x05),
    CHARGING                (0x04);

    private int code;

    G2EvseStatus(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseStatus fromCode(int code) {
        for (G2EvseStatus message : G2EvseStatus.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return FAIL;
    }
}
