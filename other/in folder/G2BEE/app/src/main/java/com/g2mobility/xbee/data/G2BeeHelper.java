package com.g2mobility.xbee.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.digi.xbee.api.models.XBee64BitAddress;
import com.digi.xbee.api.utils.HexUtils;
import com.g2mobility.xbee.recycler.RecyclerXBeeMessage;
import com.g2mobility.xbee.recycler.XBeeConversation;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class G2BeeHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "g2bee.db";
    private static final int DATABASE_VERSION = 1;

    private static final String TABLE_MESSAGE = "message";

    private static final String ID = "id";
    private static final String ADDRESS = "address";
    private static final String TYPE = "type";
    private static final String MESSAGE = "message";
    private static final String TIME = "time";
    private static final String OUTGOING = "outgoing";

    private static G2BeeHelper sInstance;
    private SQLiteDatabase mDatabase;

    public static G2BeeHelper getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new G2BeeHelper(context);
        }
        return sInstance;
    }

    private G2BeeHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        mDatabase = getWritableDatabase();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_MESSAGE + "(" +
                ID + " INTEGER PRIMARY KEY," +
                ADDRESS + " TEXT NOT NULL," +
                TYPE + " TEXT NOT NULL," +
                MESSAGE + " TEXT NOT NULL," +
                TIME + " NUMERIC NOT NULL," +
                OUTGOING + " INTEGER NOT NULL)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP " +
                "TABLE IF EXISTS " + TABLE_MESSAGE);
        onCreate(db);
    }

    public void saveMessage(String address, String type, byte[] message, Date time,
            boolean isOutgoing) {
        ContentValues values = new ContentValues();
        values.put(ADDRESS, address);
        values.put(TYPE, type);
        values.put(MESSAGE, HexUtils.byteArrayToHexString(message));
        values.put(TIME, time.getTime());
        values.put(OUTGOING, isOutgoing);

        mDatabase.insert(TABLE_MESSAGE, null, values);
    }

    public List<XBeeConversation> getConversations() {
        List<XBeeConversation> conversations = new ArrayList<>();

        Cursor c = mDatabase.rawQuery("SELECT MSG.* FROM " + TABLE_MESSAGE + " MSG WHERE " +
                TIME + "=(SELECT MAX(" + TIME + ") FROM " + TABLE_MESSAGE + " WHERE " +
                ADDRESS + "=MSG." + ADDRESS + ")", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    XBeeConversation conversation = new XBeeConversation();
                    conversation.setAddress(new XBee64BitAddress(c.getString(c.getColumnIndex
                            (ADDRESS))));
                    conversation.setType(c.getString(c.getColumnIndex(TYPE)));

                    String hex = c.getString(c.getColumnIndex(MESSAGE));
                    byte[] message = HexUtils.hexStringToByteArray(hex);
                    conversation.setMessage(message);
                    conversation.setTime(new Date(c.getLong(c.getColumnIndex(TIME))));
                    conversations.add(conversation);
                } while (c.moveToNext());
            }
            c.close();
        }

        return conversations;
    }

    public void deleteConversation(String address) {
        mDatabase.delete(TABLE_MESSAGE, ADDRESS + "='" + address + "'", null);
    }

    public void deleteXBeeMessage(String address, RecyclerXBeeMessage message) {
        mDatabase.delete(TABLE_MESSAGE, ADDRESS + "='" + address + "' AND " +
                TIME + "=" + message.getTime().getTime() + " AND " +
                OUTGOING + "=" + (message.isOutgoing() ? 1 : 0), null);
    }

    public List<RecyclerXBeeMessage> getMessages(String address) {
        List<RecyclerXBeeMessage> messages = new ArrayList<>();

        Cursor c = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGE +
                " WHERE " + ADDRESS + "='" + address + "' ORDER BY " + TIME + " DESC", null);
        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                do {
                    RecyclerXBeeMessage message = new RecyclerXBeeMessage();

                    String hex = c.getString(c.getColumnIndex(MESSAGE));
                    byte[] data = HexUtils.hexStringToByteArray(hex);
                    message.setMessage(data);
                    message.setTime(new Date(c.getLong(c.getColumnIndex(TIME))));
                    message.setIsOutgoing(c.getInt(c.getColumnIndex(OUTGOING)) > 0);
                    messages.add(message);
                } while (c.moveToNext());
            }
            c.close();
        }

        return messages;
    }

    public String getXBeeNodeType(String address) {
        String type = "?";

        Cursor c = mDatabase.rawQuery("SELECT * FROM " + TABLE_MESSAGE + " WHERE " +
                ADDRESS + "='" + address + "' ORDER BY " + TIME + " DESC" +
                " LIMIT 1", null);

        if (c != null) {
            if (c.getCount() > 0) {
                c.moveToFirst();
                type = c.getString(c.getColumnIndex(TYPE));
            }
            c.close();
        }

        return type;
    }

}
