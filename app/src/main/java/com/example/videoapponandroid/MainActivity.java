package com.example.videoapponandroid;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout; // <-- Import LinearLayout
import android.widget.TextView;
import android.widget.Toast;
import android.view.inputmethod.InputMethodManager;
import android.view.KeyEvent;
import android.view.ViewGroup; // Import ViewGroup

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity implements SortOptionsBottomSheet.SortOptionListener {

    private static final int PERMISSION_REQUEST_CODE = 100;
    private ImageButton btnSearch;
    private ImageButton btnBackFromSearch;
    private EditText searchEditText;
    private TextView toolbarTitle;
    private LinearLayout rightButtonsContainer; // <-- Khai báo container cho các nút phải
    private DemoPagerAdapter pagerAdapter;
    private ViewPager2 viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton btnSort = findViewById(R.id.btn_sort);
        btnSearch = findViewById(R.id.btn_search);
        ImageButton btnSettings = findViewById(R.id.btn_settings);
        btnBackFromSearch = findViewById(R.id.btnBackFromSearch);
        rightButtonsContainer = findViewById(R.id.right_buttons_container); // <-- Ánh xạ container

        // Khởi tạo EditText cho tìm kiếm
        searchEditText = new EditText(this);
        searchEditText.setLayoutParams(new Toolbar.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, // Chiếm hết chiều rộng còn lại
                ViewGroup.LayoutParams.WRAP_CONTENT // Chiều cao tự động
        ));
        searchEditText.setHint("Tìm kiếm video...");
        searchEditText.setTextColor(ContextCompat.getColor(this, android.R.color.white));
        searchEditText.setHintTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        searchEditText.setSingleLine(true); // Đảm bảo chỉ trên một dòng

        // Thêm searchEditText vào Toolbar (vị trí sau btnBackFromSearch, trước toolbar_title và rightButtonsContainer)
        // Chúng ta sẽ quản lý vị trí bằng cách ẩn/hiện các views khác
        toolbar.addView(searchEditText); // Thêm vào cuối Toolbar để dễ quản lý thứ tự hiển thị
        searchEditText.setVisibility(View.GONE); // Ẩn ban đầu

        // Để searchEditText chiếm đúng không gian khi hiển thị, chúng ta cần đặt nó trong code
        // Nó sẽ được hiển thị khi toolbarTitle và rightButtonsContainer ẩn đi

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == android.view.inputmethod.EditorInfo.IME_ACTION_SEARCH || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch(searchEditText.getText().toString());
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if (imm != null) {
                    imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
                }
                return true;
            }
            return false;
        });

        btnSort.setOnClickListener(v -> showSortOptionsBottomSheet());
        btnSearch.setOnClickListener(v -> toggleSearchBar());
        btnSettings.setOnClickListener(v -> Toast.makeText(MainActivity.this, "Nút Cài đặt được nhấn!", Toast.LENGTH_SHORT).show());
        btnBackFromSearch.setOnClickListener(v -> toggleSearchBar()); // Đóng thanh tìm kiếm khi nhấn nút quay lại

        viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        pagerAdapter = new DemoPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> tab.setText(pagerAdapter.getPageTitle(position))
        ).attach();

        checkAndRequestPermissions();
    }

    private void toggleSearchBar() {
        if (searchEditText.getVisibility() == View.VISIBLE) {
            // Đang hiển thị thanh tìm kiếm -> Ẩn đi
            searchEditText.setVisibility(View.GONE);
            btnBackFromSearch.setVisibility(View.GONE);
            toolbarTitle.setVisibility(View.VISIBLE);
            rightButtonsContainer.setVisibility(View.VISIBLE); // Hiển thị lại container nút bên phải

            searchEditText.setText("");
            performSearch(""); // Đảm bảo làm mới danh sách khi ẩn thanh tìm kiếm
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);
            }
        } else {
            // Đang ẩn thanh tìm kiếm -> Hiển thị lên
            searchEditText.setVisibility(View.VISIBLE);
            btnBackFromSearch.setVisibility(View.VISIBLE);
            toolbarTitle.setVisibility(View.GONE);
            rightButtonsContainer.setVisibility(View.GONE); // Ẩn container nút bên phải

            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
            }
        }
    }

    private void performSearch(String query) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof OneFragment) {
            ((OneFragment) currentFragment).filterVideos(query);
        } else {
            Toast.makeText(this, "Chức năng tìm kiếm chỉ khả dụng trên tab Video.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showSortOptionsBottomSheet() {
        SortOptionsBottomSheet sortOptionsBottomSheet = new SortOptionsBottomSheet();
        sortOptionsBottomSheet.setSortOptionListener(this);
        sortOptionsBottomSheet.show(getSupportFragmentManager(), sortOptionsBottomSheet.getTag());
    }

    @Override
    public void onSortOptionSelected(SortOptionsBottomSheet.SortOption sortOption) {
        Fragment currentFragment = getSupportFragmentManager().findFragmentByTag("f" + viewPager.getCurrentItem());
        if (currentFragment instanceof OneFragment) {
            ((OneFragment) currentFragment).sortVideos(sortOption);
        } else {
            Toast.makeText(this, "Chức năng sắp xếp chỉ khả dụng trên tab Video.", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkAndRequestPermissions() {
        String permission;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else {
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            Toast.makeText(this, "Quyền truy cập bộ nhớ đã được cấp.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Quyền truy cập bộ nhớ được cấp!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối. Không thể hiển thị video.", Toast.LENGTH_LONG).show();
            }
        }
    }
}