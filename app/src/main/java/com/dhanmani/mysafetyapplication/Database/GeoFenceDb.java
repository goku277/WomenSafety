package com.dhanmani.mysafetyapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

public class GeoFenceDb extends SQLiteOpenHelper {
    Context context;
    public GeoFenceDb(@Nullable Context context) {
        super(context, "geofencedb", null, 1);
        this.context= context;
    }

    public void delete() {
        String query= "select * from geofence";
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("geofence", null, null);
        }
        else Toast.makeText(context, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void insertData(String geo) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("geofenceinitiated", geo);
        db.insert("geofence",null,cv);
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query= "create table geofence(geofenceinitiated text);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}