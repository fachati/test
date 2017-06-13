package com.g2mobility.status;

import android.content.Context;

/**
 * Created by fachati on 22/03/17.
 */

public enum EvseColorEnum {


    WHITE                                   (R.drawable.borderwhite),
    YELLOW                                   (R.drawable.borderyellow),
    PINK                                 (R.drawable.borderpink),
    RED                                   (R.drawable.borderred),
    BLEU                                (R.drawable.borderbleu),
    CIAN                                 (R.drawable.bordercian),
    ORANGE                                 (R.drawable.borderorange),
    GREEN                               (R.drawable.bordergreen);

    private int codeColor;



    EvseColorEnum(int code) {
        this.codeColor = code;
    }

    public int getCode() {
        return codeColor;
    }


    public static Context getContext(){
        return GlobalClass.getAppContext();
    }

    }
