package com.dhanmani.mysafetyapplication.Database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

import androidx.annotation.Nullable;

// Its a database class that is being made till now and its corresponding core implementations are left to be done...

public class ContactsDb extends SQLiteOpenHelper {

    Context ctx;
    public ContactsDb(@Nullable Context context) {
        super(context, "contactsdb", null, 1);
        this.ctx= context;
    }

    public void insertData(String name1, String number1) {
        SQLiteDatabase db= this.getWritableDatabase();
        ContentValues cv= new ContentValues();
        cv.put("name", name1);
        cv.put("number", number1);
        db.insert("contact",null,cv);
        db.close();
    }

    public void delete() {
        String query= "select * from contact";
        SQLiteDatabase db= this.getWritableDatabase();
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("contact", null, null);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(String name, String number) {
        String query= "select * from contact";
        SQLiteDatabase db= this.getWritableDatabase();
        String values[]= {name,number};
        Cursor c1= db.rawQuery(query,null);
        if (c1.getCount() > 0) {
            db.delete("contact", "name=? and number =?", values);
        }
        else Toast.makeText(ctx, " sorry but no data to delete!", Toast.LENGTH_SHORT).show();
        db.close();
    }

    public void delete(String name1) {
        String query= "select * from contact";
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
        String query= "create table contact(name text, number text);";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
