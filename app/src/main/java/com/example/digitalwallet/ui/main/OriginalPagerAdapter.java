package com.example.digitalwallet.ui.main;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.digitalwallet.CategoryTab;
import com.example.digitalwallet.PaymentTab;

public class OriginalPagerAdapter extends FragmentPagerAdapter {

    private CharSequence[] tab_Titles = {"収支履歴", "カテゴリー一覧"};

    public OriginalPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new PaymentTab();
                break;
            case 1:
                fragment = new CategoryTab();
                break;
        }
        return fragment;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return tab_Titles[position];
    }

    @Override
    public int getCount() {
        return tab_Titles.length;
    }
}
