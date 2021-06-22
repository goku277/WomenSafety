package com.dhanmani.mysafetyapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;

public class LocationBook extends SQLiteOpenHelper {

    Context ctx;
    public LocationBook(@Nullable Context context) {
        super(context, "LocationTracker", null, 1);
        this.ctx= context;
    }


    public void insertData(String place, String latitude, String longitude, String addressline) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("place",place);
        cv.put("latitude",latitude);
        cv.put("longitude",longitude);
        cv.put("addressline",addressline);
        db.insert("location",null,cv);
        db.close();
    }

    public void delete() {
        String query= "select * from location";
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("location", null, null);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(String place, String latitude, String longitude) {
        String query= "select * from location";
        SQLiteDatabase db= this.getWritableDatabase();
        String values[]= {place,latitude,longitude};
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("location", "place=? and latitude =? and longitude =?", values);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public HashMap<String, ArrayList<String>> getValues() {
        String lat_long="";
        ArrayList<String> placeValues= new ArrayList<>();
        HashMap<String, ArrayList<String>> map= new HashMap<>();
        SQLiteDatabase db= this.getReadableDatabase();
        String cols[]= {"place","latitude","longitude"};
        Cursor cur= db.query("location",cols,null,null,null,null,null);
        if (cur.getCount() > 0 && cur!=null) {
            if (cur.moveToFirst()) {
                do {
                    lat_long = cur.getString(1) + "" + cur.getString(2);
                    placeValues.add(cur.getString(0));
                    map.put(lat_long, placeValues);
                    lat_long = "";
                } while (cur.moveToNext());
            }
            db.close();
        }
        return map;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query= "create table location(place text, latitude text, longitude text, addressline text);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
