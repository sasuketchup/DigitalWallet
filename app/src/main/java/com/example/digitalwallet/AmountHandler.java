package com.example.digitalwallet;

import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.viewpager.widget.ViewPager;

import java.util.Calendar;

public class AmountHandler {

    // 収支を入力または上書き・削除するメソッド
    public void writeInOut(final Context context, final SQLiteDatabase db, View layout, LayoutInflater inflater, ViewGroup root, AlertDialog inputDialog, String[] categoryList, final int[] categoryID, final int latestPTID, final ViewPager viewPager) {

        layout = inflater.inflate(R.layout.input_dialog, root);

        // ダイアログのボタン取得
        final Button saveBtn = layout.findViewById(R.id.inputSaveBtn);
        final Button fixBtn = layout.findViewById(R.id.fixBtn);
        final Button deleteBtn = layout.findViewById(R.id.deleteBtn);

        // 修正・削除ボタン無効化
        fixBtn.setEnabled(false);
        deleteBtn.setEnabled(false);

        // ダイアログ生成
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("収支入力");
        builder.setView(layout);

        // スピナーセット
        ArrayAdapter<String> cateAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, categoryList);
        cateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        final Spinner cateSpinner = (Spinner) layout.findViewById(R.id.inputCateSpinner);
        cateSpinner.setAdapter(cateAdapter);

        builder.setNegativeButton(
                "キャンセル",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }
        );

        inputDialog = builder.show();

        // 保存ボタン
        final View finalLayout = layout;
        final AlertDialog finalInputDialog = inputDialog;
        saveBtn.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        // 日付取得
                        Calendar calendar = Calendar.getInstance();
                        int month = calendar.get(Calendar.MONTH) + 1;
                        int date = calendar.get(Calendar.DATE);

                        // 金額
                        int amount = 0;
                        EditText amountText = finalLayout.findViewById(R.id.inputAmount);
                        String amountSt = amountText.getText().toString();

                        // 金額を入力してください！！
                        if (amountSt.equals("")) {
                            Toast.makeText(context, "金額を入力してください！", Toast.LENGTH_LONG).show();
                        } else { // 金額が入力されている場合

                            amount = Integer.parseInt(amountSt);

                            // 選択されているアイテムからid取得
                            int index =cateSpinner.getSelectedItemPosition();

                            // エディットテキストから収支の内容取得
                            EditText detailText = finalLayout.findViewById(R.id.inputDetail);
                            String detail = detailText.getText().toString();
                            if (detail.equals("")) {
                                detail = "-";
                            }

                            // 残額計算のための金額
                            int inoutAmount = 0;

                            // 収支
                            int inout = 0;
                            RadioGroup radioGroup = finalLayout.findViewById(R.id.radioGroup);
                            int checkedId = radioGroup.getCheckedRadioButtonId();
                            switch (checkedId) {
                                case R.id.paymentRadio:
                                    inout = 0;
                                    inoutAmount = 0 - amount;
                                    break;
                                case R.id.incomeRadio:
                                    inout = 1;
                                    inoutAmount = amount;
                                    break;
                            }

                            // 該当カテゴリーの金額取得
                            Cursor cursor1 = db.query("CategoryTable", new String[] {"id", "category", "amount"}, "id=" + categoryID[index], null, null, null, null);
                            cursor1.moveToFirst();
                            int categoryAmount = cursor1.getInt(2);
                            cursor1.close();
                            // 残額計算
                            categoryAmount += inoutAmount;

                            // 保存(支払い履歴)
                            ContentValues contentValues1 = new ContentValues();
                            contentValues1.put("id", latestPTID);
                            contentValues1.put("month", month);
                            contentValues1.put("date", date);
                            contentValues1.put("category", categoryID[index]);
                            contentValues1.put("inout", inout);
                            contentValues1.put("amount", amount);
                            db.insert("PaymentTable", null, contentValues1);
                            // 保存(内容)
                            ContentValues contentValues3 = new ContentValues();
                            contentValues3.put("id", latestPTID);
                            contentValues3.put("detail", detail);
                            db.insert("DetailTable", null, contentValues3);
                            // 保存(カテゴリー)
                            ContentValues contentValues2 = new ContentValues();
                            contentValues2.put("amount", categoryAmount);
                            db.update("CategoryTable", contentValues2, "id=" + categoryID[index], null);

                            // ダイアログdismiss
                            finalInputDialog.dismiss();
                            // アクティビティ再起動(タブ保持)
                            Intent intent1 = new Intent(context, MainActivity.class);
                            intent1.putExtra("keep_item", viewPager.getCurrentItem());
                            ((MainActivity)context).finish();
                            ((MainActivity)context).overridePendingTransition(0, 0);
                            context.startActivity(intent1);
                            ((MainActivity)context).overridePendingTransition(0, 0);
                        }
                    }
                }
        );
    }
}
