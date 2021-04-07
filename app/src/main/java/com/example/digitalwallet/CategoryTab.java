package com.example.digitalwallet;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.digitalwallet.ui.main.OriginalPagerAdapter;

public class CategoryTab extends Fragment {

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.tab_category,container,false);
    }

    View layout;

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        MyOpenHelper helper = new MyOpenHelper(getContext());
        final SQLiteDatabase db = helper.getWritableDatabase();

        // カテゴリーテーブルの行数取得
        long countCT = DatabaseUtils.queryNumEntries(db, "CategoryTable");

        Cursor cursor = db.query("CategoryTable", new String[] {"id", "category", "amount"}, null, null, null, null, null);

        cursor.moveToFirst();

        LinearLayout varCategoryLayout = view.findViewById(R.id.categoryLayout);
        LinearLayout[] category = new LinearLayout[(int) countCT];

        Button[] categoryBtn = new Button[(int) countCT];
        TextView[] categoryAmountView = new TextView[(int) countCT];

        // 未分類を先頭に固定
        category[0] = new LinearLayout(getContext());
        category[0].setOrientation(LinearLayout.HORIZONTAL);
        categoryBtn[0] = new Button(getContext());
        categoryAmountView[0] = new TextView(getContext());

        String categoryName0 = cursor.getString(1);
        int categoryAmount0 = cursor.getInt(2);

        // 残額によって色分け
        int amountColor0 = Color.GRAY;
        if (categoryAmount0 > 0) {
            amountColor0 = Color.GREEN;
        } else if (categoryAmount0 < 0) {
            amountColor0 = Color.RED;
        }

        categoryBtn[0].setText(categoryName0);
        categoryBtn[0].setPadding(100, 0, 0, 0);
        categoryAmountView[0].setText(categoryAmount0 + "円");
        categoryAmountView[0].setTextColor(amountColor0);
        categoryAmountView[0].setTextSize(24);
        categoryAmountView[0].setGravity(Gravity.RIGHT);
        categoryAmountView[0].setPadding(0, 0, 100, 0);

        LinearLayout.LayoutParams layoutParams0 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
        layoutParams0.weight = 1;
        categoryBtn[0].setLayoutParams(layoutParams0);
        categoryAmountView[0].setLayoutParams(layoutParams0);

        category[0].addView(categoryBtn[0]);
        category[0].addView(categoryAmountView[0]);
        varCategoryLayout.addView(category[0]);

        // 残りは最新から表示
        cursor.moveToLast();
        // カテゴリー一覧を表示
        for (int i=1; i<countCT; i++) {
            category[i] = new LinearLayout(getContext());
            category[i].setOrientation(LinearLayout.HORIZONTAL);
            categoryBtn[i] = new Button(getContext());
            categoryAmountView[i] = new TextView(getContext());

            String categoryName = cursor.getString(1);
            int categoryAmount = cursor.getInt(2);

            // 残額によって色分け
            int amountColor = Color.GRAY;
            if (categoryAmount > 0) {
                amountColor = Color.GREEN;
            } else if (categoryAmount < 0) {
                amountColor = Color.RED;
            }

            categoryBtn[i].setText(categoryName);
            categoryBtn[i].setPadding(100, 0, 0, 0);
            categoryAmountView[i].setText(categoryAmount + "円");
            categoryAmountView[i].setTextColor(amountColor);
            categoryAmountView[i].setTextSize(24);
            categoryAmountView[i].setGravity(Gravity.RIGHT);
            categoryAmountView[i].setPadding(0, 0, 100, 0);

            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            layoutParams.weight = 1;
            categoryBtn[i].setLayoutParams(layoutParams);
            categoryAmountView[i].setLayoutParams(layoutParams);

            category[i].addView(categoryBtn[i]);
            category[i].addView(categoryAmountView[i]);
            varCategoryLayout.addView(category[i]);

            cursor.moveToPrevious();
        }
        cursor.close();

        // カテゴリー追加ダイアログ表示のためのレイアウトインフレータ
        final LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        // カテゴリー追加ボタン
        view.findViewById(R.id.addCateBtn).setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        layout = inflater.inflate(R.layout.add_category_dialog, (ViewGroup) view.findViewById(R.id.addCate_root));

                        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                        builder.setTitle("カテゴリー追加");
                        builder.setView(layout);

                        builder.setPositiveButton("追加", null);

                        builder.setNegativeButton(
                                "キャンセル",
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                }
                        );

                        final AlertDialog addCateDialog = builder.show();

                        Button addBtn = addCateDialog.getButton(DialogInterface.BUTTON_POSITIVE);

                        addBtn.setOnClickListener(
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {

                                        // エディットテキストからカテゴリー名を取得
                                        String newCategoryName;
                                        EditText newCategoryText = layout.findViewById(R.id.inputCateName);
                                        newCategoryName = newCategoryText.getText().toString();

                                        // 空の場合
                                        if (newCategoryName.equals("")) {
                                            // カテゴリー名を入力してください！
                                            Toast.makeText(getContext(), "カテゴリー名を入力してください！", Toast.LENGTH_LONG).show();
                                        } else { // カテゴリー名が入力されている場合
                                            Cursor cursor1 = db.query("CategoryTable", new String[] {"id", "category", "amount"}, null, null, null, null, null);
                                            // 一番新しいカテゴリーのIDを取得
                                            cursor1.moveToLast();
                                            int latestID = cursor1.getInt(0);
                                            cursor1.close();
                                            // 保存
                                            ContentValues contentValues = new ContentValues();
                                            contentValues.put("id", latestID + 1);
                                            contentValues.put("category", newCategoryName);
                                            contentValues.put("amount", 0);
                                            db.insert("CategoryTable", null, contentValues);

                                            addCateDialog.dismiss();

                                            // アクティビティ再起動(タブ保持)
                                            Intent intent = new Intent(getActivity(), MainActivity.class);
                                            intent.putExtra("keep_item", 1);
                                            getActivity().finish();
                                            getActivity().overridePendingTransition(0, 0);
                                            startActivity(intent);
                                            getActivity().overridePendingTransition(0, 0);

                                            // getFragmentManager().beginTransaction().replace(R.id.viewPager, OriginalPagerAdapter).commit();
                                        }
                                    }
                                }
                        );
                    }
                }
        );
    }
}
