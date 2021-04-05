package com.example.digitalwallet;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.digitalwallet.ui.main.OriginalPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import org.w3c.dom.Text;

import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    LinearLayout varCategoryLayout;

    int category = 0;

    // 支払いテーブルの最新のid
    int latestPTID = 0;

    View layout;
    View allocateLayout;

    // 割り当て金額
    int allocateAmount = 0;

    AlertDialog inputDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Intent intent = getIntent();
        int tab_item = intent.getIntExtra("keep_item", 0);

        OriginalPagerAdapter adapter = new OriginalPagerAdapter(getSupportFragmentManager());
        final ViewPager viewPager = findViewById(R.id.viewPager);
        viewPager.setAdapter(adapter);

        viewPager.setCurrentItem(tab_item);

        TabLayout tabLayout = findViewById(R.id.mainTab);
        tabLayout.setupWithViewPager(viewPager);

        final LayoutInflater inflater = (LayoutInflater)this.getSystemService(LAYOUT_INFLATER_SERVICE);

        MyOpenHelper helper = new MyOpenHelper(this);
        final SQLiteDatabase db = helper.getWritableDatabase();

        // カテゴリーテーブルの行数取得
        long countCT = DatabaseUtils.queryNumEntries(db, "CategoryTable");
        // カテゴリーテーブルが空のとき、初期状態として"未分類"を挿入
        if (countCT == 0) {
            ContentValues contentValues = new ContentValues();
            contentValues.put("id", 0);
            contentValues.put("category", "未分類");
            contentValues.put("amount", 0);
            db.insert("CategoryTable", null, contentValues);
        }

        // 所持金合計の変数
        int totalAmount = 0;
        // カテゴリーテーブルの行数再取得
        countCT = DatabaseUtils.queryNumEntries(db, "CategoryTable");
        // カテゴリーのidを格納する配列(削除した場合にidとindexが一致しないことがあるため)
        final int[] categoryID = new int[(int) countCT];
        // カテゴリー名を格納する配列
        final String[] categoryList = new String[(int) countCT];
        // カテゴリーの残額を格納する配列
        final int[] cateAmount = new int[(int) countCT];
        // すべてのカテゴリーと残額取得
        Cursor cursor0 = db.query("CategoryTable", new String[] {"id", "category", "amount"}, null, null, null, null, null);
        cursor0.moveToFirst();
        // 合計金額計算&カテゴリー名とidを配列に格納
        for (int i=0; i<countCT; i++) {
            categoryID[i] = cursor0.getInt(0);
            categoryList[i] = cursor0.getString(1);
            cateAmount[i] = cursor0.getInt(2);
            totalAmount += cateAmount[i];
            cursor0.moveToNext();
        }
        cursor0.close();
        // 所持金合計表示
        TextView totalText = findViewById(R.id.totalAmount);
        totalText.setText(String.valueOf(totalAmount));

        // 支払いテーブルの行数取得
        long countPT = DatabaseUtils.queryNumEntries(db, "PaymentTable");
        // テーブルが空でないとき、最新のid取得
        if (countPT > 0) {
            Cursor cursor = db.query("PaymentTable", new String[] {"id", "month", "date", "category", "inout", "amount"}, null, null, null, null, null);
            cursor.moveToLast();
            latestPTID = cursor.getInt(0) + 1;
            cursor.close();
        }

        // 所持金合計ボタン(割り当て)
        findViewById(R.id.totalBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        allocateLayout = inflater.inflate(R.layout.allocate_dialog, (ViewGroup)findViewById(R.id.allocate_root));
                        // ダイアログ生成
                        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("所持金割り当て");
                        builder.setView(allocateLayout);

                        // 金額のエディットテキスト取得
                        final EditText allocateAmountText = allocateLayout.findViewById(R.id.allocateAmount);

                        // 各種TextView取得
                        final TextView cateText1 = allocateLayout.findViewById(R.id.cateText1);
                        final TextView cateText2 = allocateLayout.findViewById(R.id.cateText2);
                        final TextView beforeAmountText1 = allocateLayout.findViewById(R.id.beforeAmount1);
                        final TextView beforeAmountText2 = allocateLayout.findViewById(R.id.beforeAmount2);
                        final TextView afterAmountText1 = allocateLayout.findViewById(R.id.afterAmount1);
                        final TextView afterAmountText2 = allocateLayout.findViewById(R.id.afterAmount2);

                        // ダイアログを開くときまず未分類と残額を表示
                        cateText1.setText(categoryList[0]);
                        cateText2.setText(categoryList[0]);
                        beforeAmountText1.setText(cateAmount[0] + "円");
                        beforeAmountText2.setText(cateAmount[0] + "円");
                        afterAmountText1.setText(cateAmount[0] + "円");
                        afterAmountText2.setText(cateAmount[0] + "円");

                        // スピナーセット
                        ArrayAdapter<String> cateAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categoryList);
                        cateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        // 割り当て元
                        final Spinner cateSpinner1 = (Spinner) allocateLayout.findViewById(R.id.cateSpinner1);
                        cateSpinner1.setAdapter(cateAdapter);
                        // 割り当て先
                        final Spinner cateSpinner2 = (Spinner) allocateLayout.findViewById(R.id.cateSpinner2);
                        cateSpinner2.setAdapter(cateAdapter);

                        // スピナーの項目が変更されたとき(割り当て元)
                        cateSpinner1.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        Spinner spinner = (Spinner) parent;
                                        String item = (String) spinner.getSelectedItem();
                                        cateText1.setText(item);

                                        if (allocateAmountText.getText().toString().equals("")) {
                                            allocateAmount = 0;
                                        } else {
                                            allocateAmount = Integer.parseInt(allocateAmountText.getText().toString());
                                        }

                                        int index1 = spinner.getSelectedItemPosition();
                                        int index2 = cateSpinner2.getSelectedItemPosition();
                                        // 割り当て先と同じカテゴリーの場合0
                                        if (index1 == index2) {
                                            allocateAmount = 0;
                                        }

                                        int afterAmount1 = cateAmount[index1] - allocateAmount;
                                        int afterAmount2 = cateAmount[index2] + allocateAmount;
                                        beforeAmountText1.setText(cateAmount[index1] + "円");
                                        afterAmountText1.setText(afterAmount1 + "円");
                                        afterAmountText2.setText(afterAmount2 + "円");
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                }
                        );

                        // スピナーの項目が変更されたとき(割り当て先)
                        cateSpinner2.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    @Override
                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                                        Spinner spinner = (Spinner) parent;
                                        String item = (String) spinner.getSelectedItem();
                                        cateText2.setText(item);

                                        if (allocateAmountText.getText().toString().equals("")) {
                                            allocateAmount = 0;
                                        } else {
                                            allocateAmount = Integer.parseInt(allocateAmountText.getText().toString());
                                        }

                                        int index2 = spinner.getSelectedItemPosition();
                                        int index1 = cateSpinner1.getSelectedItemPosition();
                                        // 割り当て元と同じカテゴリーの場合0
                                        if (index2 == index1) {
                                            allocateAmount = 0;
                                        }

                                        int afterAmount2 = cateAmount[index2] + allocateAmount;
                                        int afterAmount1 = cateAmount[index1] - allocateAmount;
                                        beforeAmountText2.setText(cateAmount[index2] + "円");
                                        afterAmountText2.setText(afterAmount2 + "円");
                                        afterAmountText1.setText(afterAmount1 + "円");
                                    }

                                    @Override
                                    public void onNothingSelected(AdapterView<?> parent) {

                                    }
                                }
                        );

                        // OKボタン
                        builder.setPositiveButton(
                                "OK",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                }
                        );

                        // キャンセルボタン
                        builder.setNegativeButton(
                                "キャンセル",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                        builder.show();
                    }
                }
        );

        // 収支入力ボタン
        findViewById(R.id.inputBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        layout = inflater.inflate(R.layout.input_dialog, (ViewGroup)findViewById(R.id.input_root));

                        // ダイアログのボタン取得
                        final Button saveBtn = layout.findViewById(R.id.inputSaveBtn);
                        final Button fixBtn = layout.findViewById(R.id.fixBtn);
                        final Button deleteBtn = layout.findViewById(R.id.deleteBtn);

                        // 修正・削除ボタン無効化
                        fixBtn.setEnabled(false);
                        deleteBtn.setEnabled(false);

                        // ダイアログ生成
                        final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                        builder.setTitle("収支入力");
                        builder.setView(layout);

                        // スピナーセット
                        ArrayAdapter<String> cateAdapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_item, categoryList);
                        cateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                        final Spinner cateSpinner = (Spinner) layout.findViewById(R.id.inputCateSpinner);
                        cateSpinner.setAdapter(cateAdapter);

                        // 保存ボタン
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
                                        EditText amountText = layout.findViewById(R.id.inputAmount);
                                        CharSequence amountSt = amountText.getText();

                                        // 金額を入力してください！！
                                        if (amountSt.toString().equals("")) {
                                            Toast.makeText(MainActivity.this, "金額を入力してください！", Toast.LENGTH_LONG).show();
                                        } else { // 金額が入力されている場合

                                            amount = Integer.parseInt(String.valueOf(amountSt));

                                            // 選択されているアイテムからid取得
                                            int index =cateSpinner.getSelectedItemPosition();
                                            category = categoryID[index];

                                            // 残額計算のための金額
                                            int inoutAmount = 0;

                                            // 収支
                                            int inout = 0;
                                            RadioGroup radioGroup = layout.findViewById(R.id.radioGroup);
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
                                            Cursor cursor1 = db.query("CategoryTable", new String[] {"id", "category", "amount"}, "id=" + category, null, null, null, null);
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
                                            contentValues1.put("category", category);
                                            contentValues1.put("inout", inout);
                                            contentValues1.put("amount", amount);
                                            db.insert("PaymentTable", null, contentValues1);
                                            // 保存(カテゴリー)
                                            ContentValues contentValues2 = new ContentValues();
                                            contentValues2.put("amount", categoryAmount);
                                            db.update("CategoryTable", contentValues2, "id=" + category, null);

                                            // ダイアログdismiss
                                            inputDialog.dismiss();
                                            // アクティビティ再起動(タブ保持)
                                            Intent intent1 = new Intent(MainActivity.this, MainActivity.class);
                                            intent1.putExtra("keep_item", viewPager.getCurrentItem());
                                            finish();
                                            overridePendingTransition(0, 0);
                                            startActivity(intent1);
                                            overridePendingTransition(0, 0);
                                        }
                                    }
                                }
                        );

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
                    }
                }
        );

    }
}
