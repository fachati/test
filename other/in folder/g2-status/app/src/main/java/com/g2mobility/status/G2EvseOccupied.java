package com.g2mobility.status;


public enum G2EvseOccupied {

    SANSPRISE                               (0x00),
    P1OCCUPIED                              (0x01),
    P2OCCUPIED                              (0x02),
    OCCUPIED                                (0x03);

    private int code;

    G2EvseOccupied(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static G2EvseOccupied fromCode(int code) {
        for (G2EvseOccupied message : G2EvseOccupied.values()) {
            if (message.getCode() == code) {
                return message;
            }
        }
        return OCCUPIED;
    }
}