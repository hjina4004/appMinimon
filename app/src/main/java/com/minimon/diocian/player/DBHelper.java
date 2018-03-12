package com.minimon.diocian.player;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by GOOD on 2018-03-12.
 */

public class DBHelper extends SQLiteOpenHelper {

    private dbHelperListenr mListner;

    public interface dbHelperListenr{
        void onSucess(JSONObject data);
        void onFail(JSONObject data);
    }
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public void setListener(dbHelperListenr listener){
        mListner = listener;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE SEARCHHISTORY (log TEXT, date TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void insert(String history, String date){
        if(isExist(history)){
            update(history,date);
        }else {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO SEARCHHISTORY VALUES ('" + history + "','" + date + "');");
            db.close();
            if(mListner != null){
                try {
                    JSONObject obj = new JSONObject();
                    obj.put("functionName", "insert");
                    obj.put("history",history);
                    obj.put("date",date);
                    mListner.onSucess(obj);
                }catch (JSONException e){
                    mListner.onFail(null);
                }
            }
        }
    }

    private void update(String history, String date){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE SEARCHHISTORY SET date='"+date+"' WHERE log='"+history+"';");
        db.close();
        if(mListner != null){
            try {
                JSONObject obj = new JSONObject();
                obj.put("functionName", "update");
                obj.put("history",history);
                obj.put("date",date);
                mListner.onSucess(obj);
            }catch (JSONException e){
                mListner.onFail(null);
            }
        }
    }

    public void delete(String history){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM SEARCHHISTORY WHERE log='"+history+"';");
        db.close();
    }

    public void deleteAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM SEARCHHISTORY");
        db.close();
    }

    public List<SearchItem> getResult(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SEARCHHISTORY",null);
        List<SearchItem> result = new ArrayList<>();
        while (cursor.moveToNext()){
            SearchItem item = new SearchItem();
            item.setHistory(cursor.getString(0));
            item.setDate(cursor.getString(1));
            result.add(item);
        }
        db.close();
        return result;
    }

    public List<String> getHistoryResult(){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM SEARCHHISTORY",null);
        List<String> result = new ArrayList<>();
        while (cursor.moveToNext()){
            result.add(cursor.getString(0));
        }
        db.close();
        return result;
    }

    public boolean isExist(String history){
        SQLiteDatabase db = getReadableDatabase();
        SQLiteStatement s = db.compileStatement( "select count(*) from SEARCHHISTORY where log='" + history + "'; " );

        long count = s.simpleQueryForLong();
        Log.d("CursorCount",String.valueOf(count));
        db.close();
        if(count > 0){
            return true;
        }else{
            return false;
        }
    }
}
