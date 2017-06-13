package com.g2mobility.status;
import android.app.Application;
import android.content.Context;
import android.os.Environment;
import android.util.Log;


import com.digi.xbee.api.models.XBee64BitAddress;
import com.g2mobility.status.helper.LocaleHelper;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class GlobalClass extends Application {

    private EVSE[] evseList;
    private static Context context;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleHelper.onAttach(base, "en"));
    }

    @Override
    public void onCreate() {
        super.onCreate();

        context = getApplicationContext();
        getBorneInformation();
    }

    public static Context getAppContext() {
        return context;
    }


    public ArrayList<Borne> getBorneInformation() {

        ArrayList<Borne> list;
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonSerialize.Inclusion.NON_NULL);
        mapper.setSerializationConfig(mapper.getSerializationConfig()
                .without(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS)
                .withVisibilityChecker(
                        mapper.getVisibilityChecker()
                                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)));
        mapper.setDeserializationConfig(mapper.getDeserializationConfig()
                .withVisibilityChecker(
                        mapper.getVisibilityChecker()
                                .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
                                .withFieldVisibility(JsonAutoDetect.Visibility.ANY)));
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                +"/Download", "status.json");
        if(file.exists())
            Log.e("existe","exist");

        try {
            list = mapper.readValue(file, new TypeReference<
                    ArrayList<Borne>>() {
            });
            if (list == null) {
                return new ArrayList<>();
            } else {
                evseList=new EVSE[list.size()];
                for(int i=0;i<list.size();i++){
                    evseList[i]=new EVSE(new XBee64BitAddress(list.get(i).getAddress()),list.get(i).getNodeID());
                }

                return list;
            }

        } catch (IOException e) {
            Log.e("log",e.toString());
            return new ArrayList<>();
        }
    }

    public EVSE[] getEvseList() {
        return evseList;
    }
}
