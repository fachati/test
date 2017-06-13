package com.g2mobility.status;

import android.content.Context;

/**
 *
 * Created by fachati on 21/03/17.
 */

public enum EvseStatusEnum {

    UNDEF                       (5,G2EvseLevel.UNDEF,getContext().getString(R.string.UNDEF_T1),getContext().getString(R.string.UNDEF_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    ADMIN_MODE                  (0,G2EvseLevel.ADMIN,getContext().getString(R.string.ADMIN_MODE_T1),getContext().getString(R.string.ADMIN_MODE_T2),EvseColorEnum.YELLOW,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    ADMIN_CMD_ACK               (41,G2EvseLevel.ADMIN,getContext().getString(R.string.ADMIN_CMD_ACK_T1),getContext().getString(R.string.ADMIN_CMD_ACK_T2),EvseColorEnum.YELLOW,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    OFFLINE_MODE                (1,G2EvseLevel.OFFLINE,getContext().getString(R.string.OFFLINE_MODE_T1),getContext().getString(R.string.OFFLINE_MODE_T2),EvseColorEnum.PINK,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    CALIBRATING_MODE            (3,G2EvseLevel.CALIBRATION,getContext().getString(R.string.CALIBRATING_MODE_T1),getContext().getString(R.string.CALIBRATING_MODE_T2),EvseColorEnum.YELLOW,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    CALIBRATION_ERROR           (4,G2EvseLevel.CALIBRATION,getContext().getString(R.string.CALIBRATION_ERROR_T1),getContext().getString(R.string.CALIBRATION_ERROR_T2),EvseColorEnum.YELLOW,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    ERROR_LINE_FAIL             (54,G2EvseLevel.ERROR_L2,getContext().getString(R.string.ERROR_LINE_FAIL_T1),getContext().getString(R.string.ERROR_LINE_FAIL_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    ERROR_SWITCH_FAILURE        (55,G2EvseLevel.ERROR_L2,getContext().getString(R.string.ERROR_SWITCH_FAILURE_T1),getContext().getString(R.string.ERROR_SWITCH_FAILURE_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    ERROR_HDW                   (53,G2EvseLevel.ERROR_L2,getContext().getString(R.string.ERROR_HDW_T1),getContext().getString(R.string.ERROR_HDW_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    ERROR_EEPROM                (56,G2EvseLevel.ERROR_L2,getContext().getString(R.string.ERROR_EEPROM_T1),getContext().getString(R.string.ERROR_EEPROM_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_Indefini)),
    ERROR_NEUTRAL               (57,G2EvseLevel.ERROR_L2,getContext().getString(R.string.ERROR_NEUTRAL_T1),getContext().getString(R.string.ERROR_NEUTRAL_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    ERROR_MODE3                 (51,G2EvseLevel.ERROR_L1,getContext().getString(R.string.ERROR_MODE3_T1),getContext().getString(R.string.ERROR_MODE3_T2),EvseColorEnum.ORANGE,getContext().getString(R.string.TEXT_STATUS_WARNING)),
    ERROR_OVERCURR              (52,G2EvseLevel.ERROR_L1,getContext().getString(R.string.ERROR_OVERCURR_T1),getContext().getString(R.string.ERROR_OVERCURR_T2),EvseColorEnum.ORANGE,getContext().getString(R.string.TEXT_STATUS_WARNING)),

    BLINK_CMD                   (2,G2EvseLevel.USER_L1,getContext().getString(R.string.BLINK_CMD_T1),getContext().getString(R.string.BLINK_CMD_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_Indefini)),

    RESERVED                    (19,G2EvseLevel.RUN_19,getContext().getString(R.string.RESERVED_T1),getContext().getString(R.string.RESERVED_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_RESERVED)),

    IDLE                        (10,G2EvseLevel.RUN_19,getContext().getString(R.string.IDLE_T1),getContext().getString(R.string.IDLE_T2),EvseColorEnum.GREEN,getContext().getString(R.string.TEXT_STATUS_FREE)),

    USER_LI_TAG                 (40,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LI_TAG_T1),getContext().getString(R.string.USER_LI_TAG_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_AUTHENTIFICATION)),
    USER_LI_VALIDATION_WAIT     (30,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LI_VALIDATION_WAIT_T1),getContext().getString(R.string.USER_LI_VALIDATION_WAIT_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_AUTHENTIFICATION)),
    USER_LI_SUCCESS             (42,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LI_SUCCESS_T1),getContext().getString(R.string.USER_LI_SUCCESS_T2),EvseColorEnum.GREEN,getContext().getString(R.string.TEXT_STATUS_AUTHENTIFICATION)),
    USER_LI_FAILURE             (43,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LI_FAILURE_T1),getContext().getString(R.string.USER_LI_FAILURE_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_AUTHENTIFICATION)),

    PREP_NOT_CONN               (32,G2EvseLevel.RUN_19,getContext().getString(R.string.PREP_NOT_CONN_T1),getContext().getString(R.string.PREP_NOT_CONN_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_SESSION)),
    PREP_NOT_CONN_DOOR_O        (34,G2EvseLevel.RUN_19,getContext().getString(R.string.PREP_NOT_CONN_DOOR_O_T1),getContext().getString(R.string.PREP_NOT_CONN_DOOR_O_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_SESSION)),
    PREP_CONN_DOOR_O            (33,G2EvseLevel.RUN_19,getContext().getString(R.string.PREP_CONN_DOOR_O_T1),getContext().getString(R.string.PREP_CONN_DOOR_O_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_SESSION)),
    PREP_CONN                   (20,G2EvseLevel.RUN_19,getContext().getString(R.string.PREP_CONN_T1),getContext().getString(R.string.PREP_CONN_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_SESSION)),
    SESSION_NOT_CONN            (21,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_NOT_CONN_T1),getContext().getString(R.string.SESSION_NOT_CONN_T2),EvseColorEnum.BLEU,getContext().getString(R.string.TEXT_STATUS_SESSION)),

    SESSION_EV_SUSPENDED        (13,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_EV_SUSPENDED_T1),getContext().getString(R.string.SESSION_EV_SUSPENDED_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),
    SESSION_CHARGING_EV         (14,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_CHARGING_EV_T1),getContext().getString(R.string.SESSION_CHARGING_EV_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),
    SESSION_CHARGING_AUX        (15,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_CHARGING_AUX_T1),getContext().getString(R.string.SESSION_CHARGING_AUX_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),
    SESSION_EVSE_SUSPENDED      (16,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_EVSE_SUSPENDED_T1),getContext().getString(R.string.SESSION_EVSE_SUSPENDED_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),
    SESSION_EV_WAKEUP           (17,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_EV_WAKEUP_T1),getContext().getString(R.string.SESSION_EV_WAKEUP_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),
    SESSION_WAIT_EV_STOP_CHARGE (18,G2EvseLevel.RUN_19,getContext().getString(R.string.SESSION_WAIT_EV_STOP_CHARGE_T1),getContext().getString(R.string.SESSION_WAIT_EV_STOP_CHARGE_T2),EvseColorEnum.CIAN,getContext().getString(R.string.TEXT_STATUS_RECHARGE)),

    USER_LO_TAG                 (46,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LO_TAG_T1),getContext().getString(R.string.USER_LO_TAG_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_INFIRMATION)),
    USER_LO_VALIDATION_WAIT     (31,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LO_VALIDATION_WAIT),getContext().getString(R.string.USER_LO_VALIDATION_WAIT_T2),EvseColorEnum.WHITE,getContext().getString(R.string.TEXT_STATUS_INFIRMATION)),
    USER_LO_SUCCESS             (44,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LO_SUCCESS_T1),getContext().getString(R.string.USER_LO_SUCCESS_T2),EvseColorEnum.GREEN,getContext().getString(R.string.TEXT_STATUS_INFIRMATION)),
    USER_LO_FAILURE             (45,G2EvseLevel.USER_L2,getContext().getString(R.string.USER_LO_FAILURE_T1),getContext().getString(R.string.USER_LO_FAILURE_T2),EvseColorEnum.RED,getContext().getString(R.string.TEXT_STATUS_INFIRMATION)),

    FINISH_PLUG_IN              (11,G2EvseLevel.RUN_19,getContext().getString(R.string.FINISH_PLUG_IN_T1),getContext().getString(R.string.FINISH_PLUG_IN_T2),EvseColorEnum.GREEN,getContext().getString(R.string.TEXT_STATUS_FREE)),
    FINISH_DOOR_O               (12,G2EvseLevel.RUN_19,getContext().getString(R.string.FINISH_DOOR_O),getContext().getString(R.string.FINISH_DOOR_O_T2),EvseColorEnum.GREEN,getContext().getString(R.string.TEXT_STATUS_FREE));



    private int id;
    private String textL1;
    private String textL2;
    private G2EvseLevel level;
    private EvseColorEnum color;
    private String textStatus;


    EvseStatusEnum(int id, G2EvseLevel level, String textL1, String textL2, EvseColorEnum color, String textStatus) {
        this.id = id;
        this.textL1 = textL1;
        this.textL2 = textL2;
        this.level = level;
        this.color = color;
        this.textStatus = textStatus;
    }

    EvseStatusEnum(int id, G2EvseLevel level, String textL1, String textL2) {
        this.id = id;
        this.textL1 = textL1;
        this.level = level;
        this.textL2 = textL2;
    }

    public EvseColorEnum getColor() {
        return color;
    }

    public String getTextStatus() {
        return textStatus;
    }

    public int getId() {
        return id;
    }

    public String getTextL1() {
        return textL1;
    }

    public String getTextL2() {
        return textL2;
    }

    public G2EvseLevel getLevel() {
        return level;
    }

    public static Context getContext(){
        return GlobalClass.getAppContext();
    }

    public static EvseStatusEnum getEvseStatusEnumFromId(int id) {
        for (EvseStatusEnum s : EvseStatusEnum.values()) {
            if(s.getId()==id)
                return s;

        }
        return EvseStatusEnum.UNDEF;
    }

    public boolean isOccupied(){
        if(color.equals(EvseColorEnum.BLEU) || color.equals(EvseColorEnum.CIAN) || color.equals(EvseColorEnum.ORANGE))
            return true;
        return false;
    }

    public boolean isAvailable(){
        if(color.equals(EvseColorEnum.GREEN))
            return true;
        return false;
    }

    public boolean isError(){
        if(color.equals(EvseColorEnum.RED))
            return true;
        return false;
    }

    public boolean isOut(){
        if(color.equals(EvseColorEnum.PINK))
            return true;
        return false;
    }

}
