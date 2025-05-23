package com.example.videoapponandroid;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment; // Import này để dùng cho dismiss

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

// OneFragment cần implement VideoOptionsBottomSheet.VideoOptionListener
public class OneFragment extends Fragment implements VideoOptionsBottomSheet.VideoOptionListener {

    private RecyclerView recyclerView;
    private VideoAdapter videoAdapter;
    private List<VideoFile> videoList;
    private FloatingActionButton fabAddVideo;

    private static final int REQUEST_CODE_CAPTURE_VIDEO = 1;
    private static final int REQUEST_CODE_PICK_VIDEO = 2;
    private static final int PERMISSION_REQUEST_CODE = 100;
    private static final int DELETE_PERMISSION_REQUEST_CODE = 101; // Mã request mới, phải khớp với VideoOptionsBottomSheet

    public OneFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_one, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewVideos);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        videoList = new ArrayList<>();
        // Truyền getChildFragmentManager() và đảm bảo VideoAdapter cũng cần Context và FragmentManager
        // VideoAdapter sẽ cần FragmentManager để show VideoOptionsBottomSheet
        videoAdapter = new VideoAdapter(getContext(), videoList, getChildFragmentManager());
        recyclerView.setAdapter(videoAdapter);

        fabAddVideo = view.findViewById(R.id.fabAddVideo);

        fabAddVideo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddVideoOptionsBottomSheet bottomSheet = new AddVideoOptionsBottomSheet();
                bottomSheet.show(getChildFragmentManager(), bottomSheet.getTag());
            }
        });

        loadVideos(); // Gọi hàm loadVideos để kiểm tra và yêu cầu quyền

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Xử lý kết quả trả về từ các Activity khác (ví dụ: Camera, Gallery, hoặc yêu cầu xóa MediaStore)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CODE_CAPTURE_VIDEO || requestCode == REQUEST_CODE_PICK_VIDEO) {
                Uri videoUri = null;
                if (data != null) {
                    videoUri = data.getData();
                }

                if (videoUri != null) {
                    if (requestCode == REQUEST_CODE_CAPTURE_VIDEO) {
                        Toast.makeText(getContext(), "Video mới từ Camera đã được ghi: " + videoUri.toString(), Toast.LENGTH_LONG).show();
                    } else if (requestCode == REQUEST_CODE_PICK_VIDEO) {
                        Toast.makeText(getContext(), "Video đã chọn từ Thư viện/File Manager: " + videoUri.toString(), Toast.LENGTH_LONG).show();
                    }

                    // Chuyển hướng đến VideoPlayerActivity để phát video đã chọn/quay
                    Intent playIntent = new Intent(getContext(), VideoPlayerActivity.class);
                    playIntent.setData(videoUri);
                    startActivity(playIntent);

                    // Sau khi quay/chọn video, tải lại danh sách để cập nhật RecyclerView
                    loadVideos();
                } else {
                    Toast.makeText(getContext(), "Không lấy được URI video.", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == DELETE_PERMISSION_REQUEST_CODE) {
                // Xử lý kết quả từ yêu cầu xóa MediaStore (trên Android 10+).
                // Nếu resultCode là RESULT_OK, người dùng đã đồng ý xóa.
                Toast.makeText(getContext(), "Video đã được xóa thành công.", Toast.LENGTH_SHORT).show();
                loadVideos(); // Tải lại danh sách sau khi xóa

                // Tìm và đóng VideoOptionsBottomSheet nếu nó vẫn đang hiển thị
                BottomSheetDialogFragment bottomSheet = (BottomSheetDialogFragment) getChildFragmentManager().findFragmentByTag(VideoOptionsBottomSheet.TAG);
                if (bottomSheet != null) {
                    bottomSheet.dismiss();
                }
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            // Xử lý khi thao tác bị hủy
            if (requestCode == DELETE_PERMISSION_REQUEST_CODE) {
                Toast.makeText(getContext(), "Thao tác xóa đã bị hủy bởi người dùng.", Toast.LENGTH_SHORT).show();
                // Tìm và đóng VideoOptionsBottomSheet nếu nó vẫn đang hiển thị
                BottomSheetDialogFragment bottomSheet = (BottomSheetDialogFragment) getChildFragmentManager().findFragmentByTag(VideoOptionsBottomSheet.TAG);
                if (bottomSheet != null) {
                    bottomSheet.dismiss();
                }
            } else {
                Toast.makeText(getContext(), "Thao tác đã bị hủy.", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Lỗi không xác định: resultCode = " + resultCode, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Kiểm tra quyền truy cập bộ nhớ và tải video nếu được cấp.
     * Nếu chưa có quyền, sẽ yêu cầu quyền động.
     */
    private void loadVideos() {
        String readPermission;
        // Quyền WRITE_EXTERNAL_STORAGE không còn cần thiết cho việc đọc trên API 29+
        // và không hiệu quả cho việc xóa tệp của ứng dụng khác trên Android 10+.
        // Chỉ cần quyền đọc để hiển thị danh sách video.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) { // API 33+
            readPermission = Manifest.permission.READ_MEDIA_VIDEO;
        } else { // API < 33
            readPermission = Manifest.permission.READ_EXTERNAL_STORAGE;
        }

        if (getContext() != null) {
            if (ContextCompat.checkSelfPermission(getContext(), readPermission) == PackageManager.PERMISSION_GRANTED) {
                retrieveVideosFromDevice();
            } else {
                // Yêu cầu quyền từ người dùng nếu chưa được cấp
                requestPermissions(new String[]{readPermission}, PERMISSION_REQUEST_CODE);
                Log.d("OneFragment", "Đang yêu cầu quyền truy cập bộ nhớ.");
            }
        }
    }

    /**
     * Xử lý kết quả yêu cầu quyền.
     * @param requestCode Mã yêu cầu quyền.
     * @param permissions Các quyền được yêu cầu.
     * @param grantResults Kết quả cấp quyền cho từng quyền.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Quyền truy cập bộ nhớ đã được cấp.", Toast.LENGTH_SHORT).show();
                retrieveVideosFromDevice(); // Tải lại video sau khi có quyền
            } else {
                Toast.makeText(getContext(), "Quyền truy cập bộ nhớ bị từ chối. Không thể tải video.", Toast.LENGTH_LONG).show();
            }
        }
    }

    private void retrieveVideosFromDevice() {
        videoList.clear(); // Xóa danh sách hiện tại trước khi tải lại

        if (getContext() == null) {
            Log.e("OneFragment", "Context is null in retrieveVideosFromDevice.");
            return;
        }

        ContentResolver contentResolver = getContext().getContentResolver();
        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.Media._ID,
                MediaStore.Video.Media.TITLE,
                MediaStore.Video.Media.DATA, // DATA không khuyến khích dùng trên API >= 29, nhưng vẫn hoạt động cho MediaStore URIs
                MediaStore.Video.Media.DURATION,
                MediaStore.Video.Media.DATE_ADDED
        };

        String sortOrder = MediaStore.Video.Media.DATE_ADDED + " DESC";

        Cursor cursor = null;
        try {
            cursor = contentResolver.query(uri, projection, null, null, sortOrder);

            if (cursor != null && cursor.moveToFirst()) {
                // Lấy chỉ mục cột, sử dụng getColumnIndexOrThrow để đảm bảo cột tồn tại
                int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID);
                int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.TITLE);
                int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATA);
                int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION);
                int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_ADDED);

                do {
                    long id = cursor.getLong(idColumn);
                    String title = cursor.getString(titleColumn);
                    String path = cursor.getString(pathColumn);
                    long duration = cursor.getLong(durationColumn);
                    long dateAddedTimestamp = cursor.getLong(dateAddedColumn); // Lấy timestamp (giây)

                    // Định dạng timestamp sang chuỗi ngày tháng dễ đọc
                    // MediaStore.Video.Media.DATE_ADDED trả về giây, cần nhân 1000 để thành mili giây cho Date
                    SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
                    String creationTime = formatter.format(new Date(dateAddedTimestamp * 1000));

                    Uri contentUri = Uri.withAppendedPath(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, String.valueOf(id));

                    VideoFile videoFile = new VideoFile(id, title, path, duration, contentUri, creationTime);
                    videoList.add(videoFile);
                } while (cursor.moveToNext());
            } else {
                Log.d("OneFragment", "Không tìm thấy video nào trong MediaStore.");
            }
        } catch (Exception e) {
            Log.e("OneFragment", "Lỗi khi truy xuất video: " + e.getMessage());
            Toast.makeText(getContext(), "Lỗi khi tải video: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        videoAdapter.notifyDataSetChanged(); // Cập nhật RecyclerView sau khi tải dữ liệu
        Log.d("OneFragment", "Số lượng video sau khi tải lại: " + videoList.size());
        if (videoList.isEmpty()) {
            Toast.makeText(getContext(), "Không tìm thấy video nào.", Toast.LENGTH_SHORT).show(); // Hiển thị Toast nếu không có video nào
        }
    }

    // Triển khai phương thức từ interface VideoOptionListener
    @Override
    public void onVideoDeleted() {
        // Phương thức này chỉ được gọi khi xóa thành công trên Android < 10
        // hoặc nếu bạn muốn gọi nó sau khi MediaStore.createDeleteRequest() hoàn tất
        // Với Android 10+, việc tải lại danh sách sẽ được thực hiện trong onActivityResult sau DELETE_PERMISSION_REQUEST_CODE
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            loadVideos();
        }
        // Trên Android 10+, onActivityResult đã xử lý việc tải lại và đóng BottomSheet.
    }
}