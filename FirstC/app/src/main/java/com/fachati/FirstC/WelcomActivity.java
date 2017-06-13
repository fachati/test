package com.fachati.FirstC;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import java.util.Map;
import com.inmobi.ads.*;
import com.inmobi.sdk.*;

public class WelcomActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcom);

        InMobiSdk.init(this, "1dad3b893c424f2d9db53e28c12594d5"); //'this' is used specify context
        // ‘this’ is used to specify context, replace it with the appropriate context as needed.
        InMobiInterstitial interstitial = new InMobiInterstitial(this, 1480231259309L, new InMobiInterstitial.InterstitialAdListener() {
            @Override
            public void onAdRewardActionCompleted(InMobiInterstitial ad, Map rewards) {}
            @Override
            public void onAdDisplayed(InMobiInterstitial ad) {}
            @Override
            public void onAdDismissed(InMobiInterstitial ad) {}
            @Override
            public void onAdInteraction(InMobiInterstitial ad, Map params) {}
            @Override
            public void onAdLoadSucceeded(final InMobiInterstitial ad) {
                ad.load();
            }
            @Override
            public void onAdLoadFailed(InMobiInterstitial ad, InMobiAdRequestStatus requestStatus) {}
            @Override
            public void onUserLeftApplication(InMobiInterstitial ad){}
        });


    }
}
