package com.example.videoapponandroid;

import android.Manifest; // Import cho quyền truy cập bộ nhớ
import android.content.pm.PackageManager;
import android.os.Build; // Import để kiểm tra phiên bản Android
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast; // Import cho Toast

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar; // Import đúng Toolbar
import androidx.core.app.ActivityCompat; // Import cho ActivityCompat
import androidx.core.content.ContextCompat; // Import cho ContextCompat
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 100; // Mã yêu cầu quyền

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); // THAMC CHIẾU TỚI FILE XML ACTIVITY_MAIN

       // THAM CHIẾU TỚI TOOLBAR
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar); // ĐẶT LÀM ACTIVITY CHÍNH CỦA ACTIONBAR

        TextView toolbarTitle = findViewById(R.id.toolbar_title);
        ImageButton btnSort = findViewById(R.id.btn_sort);
        ImageButton btnSearch = findViewById(R.id.btn_search);
        ImageButton btnSettings = findViewById(R.id.btn_settings);

        // Đặt lắng nghe sự kiện (OnClickListener) cho các nút trên Toolbar
        btnSort.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Nút Sắp xếp được nhấn!", Toast.LENGTH_SHORT).show();
                // TODO: Thêm logic xử lý sắp xếp của bạn tại đây
            }
        });

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Nút Tìm kiếm được nhấn!", Toast.LENGTH_SHORT).show();
                // TODO: Thêm logic xử lý tìm kiếm của bạn tại đây
            }
        });

        btnSettings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Nút Cài đặt được nhấn!", Toast.LENGTH_SHORT).show();
                // TODO: Thêm logic xử lý cài đặt của bạn tại đây
            }
        });

        // THIẾT LẬP VIEWPAGER2 VÀ TABLAYOUT
        ViewPager2 viewPager = findViewById(R.id.pager);
        TabLayout tabLayout = findViewById(R.id.tab_layout);

        // Adapter này sẽ chứa và quản lý các Fragment (Trang Một, Trang Hai)
        DemoPagerAdapter pagerAdapter = new DemoPagerAdapter(this); //khởi tạo ADEPTER
        viewPager.setAdapter(pagerAdapter);

        // 2. Liên kết TabLayout với ViewPager2 bằng TabLayoutMediator
        // Điều này đảm bảo khi vuốt ViewPager2, tab sẽ thay đổi và ngược lại
        new TabLayoutMediator(tabLayout, viewPager,
                new TabLayoutMediator.TabConfigurationStrategy() {
                    @Override
                    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
                        // Đặt văn bản cho mỗi tab dựa trên tiêu đề từ Adapter
                        tab.setText(pagerAdapter.getPageTitle(position));
                    }
                }
        ).attach(); // Rất quan trọng: Gắn TabLayoutMediator để kích hoạt đồng bộ hóa

        // KIỂM TRA VÀ YÊU CẦU QUYỀN ĐỌC BỘ NHỚ
        checkAndRequestPermissions();
    }


     // Kiểm tra và yêu cầu quyền đọc bộ nhớ (READ_EXTERNAL_STORAGE hoặc READ_MEDIA_VIDEO).
    private void checkAndRequestPermissions() {
        String permission;
        // Chọn quyền phù hợp dựa trên phiên bản Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) trở lên
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else { // Android 12 (API 32) trở xuống
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        // Kiểm tra xem quyền đã được cấp chưa
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            // Nếu chưa, yêu cầu quyền từ người dùng
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_CODE);
        } else {
            // Nếu quyền đã được cấp, bạn có thể thông báo cho Fragment One để tải video
            // Hoặc Fragment One có thể tự kiểm tra quyền khi nó được tạo ra.
            // Trong demo này, OneFragment sẽ tự động kiểm tra và tải khi onCreateView được gọi.
            Toast.makeText(this, "Quyền truy cập bộ nhớ đã được cấp.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Xử lý kết quả yêu cầu quyền từ người dùng.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Quyền được cấp
                Toast.makeText(this, "Quyền truy cập bộ nhớ được cấp!", Toast.LENGTH_SHORT).show();
                // Không cần làm gì thêm ở đây, OneFragment sẽ tự tải video khi nó khởi tạo.
            } else {
                // Quyền bị từ chối
                Toast.makeText(this, "Quyền truy cập bộ nhớ bị từ chối. Không thể hiển thị video.", Toast.LENGTH_LONG).show();
            }
        }
    }
}