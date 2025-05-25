// Đặt file này trong thư mục: com.example.videoapponandroid
package com.example.videoapponandroid;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class FolderVideoListActivity extends AppCompatActivity {

    private RecyclerView recyclerViewFolderVideos;
    private VideoAdapter videoAdapter;
    private List<VideoFile> videoList;
    private TextView textViewNoVideos;
    private String folderPath;
    private String folderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_folder_video_list); // Tạo layout này

        // Lấy đường dẫn thư mục và tên thư mục từ Intent
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("FOLDER_PATH")) {
            folderPath = intent.getStringExtra("FOLDER_PATH");
            folderName = intent.getStringExtra("FOLDER_NAME");
        }

        if (folderPath == null || folderPath.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy đường dẫn thư mục.", Toast.LENGTH_SHORT).show();
            finish(); // Đóng Activity nếu không có đường dẫn thư mục
            return;
        }

        // Thiết lập Toolbar
        Toolbar toolbar = findViewById(R.id.toolbar_folder_videos); // Tạo ID này trong layout
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút quay lại
            getSupportActionBar().setTitle(folderName != null ? folderName : "Video trong Thư mục"); // Đặt tiêu đề Toolbar
        }

        // Xử lý sự kiện nút quay lại trên Toolbar
        toolbar.setNavigationOnClickListener(v -> finish());

        recyclerViewFolderVideos = findViewById(R.id.recyclerViewFolderVideos);
        recyclerViewFolderVideos.setLayoutManager(new LinearLayoutManager(this));
        textViewNoVideos = findViewById(R.id.textViewNoVideosInFolder);

        videoList = new ArrayList<>();
        // Truyền getChildFragmentManager() hoặc getSupportFragmentManager() tùy theo cách bạn xử lý BottomSheet
        // Ở đây là Activity, nên dùng getSupportFragmentManager()
        videoAdapter = new VideoAdapter(this, videoList, getSupportFragmentManager());
        recyclerViewFolderVideos.setAdapter(videoAdapter);

        loadVideosFromFolder(folderPath); // Tải video từ thư mục cụ thể
    }

    private void loadVideosFromFolder(String path) {
        videoList.clear(); // Xóa danh sách hiện tại trước khi tải lại

        ContentResolver contentResolver = getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED
        };

        // Lọc video theo đường dẫn thư mục
        String selection = MediaStore.Video.Media.DATA + " LIKE ?";
        String[] selectionArgs = new String[]{path + "%"}; // Lọc các video có đường dẫn bắt đầu bằng đường dẫn thư mục

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, selection, selectionArgs, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                do {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String videoFullPath = cursor.getString(pathColumn);
                    long duration = cursor.getLong(durationColumn);
                    long dateAddedTimestamp = cursor.getLong(dateAddedColumn);

                    // Kiểm tra xem video có thực sự nằm trong thư mục con hay không
                    // Điều này cần thiết vì LIKE '%' có thể trả về các đường dẫn con không mong muốn
                    if (videoFullPath != null && videoFullPath.startsWith(path + File.separator)) {
                        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                        String creationTime = formatter.format(new Date(dateAddedTimestamp * 1000));
                        Uri contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                        VideoFile videoFile = new VideoFile(id, title, videoFullPath, duration, contentUri, creationTime);
                        videoList.add(videoFile);
                    }
                } while (cursor.moveToNext());
            } else {
                Log.d("FolderVideoListActivity", "Không tìm thấy video nào trong thư mục: " + path);
            }
        } catch (Exception e) {
            Log.e("FolderVideoListActivity", "Lỗi khi truy xuất video từ thư mục: " + e.getMessage(), e);
            textViewNoVideos.setText("Đã xảy ra lỗi khi tải video từ thư mục: " + e.getLocalizedMessage());
            textViewNoVideos.setVisibility(View.VISIBLE);
            recyclerViewFolderVideos.setVisibility(View.GONE);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        videoAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView sau khi tải dữ liệu
        if (videoList.isEmpty()) {
            textViewNoVideos.setText("Không tìm thấy video nào trong thư mục này.");
            textViewNoVideos.setVisibility(View.VISIBLE);
            recyclerViewFolderVideos.setVisibility(View.GONE);
        } else {
            textViewNoVideos.setVisibility(View.GONE);
            recyclerViewFolderVideos.setVisibility(View.VISIBLE);
            Log.d("FolderVideoListActivity", "Đã tải thành công " + videoList.size() + " video từ thư mục: " + path);
        }
    }
}