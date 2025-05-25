// Đặt file này trong thư mục: com.example.videoapponandroid
package com.example.videoapponandroid;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings; // Import Settings
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

// Import các lớp Model và Adapter của bạn
import com.example.videoapponandroid.FolderAdapter;
import com.example.videoapponandroid.Folder;
import com.example.videoapponandroid.VideoAdapter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TwoFragment extends Fragment implements FolderAdapter.OnItemClickListener {

    private RecyclerView recyclerViewFolders;
    private FolderAdapter folderAdapter;
    private List<Folder> folderList;
    private TextView textViewNoFolders; // TextView hiển thị khi không có thư mục hoặc quyền

    public TwoFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_two, container, false);

        recyclerViewFolders = view.findViewById(R.id.recyclerViewFolders);
        recyclerViewFolders.setLayoutManager(new LinearLayoutManager(getContext()));
        textViewNoFolders = view.findViewById(R.id.textViewNoFolders);

        folderList = new ArrayList<>();
        folderAdapter = new FolderAdapter(folderList);
        folderAdapter.setOnItemClickListener(this);
        recyclerViewFolders.setAdapter(folderAdapter);

        // Gọi phương thức kiểm tra quyền và tải dữ liệu khi Fragment được tạo
        checkPermissionAndLoadData();

        return view;
    }

    // Phương thức này sẽ được gọi khi Fragment được resume
    // để kiểm tra lại quyền và tải dữ liệu nếu cần (ví dụ: sau khi người dùng cấp quyền từ cài đặt)
    @Override
    public void onResume() {
        super.onResume();
        checkPermissionAndLoadData();
    }

    private void checkPermissionAndLoadData() {
        if (getContext() == null) {
            return; // Đảm bảo Context không null
        }

        String permission;
        // Chọn quyền phù hợp dựa trên phiên bản Android
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // Android 13 (API 33) trở lên
            permission = Manifest.permission.READ_MEDIA_VIDEO;
        } else { // Android 12 (API 32) trở xuống
            permission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (ContextCompat.checkSelfPermission(getContext(), permission) != PackageManager.PERMISSION_GRANTED) {
            Log.d("TwoFragment", "Quyền truy cập bộ nhớ chưa được cấp cho Fragment.");
            // Quyền chưa được cấp: Hiển thị thông báo và hướng dẫn người dùng
            textViewNoFolders.setText("Ứng dụng cần quyền truy cập bộ nhớ để hiển thị video.\nVui lòng cấp quyền trong Cài đặt của ứng dụng.");
            textViewNoFolders.setVisibility(View.VISIBLE);
            recyclerViewFolders.setVisibility(View.GONE);

            // Thêm OnClickListener để người dùng có thể chạm và mở cài đặt ứng dụng
            textViewNoFolders.setOnClickListener(v -> {
                Log.d("TwoFragment", "Người dùng nhấp vào thông báo, mở cài đặt ứng dụng.");
                Toast.makeText(getContext(), "Vui lòng cấp quyền truy cập bộ nhớ trong Cài đặt của ứng dụng.", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getContext().getPackageName(), null);
                intent.setData(uri);
                startActivity(intent);
            });

        } else {
            Log.d("TwoFragment", "Quyền truy cập bộ nhớ đã được cấp cho Fragment. Bắt đầu tải thư mục.");
            textViewNoFolders.setOnClickListener(null); // Xóa listener nếu quyền đã có
            loadFolders(); // Nếu quyền đã có, tải dữ liệu
        }
    }

    // Không cần ghi đè onRequestPermissionsResult ở đây nữa,
    // vì MainActivity đã xử lý việc yêu cầu và kết quả quyền toàn cục.
    // Nếu bạn muốn Fragment phản ứng cụ thể với việc cấp quyền ngay lập tức
    // sau khi hộp thoại được đóng, bạn có thể gọi lại checkPermissionAndLoadData() trong onResume().


    private void loadFolders() {
        List<Folder> newFolderList = new ArrayList<>();
        Map<String, Folder> folderMap = new HashMap<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        String[] projection = {
                MediaStore.Video.Media.DATA,
                MediaStore.Video.Media.BUCKET_DISPLAY_NAME
        };
        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null;
        try {
            if (getContext() != null) {
                cursor = getContext().getContentResolver().query(uri, projection, null, null, sortOrder);

                if (cursor != null) {
                    Log.d("TwoFragment", "Tổng số video tìm thấy qua MediaStore: " + cursor.getCount());
                    while (cursor.moveToNext()) {
                        String videoPath = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA));
                        String folderName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.BUCKET_DISPLAY_NAME));

                        if (folderName == null || folderName.isEmpty()) {
                            File videoFile = new File(videoPath);
                            File parentDir = videoFile.getParentFile();
                            if (parentDir != null) {
                                folderName = parentDir.getName();
                            } else {
                                folderName = "Unknown Folder";
                            }
                        }

                        String folderPath = new File(videoPath).getParent();

                        if (folderMap.containsKey(folderPath)) {
                            Folder existingFolder = folderMap.get(folderPath);
                            existingFolder.setVideoCount(existingFolder.getVideoCount() + 1);
                        } else {
                            Folder newFolder = new Folder(folderName, folderPath, 1);
                            folderMap.put(folderPath, newFolder);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e("TwoFragment", "Lỗi khi tải thư mục từ MediaStore: " + e.getMessage(), e);
            // Hiển thị thông báo lỗi nếu có
            textViewNoFolders.setText("Đã xảy ra lỗi khi tải video: " + e.getLocalizedMessage() + "\nVui lòng thử lại hoặc kiểm tra quyền.");
            textViewNoFolders.setVisibility(View.VISIBLE);
            recyclerViewFolders.setVisibility(View.GONE);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        newFolderList.addAll(folderMap.values());
        folderAdapter.updateFolders(newFolderList);

        if (newFolderList.isEmpty()) {
            textViewNoFolders.setText("Không tìm thấy thư mục video nào trên thiết bị.");
            textViewNoFolders.setVisibility(View.VISIBLE);
            recyclerViewFolders.setVisibility(View.GONE);
            Log.d("TwoFragment", "Không tìm thấy thư mục video nào sau khi quét.");
        } else {
            textViewNoFolders.setVisibility(View.GONE);
            recyclerViewFolders.setVisibility(View.VISIBLE);
            Log.d("TwoFragment", "Đã tải thành công " + newFolderList.size() + " thư mục video.");
        }
    }

//    @Override
//    public void onItemClick(Folder folder) {
//        Toast.makeText(getContext(), "Bạn đã click vào thư mục: " + folder.getName() + " (" + folder.getVideoCount() + " video)", Toast.LENGTH_SHORT).show();
//        // Xử lý logic khi click vào thư mục (ví dụ: mở danh sách video)
//    }

    @Override
    public void onItemClick(Folder folder) {
        Toast.makeText(getContext(), "Bạn đã click vào thư mục: " + folder.getName() + " (" + folder.getVideoCount() + " video)", Toast.LENGTH_SHORT).show();
        // Cập nhật: Khởi chạy FolderVideoListActivity và truyền đường dẫn thư mục
        Intent intent = new Intent(getContext(), FolderVideoListActivity.class);
        intent.putExtra("FOLDER_PATH", folder.getPath()); // Truyền đường dẫn thư mục
        intent.putExtra("FOLDER_NAME", folder.getName()); // Truyền tên thư mục để hiển thị trên toolbar
        startActivity(intent);
    }

}


