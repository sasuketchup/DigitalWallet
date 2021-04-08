package com.example.digitalwallet;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CategoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // インテントを受け取る
        Intent intent = getIntent();
        int categoryID = intent.getIntExtra("categoryID", 0);

        MyOpenHelper helper = new MyOpenHelper(this);
        SQLiteDatabase db = helper.getWritableDatabase();

        // カテゴリーの情報を取得
        Cursor cursor = db.query("CategoryTable", new String[] {"id", "category", "amount"}, "id=" + categoryID, null, null, null, null);
        cursor.moveToFirst();
        String categoryName = cursor.getString(1);
        int categoryAmount = cursor.getInt(2);
        cursor.close();

        // カテゴリー名と残額を表示
        TextView cateNameText =  findViewById(R.id.categoryName);
        TextView amountText = findViewById(R.id.amount);
        cateNameText.setText(categoryName);
        amountText.setText(String.valueOf(categoryAmount));
        if (categoryAmount > 0) {
            amountText.setTextColor(Color.GREEN);
        } else if (categoryAmount < 0) {
            amountText.setTextColor(Color.RED);
        }
    }
}
