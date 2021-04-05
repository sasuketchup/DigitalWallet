package com.example.digitalwallet;

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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.w3c.dom.Text;

public class PaymentTab extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_payments,container,false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyOpenHelper helper = new MyOpenHelper(getContext());
        SQLiteDatabase db = helper.getWritableDatabase();

        long countPT = DatabaseUtils.queryNumEntries(db, "PaymentTable");

        if (countPT > 0) {
            Cursor cursor = db.query("PaymentTable", new String[] {"id", "month", "date", "category", "inout", "amount"}, null, null, null, null, null);
            cursor.moveToLast();

            LinearLayout varPaymentLayout = view.findViewById(R.id.paymentLayout);
            LinearLayout[] payment = new LinearLayout[(int) countPT];

            TextView[] dateText = new TextView[(int) countPT];
            TextView[] categoryText = new TextView[(int) countPT];
            TextView[] amountText = new TextView[(int) countPT];

            // 履歴を表示
            for (int i=0; i<countPT; i++) {
                payment[i] = new LinearLayout(getContext());
                payment[i].setOrientation(LinearLayout.HORIZONTAL);
                dateText[i] = new TextView(getContext());
                categoryText[i] = new TextView(getContext());
                amountText[i] = new TextView(getContext());

                int month = cursor.getInt(1);
                int date = cursor.getInt(2);
                int categoryId = cursor.getInt(3);
                int inout = cursor.getInt(4);
                int amount = cursor.getInt(5);

                dateText[i].setText(month + "/" + date);
                dateText[i].setTextSize(20);

                Cursor cursor1 = db.query("CategoryTable", new String[] {"id", "category", "amount"}, "id=" + categoryId, null, null, null, null);
                cursor1.moveToFirst();
                String categoryName = cursor1.getString(1);
                cursor1.close();

                categoryText[i].setText(categoryName);
                categoryText[i].setTextSize(20);

                switch (inout) {
                    case 0:
                        amountText[i].setText("-" + amount + "円");
                        break;
                    case 1:
                        amountText[i].setText("+" + amount + "円");
                        break;
                }
                amountText[i].setTextSize(24);

                dateText[i].setPadding(100, 0, 0, 0);
                dateText[i].setGravity(Gravity.RIGHT);
                categoryText[i].setGravity(Gravity.CENTER_HORIZONTAL);
                amountText[i].setGravity(Gravity.RIGHT);
                amountText[i].setPadding(0, 0, 100, 0);

                dateText[i].setWidth(300);
                categoryText[i].setWidth(400);
                amountText[i].setWidth(500);

                // payment[i].setWeightSum(5);
                // payment[i].setScaleX(0.5f);

//                LinearLayout.LayoutParams layoutParams1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
//                layoutParams1.weight = 1;
//                layoutParams2.weight = 2;
//                layoutParams3.weight = 2;
//                dateText[i].setLayoutParams(layoutParams1);
//                categoryText[i].setLayoutParams(layoutParams2);
//                amountText[i].setLayoutParams(layoutParams3);

//                dateText[i].setBackgroundColor(Color.RED);
//                categoryText[i].setBackgroundColor(Color.GREEN);
//                amountText[i].setBackgroundColor(Color.BLUE);

                payment[i].addView(dateText[i]);
                payment[i].addView(categoryText[i]);
                payment[i].addView(amountText[i]);
                varPaymentLayout.addView(payment[i]);

                cursor.moveToPrevious();
            }
            cursor.close();
        }

    }
}
