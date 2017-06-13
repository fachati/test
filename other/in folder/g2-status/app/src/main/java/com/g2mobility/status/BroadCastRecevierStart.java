package com.g2mobility.status;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BroadCastRecevierStart extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)){
            Intent i = new Intent(context, WelcomeActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);
        }
    }
}