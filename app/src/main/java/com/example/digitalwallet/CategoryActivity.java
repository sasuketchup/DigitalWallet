package com.example.digitalwallet;

import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class CategoryActivity extends AppCompatActivity {

    AmountHandler amountHandler = new AmountHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        // インテントを受け取る
        Intent intent = getIntent();
        final int categoryID = intent.getIntExtra("categoryID", 0);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);

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

        // 割り当て額を格納する変数(ベースとして残額をセット)
        int allocatedAmount = categoryAmount;

        // 収支履歴を表示
        long countCP = DatabaseUtils.queryNumEntries(db, "PaymentTable", "category=" + categoryID);

        if (countCP > 0) {

            Cursor cursor1 = db.query("PaymentTable", new String[]{"id", "month", "date", "category", "inout", "amount"}, "category=" + categoryID, null, null, null, null);
            cursor1.moveToLast();

            LinearLayout varCatePayLayout = findViewById(R.id.catePayLayout);
            LinearLayout[] catePay = new LinearLayout[(int) countCP];

            TextView[] dateText = new TextView[(int) countCP];
            TextView[] amountTextIO = new TextView[(int) countCP];

            TextView[] detailText = new TextView[(int) countCP];

            for (int i=0; i<countCP; i++) {
                catePay[i] = new LinearLayout(this);
                catePay[i].setOrientation(LinearLayout.HORIZONTAL);
                dateText[i] = new TextView(this);
                amountTextIO[i] = new TextView(this);
                detailText[i] = new TextView(this);

                int paymentID = cursor1.getInt(0);
                int month = cursor1.getInt(1);
                int date = cursor1.getInt(2);

                int inout = cursor1.getInt(4);
                int amount = cursor1.getInt(5);

                // paymentIDに対応するdetailを取得
                Cursor cursor2 = db.query("DetailTable", new String[]{"id", "detail"}, "id=" + paymentID, null, null, null, null);
                cursor2.moveToFirst();
                String detail = cursor2.getString(1);
                cursor2.close();

                dateText[i].setText(month + "/" + date);
                dateText[i].setTextSize(20);

                detailText[i].setText(detail);
                detailText[i].setTextSize(20);

                switch (inout) {
                    case 0:
                        amountTextIO[i].setText("-" + amount + "円");
                        amountTextIO[i].setTextColor(Color.BLUE);
                        // 支出のときのみ割り当て額に加算
                        allocatedAmount += amount;
                        break;
                    case 1:
                        amountTextIO[i].setText("+" + amount + "円");
                        amountTextIO[i].setTextColor(Color.GREEN);
                        break;
                }
                amountTextIO[i].setTextSize(24);

                dateText[i].setGravity(Gravity.RIGHT);
                detailText[i].setGravity(Gravity.CENTER_HORIZONTAL);
                amountTextIO[i].setGravity(Gravity.RIGHT);

                dateText[i].setWidth(200);
                detailText[i].setWidth(400);
                amountTextIO[i].setWidth(400);

                catePay[i].addView(dateText[i]);
                catePay[i].addView(detailText[i]);
                catePay[i].addView(amountTextIO[i]);
                varCatePayLayout.addView(catePay[i]);

                cursor1.moveToPrevious();
            }
            cursor1.close();
        }

        // 割り当て額表示
        TextView allocatedAmountText = findViewById(R.id.allocatedAmount);
        allocatedAmountText.setText(String.valueOf(allocatedAmount));

        // 収支入力ボタン
        findViewById(R.id.inputBtn2).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        ViewGroup root = findViewById(R.id.input_root);

                        amountHandler.writeInOut(CategoryActivity.this, db, inflater, root, categoryID, null);
                    }
                }
        );
    }
}
