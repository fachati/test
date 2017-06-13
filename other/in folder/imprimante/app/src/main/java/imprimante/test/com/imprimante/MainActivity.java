package imprimante.test.com.imprimante;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.epson.lwprint.sdk.LWPrint;
import com.epson.lwprint.sdk.LWPrintDataProvider;
import com.epson.lwprint.sdk.LWPrintDiscoverConnectionType;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinter;
import com.epson.lwprint.sdk.LWPrintDiscoverPrinterCallback;
import com.epson.lwprint.sdk.LWPrintParameterKey;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;

public class MainActivity extends Activity {

    private String TAG = "WelcomeActivity";

    LWPrint lwprint;
    Map<String, Object> _printSettings = null;
    Map<String, String> _printerInfo = null;
    Map<String, Integer> _lwStatus = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lwprint = new LWPrint(this);
        setPrintSettingsValues();
        performPrint();
    }

    private void setPrintSettingsValues() {
        int copies = LWPrintSampleUtil.DEFAULT_COPIES_SETTING;
        int tapeCut = LWPrintSampleUtil.DEFAULT_TAPE_CUT_SETTING;
        boolean halfCut = LWPrintSampleUtil.DEFAULT_HALF_CUT_SETTING;
        boolean printSpeed = LWPrintSampleUtil.DEFAULT_LOW_SPEED_SETTING;
        int density = LWPrintSampleUtil.DEFAULT_DENSITY_SETTING;

        _printSettings = new HashMap<String, Object>();
        _printSettings.put(LWPrintParameterKey.Copies, copies);
        _printSettings.put(LWPrintParameterKey.TapeCut, tapeCut);
        _printSettings.put(LWPrintParameterKey.HalfCut, halfCut);
        _printSettings.put(LWPrintParameterKey.PrintSpeed, printSpeed);
        _printSettings.put(LWPrintParameterKey.Density, density);

        SharedPreferences pref = getSharedPreferences(
                LWPrintSampleUtil.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(LWPrintParameterKey.Copies, copies);
        editor.putInt(LWPrintParameterKey.TapeCut, tapeCut);
        editor.putBoolean(LWPrintParameterKey.HalfCut, halfCut);
        editor.putBoolean(LWPrintParameterKey.PrintSpeed, printSpeed);
        editor.putInt(LWPrintParameterKey.Density, density);
        editor.commit();
        if (_printerInfo != null) {
            _printerInfo.clear();
            _printerInfo = null;
        }
        _printerInfo = new HashMap<String, String>();
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_NAME, "EPSON LW-1000P");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_PRODUCT, "(EPSON LW-1000P)");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_USBMDL, "LW-1000P");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_HOST, "192.168.133.31");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_PORT, "9100");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_TYPE, "_pdl-datastream._tcp.local.");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_DOMAIN, "local");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_SERIAL_NUMBER, "");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_CLASS, "");
        _printerInfo.put(LWPrintDiscoverPrinter.PRINTER_INFO_DEVICE_STATUS, "");
    }


    private void performPrint() {


        new AsyncTask<Object, Object, Object>() {
            @Override
            protected Object doInBackground(Object... params) {
                lwprint.setPrinterInformation(_printerInfo);
                if (_lwStatus == null) {
                    _lwStatus = lwprint.fetchPrinterStatus();
                }


                int tapeWidth = lwprint.getTapeWidthFromStatus(_lwStatus);

                SampleDataProvider provider = new SampleDataProvider();

                Map<String, Object> printParameter = new HashMap<String, Object>();
                printParameter.put(LWPrintParameterKey.Copies,
                        _printSettings.get(LWPrintParameterKey.Copies));
                printParameter.put(LWPrintParameterKey.TapeCut,
                        _printSettings.get(LWPrintParameterKey.TapeCut));
                printParameter.put(LWPrintParameterKey.HalfCut,
                        _printSettings.get(LWPrintParameterKey.HalfCut));
                printParameter.put(LWPrintParameterKey.PrintSpeed,
                        _printSettings.get(LWPrintParameterKey.PrintSpeed));
                printParameter.put(LWPrintParameterKey.Density,
                        _printSettings.get(LWPrintParameterKey.Density));
                printParameter.put(LWPrintParameterKey.TapeWidth, tapeWidth);
                lwprint.doPrint(provider, printParameter);

                return null;
            }
        }.execute();
    }

    class SampleDataProvider implements LWPrintDataProvider {


        List<ContentsData> _contentsData = null;


        @Override
        public void startOfPrint() {
            Log.d(TAG, "startOfPrint");

            LWPrintContentsXmlParser xmlParser = new LWPrintContentsXmlParser();
            try {
                _contentsData = xmlParser.parse(getResources().getAssets().open("Sample/Image_CONTENTS.plist"), "UTF-8");

                for (int i = 0; i < _contentsData.size(); i++) {
                    for (String key : _contentsData.get(i).getElementMap().keySet()) {
                        Log.e("contentData " + i, key + " - " + _contentsData.get(i).getElementMap().get(key));
                    }

                }
            } catch (Exception e) {
                Log.e(TAG, e.toString(), e);
            }
        }

        @Override
        public void endOfPrint() {
            Log.d(TAG, "endOfPrint");
        }

        @Override
        public void startPage() {
            Log.d(TAG, "startPage");
        }

        @Override
        public void endPage() {
            Log.d(TAG, "endPage");
        }

        @Override
        public int getNumberOfPages() {
            if (_contentsData == null) {
                Log.d(TAG, "getNumberOfPages: 0");
                return 0;
            } else {
                Log.d(TAG, "getNumberOfPages: "
                        + _contentsData.size());
                return _contentsData.size();
            }
        }

        @Override
        public InputStream getFormDataForPage(int pageIndex) {
            Log.d(TAG, "getFormDataForPage: pageIndex="
                    + pageIndex);
            InputStream _formDataInputStream = null;
            try {
                _formDataInputStream = getResources().getAssets().open("Sample/Image.plist");
                Log.d(TAG, "getFormDataForPage: " + "Image.plist" + "=" + _formDataInputStream.available());
            } catch (IOException e) {
                Log.e(TAG, e.toString(), e);
            }

            return _formDataInputStream;
        }

        @Override
        public String getStringContentData(String contentName, int pageIndex) {

            return "";
        }


        @Override
        public Bitmap getBitmapContentData(String contentName, int pageIndex) {
            Log.d(TAG,
                    "getBitmapContentData: contentName=" + contentName
                            + ", pageIndex=" + pageIndex);

            Bitmap TAG = Label("GS-B3LD-T3220", "ADA-97REU-004", "400v 3~", "32A", "50-60 Hz", "25kw");
            return TAG;
        }

    }

    public Bitmap Label(String Ref, String chargUnitId, String U, String I, String F, String Power) {
        Bitmap bitm = null;
        QRCodeEncoder qrCodeEncoder = new QRCodeEncoder("Réference : " + Ref + "\nChargingUnitId : " + chargUnitId + "\nhttp://www.g2mobility.com",
                null,
                Contents.Type.TEXT,
                BarcodeFormat.QR_CODE.toString(), 200);
        try {
            bitm = qrCodeEncoder.encodeAsBitmap();
        } catch (WriterException e) {
            e.printStackTrace();
        }

        Resources res = getResources();
        Bitmap bitmap = BitmapFactory.decodeResource(res, R.drawable.mark);


        Bitmap bg = Bitmap.createBitmap(400, 220, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bg);


        Paint paint = new Paint();
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setColor(Color.WHITE);
        paint.setTextSize(30);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawRect(0, 0, 600, 250, paint);

        canvas.drawBitmap(bitm, bg.getWidth() - bitm.getWidth() + 10, 10, paint);
        canvas.drawBitmap(bitmap, null, new Rect(180, 100, 220, 160), null);

        paint.setColor(Color.BLACK);

        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((paint.descent() + paint.ascent()) / 2)) ;
        canvas.drawText(Ref, xPos, 30, paint);

        paint.setTextSize(30);
        canvas.drawText("Serial N :"+paint.getTextSize(), bg.getWidth() / 7, 60, paint);
        canvas.drawText(chargUnitId, 10, 90, paint);

        paint.setTextSize(22);
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        canvas.drawText("  U :  " + U, 10, 120, paint);
        canvas.drawText("  I  :  " + I, 10, 140, paint);
        canvas.drawText("  F :  " + F, 10, 160, paint);
        canvas.drawText("     IP44/56", 10, 180, paint);

        paint.setTextSize(27);
        canvas.drawText(Power, bg.getWidth() / 3, 190, paint);
        canvas.drawText("G²Mobility", bg.getWidth() / 2 + 45, 210, paint);

        return bg;
    }
}


