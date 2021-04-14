package com.example.digitalwallet;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MyOpenHelper extends SQLiteOpenHelper {

    public MyOpenHelper(Context context) {
        super(context, "WalletDB", null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // version1
        db.execSQL("create table CategoryTable(id integer primary key, category text, amount integer);");
        db.execSQL("create table PaymentTable(id integer primary key, month integer, date integer, category integer, inout integer, amount integer);");

        // version2
        version2(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version2
        if (oldVersion <= 1 && newVersion >= 2) {
            version2(db);
        }
    }

    // version2で追加
    private void version2(SQLiteDatabase db) {
        // 収支の内容を保存するテーブル作成
        db.execSQL("create table DetailTable(id integer primary key, detail text);");
    }
}
