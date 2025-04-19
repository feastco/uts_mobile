package com.example.uts_a22202303006.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter untuk mengatur fragment dalam ViewPager (misal untuk tab layout).
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {

    // List untuk menyimpan fragment dan judulnya
    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitleList = new ArrayList<>();

    /**
     * Konstruktor ViewPagerAdapter
     * @param fm FragmentManager
     */
    public ViewPagerAdapter(FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
    }

    // Mengembalikan fragment berdasarkan posisi
    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragmentList.get(position);
    }

    // Mengembalikan jumlah total fragment
    @Override
    public int getCount() {
        return fragmentList.size();
    }

    // Menambahkan fragment dan judulnya ke dalam adapter
    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitleList.add(title);
    }

    // Mengembalikan judul untuk tab layout (opsional)
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitleList.get(position);
    }
}
