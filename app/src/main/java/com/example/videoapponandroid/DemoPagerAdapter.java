package com.example.videoapponandroid; // Đảm bảo đúng package của bạn

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;
import java.util.List;

public class DemoPagerAdapter extends FragmentStateAdapter {

    private final List<Fragment> fragmentList = new ArrayList<>();
    private final List<String> fragmentTitles = new ArrayList<>();

    public DemoPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        // Thêm các Fragment và tiêu đề vào danh sách
        addFragment(new OneFragment(), "Video");
        addFragment(new TwoFragment(), "Thư mục");
        addFragment(new ThreeFragment(), "Danh sách phát");
    }

    public void addFragment(Fragment fragment, String title) {
        fragmentList.add(fragment);
        fragmentTitles.add(title);
    }

    @Override
    public int getItemCount() {
        return fragmentList.size();
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return fragmentList.get(position);
    }

    public String getPageTitle(int position) {
        return fragmentTitles.get(position);
    }
}