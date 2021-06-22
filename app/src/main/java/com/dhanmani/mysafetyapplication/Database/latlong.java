package com.dhanmani.mysafetyapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class latlong extends SQLiteOpenHelper {

    Context ctx;
    public latlong(@Nullable Context context) {
        super(context, "latlngdb", null, 1);
        this.ctx= context;
    }

    public void insertData(String lat, String lon) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("latitude", lat);
        cv.put("longitude", lon);
        db.insert("latlng",null,cv);
        db.close();
    }

    public void delete() {
        String query= "select * from latlng";
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("latlng", null, null);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(String lat, String lon) {
        String query= "select * from latlng";
        SQLiteDatabase db= this.getWritableDatabase();
        String values[]= {lat,lon};
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("latlng", "name=? and number =?", values);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(String name1) {
        String query= "select * from latlng";
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor c1= db.rawQuery(query,null);
        if (c1!=null && c1.getCount() > 0) {
            if (c1.moveToFirst()) {
                do {
                    if (name1.equals(c1.getString(0))) {
                        db.delete("contact", "name=?", new String[]{name1});
                    }
                } while (c1.moveToNext());
            }
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query= "create table latlng(latitude text, longitude text);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
