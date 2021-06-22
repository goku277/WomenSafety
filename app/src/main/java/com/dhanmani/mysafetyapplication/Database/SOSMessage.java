package com.dhanmani.mysafetyapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class SOSMessage extends SQLiteOpenHelper {

    Context context;
    public SOSMessage(@Nullable Context context) {
        super(context, "sosmessagedb", null, 1);
        this.context= context;
    }

    public void insertData(String sosmessage) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("message", sosmessage);
        db.insert("sosmessage",null,cv);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query= "create table sosmessage(message text);";
        db.execSQL(query);
    }

    public void delete() {
        SQLiteDatabase db= this.getWritableDatabase();
        db.delete("sosmessage", null, null);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
