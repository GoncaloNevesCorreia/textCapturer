package com.joythis.android.pdm2textcapturer;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Calendar;

public class TextDB extends SQLiteOpenHelper {
    Context mContext;
    AmUtil amUtil;

    public final static int DB_VERSION = 2;

    public final static String DB_NAME = "Text.DB";
    public final static String TABLE_TEXT = "tableText";

    public final static String COL_ID = "col_id";
    public final static String COL_TEXT = "col_text";
    public final static String COL_DATE = "col_date";

    public final static String DROP_TABLE_TEXT = "DROP TABLE IF EXISTS " + TABLE_TEXT + ";";

    public final static String CREATE_TABLE_TEXT = "CREATE TABLE IF NOT EXISTS " + TABLE_TEXT
            + "( " + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            COL_TEXT + " TEXT NOT NULL, " +
            COL_DATE + " TEXT NOT NULL);";

    public final static String SELECT_ALL = "SELECT * FROM " + TABLE_TEXT + " ORDER BY " + COL_DATE +" DESC;";


    public TextDB(@Nullable Context context) {
        super(context, DB_NAME, null, DB_VERSION);

        this.mContext = context;
        amUtil = new AmUtil((Activity) context);
    }//ContactDB


    @Override
    public void onCreate(SQLiteDatabase db) {
        installDB(db);
    }


    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            db.execSQL(DROP_TABLE_TEXT);
            installDB(db);
        }
    }

    void installDB(SQLiteDatabase pDB) {
        pDB.execSQL(CREATE_TABLE_TEXT);
    }

    public static final long EMPTY_VALUE = -1;
    public static final long INSERTED_WITH_SUCCESS = 1;

    public long insertText(String pStrText) {
        String cleanText = pStrText.trim();
        if (cleanText.isEmpty()) return EMPTY_VALUE;

        //try to insert new pStrText
        try {
            SQLiteDatabase db = getWritableDatabase();
            if (db != null) {
                ContentValues pairsCV = new ContentValues();

                Calendar cal = Calendar.getInstance();

                String strDate = amUtil.CalendarToString(cal);

                pairsCV.put(COL_TEXT, cleanText);
                pairsCV.put(COL_DATE, strDate);
                db.insert(TABLE_TEXT, null, pairsCV);
                db.close();
                return INSERTED_WITH_SUCCESS;
            }
        } catch (Exception e) {
            Log.e(
                    this.getClass().getName(),
                    e.toString()
            );
        }

        return -1;

    }//insertText

    public ArrayList<SharedText> selectAll() {
        ArrayList<SharedText> aRet = new ArrayList<>();

        try {
            SQLiteDatabase db = this.getReadableDatabase();

            if (db != null) {
                Cursor cursor = db.rawQuery(SELECT_ALL, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    while (!cursor.isAfterLast()) {

                        int idxOfId = cursor.getColumnIndex(COL_ID);//0
                        int _id = cursor.getInt(idxOfId);

                        int idxOfText = cursor.getColumnIndex(COL_TEXT);//1
                        String strName = cursor.getString(idxOfText);

                        int idxOfDate = cursor.getColumnIndex(COL_DATE); //2
                        String strNumber = cursor.getString(idxOfDate);

                        SharedText c = new SharedText(_id, strName, strNumber);

                        aRet.add(c);

                        cursor.moveToNext();
                    }//while there are records to be read and included in the return

                    db.close();
                }//if there is a cursor objet to navigate through the objects that the select returned
            }//if we got a readable database
        } catch (Exception e) {
            Log.e(
                    getClass().getName(),
                    e.toString()
            );
        }

        return aRet;
    }//selectAll


    public final static boolean ITEM_REMOVED_FROM_DB = true;

    public boolean remove(int id) {
        SQLiteDatabase db = getWritableDatabase();
        if (db != null) {
            String strWhereFilter = COL_ID + "=?";
            String[] aValuesForFilters = {String.valueOf(id)};
            try {
                db.delete(TABLE_TEXT, strWhereFilter, aValuesForFilters);
                return ITEM_REMOVED_FROM_DB;
            }//try
            catch (Exception e) {
                Log.e(
                        this.getClass().getName(),
                        e.toString()
                );
            }//catch
        }//if

        return !ITEM_REMOVED_FROM_DB;
    }//remove
}
