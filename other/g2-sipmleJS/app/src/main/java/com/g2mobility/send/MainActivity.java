package com.g2mobility.send;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends Activity {

    WebView myBrowser;
    EditText Msg;
    Button btnSendMsg;
    String textButton;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textButton="Msg";

        myBrowser = (WebView)findViewById(R.id.mybrowser);
        myBrowser.setWebChromeClient(new WebChromeClient() {
            public boolean onConsoleMessage(ConsoleMessage msg) {
                Log.d("MyApplication", msg.message() + " -- From line "
                        + msg.lineNumber() + " of "
                        + msg.sourceId());
                return false;
            }
        });
        final MyJavaScriptInterface myJavaScriptInterface
                = new MyJavaScriptInterface(this);

        myBrowser.addJavascriptInterface(myJavaScriptInterface, "AndroidFunction");

        myBrowser.getSettings().setJavaScriptEnabled(true);
        myBrowser.loadUrl("file:///android_asset/G2.html");

        Msg = (EditText)findViewById(R.id.msg);
        btnSendMsg = (Button)findViewById(R.id.sendmsg);

        final int[] addr = new int[]{
                0x00, 0x13, 0xA2, 0x00, 0x40, 0xDA, 0xF4, 0xc2
        };
        final int[] payload = new int[]{
                0xf3, 0xFE, 0x0, 0x0, 0x0, 0x20, 0x00, 0x00, 0x00, 0x0, 0x0, 0x0, 0x0, 0x0
        };

        final String msgaddr=showTab(addr);
        final String msgframe=showTab(payload);

        btnSendMsg.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                // TODO Auto-generated method stub
                final String fn = "javascript:process_rx_packet(\"" + msgaddr.substring(1) + "\", \"" + msgframe.substring(1) + "\")";
                Log.e("Welcomactivity",fn);
                String msgToSend = "00 11";
                myBrowser.loadUrl(fn);

            }
        });
    }

    public String showTab(int[] tab) {
        String SA = "";
        for (int i:tab)
            SA = SA + " " + Integer.toHexString(i);
        return SA;
    }

    public class MyJavaScriptInterface {
        Context mContext;

        MyJavaScriptInterface(Context c) {
            mContext = c;
        }

        @JavascriptInterface
        public void showToast(String toast){
            String tram=toast.substring(8,toast.indexOf("addr")-1);
            String address=toast.substring(toast.lastIndexOf(":")+2,toast.length());
            tram=tram.trim().replaceAll(" +", " ");
            Toast.makeText(mContext, tram+"\n"+address, Toast.LENGTH_SHORT).show();
            Log.e("WelcomActivity",tram+"\n"+address);
        }


        @JavascriptInterface
        public void openAndroidDialog(){
            AlertDialog.Builder myDialog
                    = new AlertDialog.Builder(MainActivity.this);
            myDialog.setTitle("DANGER!");
            myDialog.setMessage("You can do what you want!"+" "+textButton);
            myDialog.setPositiveButton("ON", null);
            myDialog.show();
        }

    }
}
